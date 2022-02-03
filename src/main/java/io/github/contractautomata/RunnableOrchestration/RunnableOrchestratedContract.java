package io.github.contractautomata.RunnableOrchestration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.state.CAState;
import contractAutomata.automaton.state.State;
import contractAutomata.automaton.transition.MSCATransition;

/**
 * 
 * Abstract class implementing the runtime environment of an orchestrated principal contract automata.
 *  
 * @author Davide Basile
 *
 */
public abstract class RunnableOrchestratedContract implements Runnable {

	private final MSCA contract;
	private final int port;
	private CAState currentState;
	private final Object service;

	public RunnableOrchestratedContract(MSCA contract, int port, Object service) throws IOException {
		super();
		this.contract = contract;
		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);
		this.port = port;
		this.service=service;
	}
	

	public int getPort() {
		return port;
	}

	public MSCA getContract() {
		return contract;
	}

	public CAState getCurrentState() {
		return currentState;
	}

	@Override
	public void run() {
		try    (final ServerSocket s =  ServerSocketFactory.getDefault().createServerSocket(port);
				final Socket socket = s.accept();
				ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());)
		{
			oout.flush();
			System.out.println("Connection with service started host " + socket.getLocalAddress().toString() + ", port "+socket.getLocalPort());
			while (true) {
				//receive message from orchestrator
				String action = (String) oin.readObject();
					
				System.out.println("Service on host " + socket.getLocalAddress().toString() + ", port "+socket.getLocalPort()+": received message "+action);

				if (action.equals(RunnableOrchestration.stop_msg))
				{
					if (currentState.isFinalstate())
						break;
					else
						throw new RuntimeException("Not in a final state!");
				}

				if (action.startsWith(RunnableOrchestration.choice_msg))
				{
					choice(oout,oin);
					continue;
				}

				//find a transition to fire
				MSCATransition t = contract.getForwardStar(currentState)
						.stream()
						.filter(tr->tr.getLabel().getUnsignedAction().equals(action))
						.findAny()
						.orElseThrow(UnsupportedOperationException::new);

				try {
					Method[] arrm = service.getClass().getMethods();
					for (Method m1 : arrm)
					{
						if (m1.getName().equals(action)){
							invokeMethod(service,m1,oin,oout,t);
						}
					}
				} catch(Exception e) {
					ContractViolationException re = new ContractViolationException();
					re.addSuppressed(e);
					throw re;
				}

				//update state
				currentState=t.getTarget();
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException();
			re.addSuppressed(e);
			throw new RuntimeException(e);
		} 
	}
	
	public abstract void choice(ObjectOutputStream oout, ObjectInputStream oin) throws Exception;
	
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
	 */
	public abstract void invokeMethod(Object service, Method m1, ObjectInputStream oin, ObjectOutputStream oout, MSCATransition t ) throws Exception;

}