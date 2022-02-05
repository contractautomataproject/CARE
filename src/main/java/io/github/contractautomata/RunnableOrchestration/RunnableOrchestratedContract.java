package io.github.contractautomata.RunnableOrchestration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.state.CAState;
import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.actions.OrchestratedAction;

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
	private final Object service;
	private final OrchestratedAction act;

	public RunnableOrchestratedContract(MSCA contract, int port, Object service, OrchestratedAction act) throws IOException {
		super();
		this.contract = contract;
		this.port = port;
		this.service=service;
		this.act=act;
	}


	public int getPort() {
		return port;
	}

	public MSCA getContract() {
		return contract;
	}

	public Object getService() {
		return service;
	}
	
	@Override
	public void run() {
		try (ServerSocket servsock =  ServerSocketFactory.getDefault().createServerSocket(port);)
		{
		    while (true) {
		    	new Thread() {
		    		private Socket socket;
		    		private CAState currentState;
		    		{
		    			socket = servsock.accept();

		    			currentState = contract.getStates().parallelStream()
		    					.filter(CAState::isInitial)
		    					.findAny()
		    					.orElseThrow(IllegalArgumentException::new);
		    		}
		    		
		    		@Override
		    		public void run() {
						try    (ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
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
									choice(currentState,oout,oin);
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
											act.invokeMethod(RunnableOrchestratedContract.this, m1, oin, oout, t);
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
		    	}.start();
		    }
		} catch (IOException e2) {
			RuntimeException re = new RuntimeException();
			re.addSuppressed(e2);
			throw new RuntimeException(e2);
		} 
	}


	public abstract void choice(CAState currentState,ObjectOutputStream oout, ObjectInputStream oin) throws Exception;


}

	