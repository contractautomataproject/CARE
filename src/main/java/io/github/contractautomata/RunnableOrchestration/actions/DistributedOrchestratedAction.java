package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;

public class DistributedOrchestratedAction implements OrchestratedAction {

	/**
	 * 
	 * @param service	the class implementing the contract
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
	public void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, MSCATransition t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {

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
						ObjectOutputStream osout = new ObjectOutputStream(socket.getOutputStream());){

					osout.flush();

					//receive request from the requester and invoke offer method
					Object offer=m1.invoke(rc.getService(),c.cast(osin.readObject()));

					//provide offer
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
					ObjectInputStream osin = new ObjectInputStream(socket.getInputStream());)
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
}
