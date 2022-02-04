package io.github.contractautomata.RunnableOrchestration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import contractAutomata.automaton.Automaton;
import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.label.Label;
import contractAutomata.automaton.state.BasicState;
import contractAutomata.automaton.state.CAState;
import contractAutomata.automaton.state.State;
import contractAutomata.automaton.transition.MSCATransition;
import contractAutomata.automaton.transition.Transition;
import contractAutomata.operators.CompositionFunction;
import contractAutomata.operators.OrchestrationSynthesisOperator;
import contractAutomata.requirements.StrongAgreement;
import io.github.contractautomata.RunnableOrchestration.actions.OrchestratorAction;

/**
 * Abstract class implementing the runtime environment of a contract automata orchestration.
 * 
 * @author Davide Basile
 */
public abstract class RunnableOrchestration implements Runnable {

	public final static String stop_msg = "ORC_STOP";
	public final static String choice_msg = "ORC_CHOICE";
	public final static String stop_choice = "CHOICE_STOP";
	private final List<Integer> ports;
	private final List<String> addresses;
	private final MSCA contract;
	private final Predicate<MSCATransition> pred;
	private CAState currentState;
	private OrchestratorAction act;


	public RunnableOrchestration(Automaton<String, BasicState,Transition<String, BasicState,Label>> req, 
			Predicate<MSCATransition> pred, List<MSCA> contracts, List<String> hosts, List<Integer> port, OrchestratorAction act) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		MSCA comp=new CompositionFunction().apply(contracts,pred.negate(),100);

		contract = new OrchestrationSynthesisOperator(pred,req).apply(comp); 

		if (contract==null)
			throw new IllegalArgumentException("No orchestration");

		System.out.println("The orchestration is : " + contract.toString());


		this.pred=pred;
		this.addresses = hosts;
		this.ports = port;
		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);
		this.act=act;
	}

	public RunnableOrchestration(Automaton<String, BasicState,Transition<String, BasicState,Label>> req, 
			Predicate<MSCATransition> pred, MSCA orchestration, List<String> hosts, List<Integer> port, OrchestratorAction act) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		this.pred=pred;
		this.contract = orchestration; 
		this.addresses = hosts;
		this.ports = port;
		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);
		this.act=act;
	}
	
	public MSCA getContract() {
		return contract;
	}

	public CAState getCurrentState() {
		return currentState;
	}
	
	

	public List<Integer> getPorts() {
		return ports;
	}

	public List<String> getAddresses() {
		return addresses;
	}

	@Override
	public void run() {
		try (   AutoCloseableList<Socket> sockets = new AutoCloseableList<Socket>();
				AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<ObjectOutputStream>();
				AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<ObjectInputStream>();)
		{
			//initialising i/o
			for (int i=0;i<ports.size();i++) {
				Socket s = new Socket(InetAddress.getByName(addresses.get(i)), ports.get(i));
				sockets.add(s);
				oout.add(new ObjectOutputStream(s.getOutputStream()));
				oout.get(i).flush();
				oin.add(new ObjectInputStream(s.getInputStream()));
			}


			while(true)
			{
				System.out.println("Orchestrator, current state is "+currentState.toString());

				//get forward state of the state
				List<MSCATransition> fs = new ArrayList<>(contract.getForwardStar(currentState));

				//check absence of deadlocks
				if (fs.isEmpty()&&!currentState.isFinalstate()) 
					throw new RuntimeException("Deadlocked Orchestration!");


				//the choice on the transition to fire or to terminate is made beforehand
				final String choice;
				if (fs.size()>1 || (currentState.isFinalstate() && fs.size()==1))
				{
					System.out.println("Orchestrator sending choice message");
					for (ObjectOutputStream o : oout) {
						o.writeObject(RunnableOrchestration.choice_msg);
						o.flush();
					}
					choice = choice(oout,oin);
				}
				else if (fs.size()==1)
					choice = fs.get(0).getLabel().getUnsignedAction();
				else
					choice="";//for initialization

				//check final state
				if (currentState.isFinalstate() && (fs.isEmpty()||choice==stop_choice))
				{

					System.out.println("Orchestrator sending termination message");
					for (ObjectOutputStream o : oout) {
						o.flush();
						o.writeObject(stop_msg);
					}
					return;
				}

				//select a transition to fire
				MSCATransition t = fs.stream()
						.filter(tr->tr.getLabel().getUnsignedAction().equals(choice))
						.findAny()
						.orElseThrow(RuntimeException::new);


				if (t.getLabel().isRequest())
					throw new RuntimeException("The orchestration has unmatched requests!");

				if (t.getLabel().isOffer()&&(pred instanceof StrongAgreement))
					throw new RuntimeException("The orchestration has unmatched offers!");

				System.out.println("Orchestrator, selected transition is "+t.toString());

				act.doAction(this, t, oout, oin);
				
				currentState = t.getTarget();
			}

		}catch (Exception e) {
			RuntimeException re = new RuntimeException();
			re.addSuppressed(e);
			throw new RuntimeException();
		}
	}	
	
	/**
	 * 
	 * @param oout	output to the services
	 * @param oin	input from the services
	 * @return	the selected action to fire
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract String choice(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException;
	
}
