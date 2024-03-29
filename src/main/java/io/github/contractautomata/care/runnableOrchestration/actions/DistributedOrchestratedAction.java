package io.github.contractautomata.care.runnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.label.TypedCALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

public class DistributedOrchestratedAction implements OrchestratedAction {

	/**
	 *
	 * @param m1		the method of service to call
	 * @param oin		input from the orchestrator
	 * @param oout		output to the orchestrator
	 * @param t			transition of the contract selected to be fired
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws  
	 */
	public synchronized void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, ModalTransition<String, Action, State<String>, TypedCALabel> t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {

		Class<?> c=m1.getParameterTypes()[0];

		if (t.getLabel().isOffer())
		{
			//receive type from the orchestrator
			String type = (String)oin.readObject();

			if (type.equals("match"))	
			{
				//if is a match communicate with the requester
				ServerSocket ss = new ServerSocket(0);//find a port available
				oout.writeObject(ss.getLocalPort());//communicate the port to the orchestrator

				try (	Socket socket = ss.accept();
						ObjectInputStream osin = new ObjectInputStream(socket.getInputStream());
						ObjectOutputStream osout = new ObjectOutputStream(socket.getOutputStream())){

					osout.flush();
					//receive request from the requester and invoke offer method
					Object offer=m1.invoke(rc.getService(),c.cast(osin.readObject()));

					//provide offer to the requester
					osout.writeObject(offer);
					osout.flush();

					//wait ack
					osin.readObject();
				} catch (Exception e) {
					RuntimeException re = new RuntimeException();
					re.addSuppressed(e);
					ss.close();
					throw re;
				}
				ss.close();
			}
			else { //if is an offer communicate with the orchestrator
				Object req=m1.invoke(rc.getService(),c.cast(oin.readObject()));
				oout.writeObject(req);
				oout.flush();
			}
			
		}
		else if (t.getLabel().isRequest()) {
			final Object req=m1.invoke(rc.getService(),c.cast(null));
			final String address = (String) oin.readObject();
			final Integer port = (Integer) oin.readObject();

			try (Socket socket = new Socket(InetAddress.getByName(address), port);
					ObjectOutputStream osout = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream osin = new ObjectInputStream(socket.getInputStream()))
			{
				osout.flush();
				//send request to the offerer
				osout.writeObject(req);

				//receive and consume offer
				m1.invoke(rc.getService(),c.cast(osin.readObject()));

				//ack to the offerer
				osout.writeObject(null);
				
				//final ack to the orchestrator
				oout.writeObject(null);
				oout.flush();

			} catch (Exception e) {
				e.printStackTrace();
			};
		}

	}
	
	@Override
	public String getActionType() {
		return "Distributed";
	}		
}
