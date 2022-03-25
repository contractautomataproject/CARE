package io.github.contractautomata.RunnableOrchestration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.net.ServerSocketFactory;

import io.github.contractautomata.RunnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.Automaton;
import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;

/**
 * 
 * Abstract class implementing the runtime environment of an orchestrated principal contract automata.
 *  
 * @author Davide Basile
 *
 */
public abstract class RunnableOrchestratedContract implements Runnable {

	private final Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,TypedCALabel>> contract;
	private final int port;
	private final Object service;
	private final OrchestratedAction act;
	private final int timeout=600000;//10 minutes

	public RunnableOrchestratedContract(Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> contract,
										int port, Object service, OrchestratedAction act) throws IOException {
		super();
		this.port = port;
		this.service=service;
		this.act=act;

		Method[] arrm = service.getClass().getMethods();

		//change the labels to typed labels
		this.contract = new Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,TypedCALabel>>
				(contract.getTransition().stream()
				.map(t->{ Method met = Arrays.stream(arrm)
				.filter(m->m.getName().equals(t.getLabel().getAction().getLabel()))
				.findFirst().orElseThrow(IllegalArgumentException::new);
				TypedCALabel tcl = new TypedCALabel(t.getLabel(),met.getParameterTypes()[0],met.getReturnType());
				return new ModalTransition<>(t.getSource(),tcl,t.getTarget(),t.getModality());})
					.collect(Collectors.toSet()));
	}


	public int getPort() {
		return port;
	}

	public Object getService() {
		return service;
	}

	public Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,TypedCALabel>> getContract() {
		return contract;
	}
	
	@Override
	public void run() {
		try (ServerSocket servsock =  ServerSocketFactory.getDefault().createServerSocket(port);)
		{
			while (true) {
				new Thread() {
					private Socket socket;
					private io.github.contractautomataproject.catlib.automaton.state.State<String> currentState;
					{
						socket = servsock.accept();
						socket.setSoTimeout(timeout);
						currentState = contract.getStates().parallelStream()
								.filter(io.github.contractautomataproject.catlib.automaton.state.State::isInitial)
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

								if (action.equals(RunnableOrchestration.check_msg)) {
									check(oin, oout);
									break;
								}
								if (action.equals(RunnableOrchestration.stop_msg))
								{
									if (currentState.isFinalState())
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
								ModalTransition<String,Action, io.github.contractautomataproject.catlib.automaton.state.State<String>,TypedCALabel> t =
										contract.getForwardStar(currentState)
										.stream()
										.filter(tr->tr.getLabel().getAction().getLabel().equals(action))
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
									ContractViolationException re = new ContractViolationException(socket.getRemoteSocketAddress());
									re.addSuppressed(e);
									throw re;
								}

								//update state
								currentState=t.getTarget();
							}
									
						} catch (SocketTimeoutException e) {
							ContractViolationException re = new ContractViolationException(socket.getRemoteSocketAddress());
							re.addSuppressed(e);
							throw new RuntimeException(e);
						} catch (Exception e) {
							RuntimeException re = new RuntimeException();
							re.addSuppressed(e);
							throw new RuntimeException(e);
						} 

						System.out.println("Session terminated at host " + socket.getLocalAddress().toString() 
								+ ", port "+socket.getLocalPort()); 
					}
				}.start();
			}
		} catch (IOException e2) {
			RuntimeException re = new RuntimeException();
			re.addSuppressed(e2);
			throw new RuntimeException(e2);
		} 
	}


	public abstract void choice(State<String> currentState,ObjectOutputStream oout, ObjectInputStream oin) throws Exception;

	public abstract String getChoiceType();
	
	
	private void check(ObjectInputStream oin, ObjectOutputStream oout) throws ClassNotFoundException, IOException {
		String orcChoiceType = (String) oin.readObject();
		String orcActType = (String) oin.readObject();
		
		if (orcChoiceType.equals(this.getChoiceType())&&orcActType.equals(this.act.getActionType()))
			oout.writeObject(RunnableOrchestration.ack_msg);
		else {
			String msg = (orcChoiceType.equals(this.getChoiceType())?"":"uncompatible choice ") +
						 (orcActType.equals(this.act.getActionType())?"":" uncompatible action");
			oout.writeObject(msg);
		}
	}

}

