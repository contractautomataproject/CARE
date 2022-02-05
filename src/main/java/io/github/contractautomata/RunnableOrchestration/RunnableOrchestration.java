package io.github.contractautomata.RunnableOrchestration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.github.contractautomata.RunnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.label.TypedCALabel;
import io.github.davidebasile.contractautomata.automaton.Automaton;
import io.github.davidebasile.contractautomata.automaton.MSCA;
import io.github.davidebasile.contractautomata.automaton.label.Label;
import io.github.davidebasile.contractautomata.automaton.state.BasicState;
import io.github.davidebasile.contractautomata.automaton.state.CAState;
import io.github.davidebasile.contractautomata.automaton.state.State;
import io.github.davidebasile.contractautomata.automaton.transition.MSCATransition;
import io.github.davidebasile.contractautomata.automaton.transition.Transition;
import io.github.davidebasile.contractautomata.operators.CompositionFunction;
import io.github.davidebasile.contractautomata.operators.OrchestrationSynthesisOperator;
import io.github.davidebasile.contractautomata.requirements.StrongAgreement;

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


	public RunnableOrchestration(Automaton<String, String, BasicState,Transition<String, String, BasicState,Label<String>>> req, 
			Predicate<MSCATransition> pred, List<MSCA> contracts, List<String> hosts, List<Integer> port, OrchestratorAction act) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		if (!contracts.stream()
				.allMatch(c->c.getTransition().parallelStream()
						.map(MSCATransition::getLabel)
						.allMatch(l -> l instanceof TypedCALabel)))
			throw new IllegalArgumentException("A contract has no typed label");

		this.pred=pred;
		this.addresses = hosts;
		this.ports = port;
		this.act=act;

		MSCA comp=new CompositionFunction(contracts).apply(pred.negate(),Integer.MAX_VALUE);
		contract = new OrchestrationSynthesisOperator(pred,req).apply(comp); 

	}

	public RunnableOrchestration(Automaton<String, String, BasicState,Transition<String, String, BasicState,Label<String>>> req, 
			Predicate<MSCATransition> pred, MSCA orchestration, List<String> hosts, List<Integer> port, OrchestratorAction act) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		this.pred=pred;
		this.contract = orchestration; 
		this.addresses = hosts;
		this.ports = port;
		this.act=act;
	}

	public MSCA getContract() {
		return contract;
	}

	public boolean isEmptyOrchestration() {
		return contract==null;
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
		if (this.isEmptyOrchestration())
			throw new IllegalArgumentException("Empty orchestration");

		System.out.println("The orchestration is : " + contract.toString());

		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);

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
