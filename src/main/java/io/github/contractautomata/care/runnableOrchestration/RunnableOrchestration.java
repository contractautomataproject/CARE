package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.label.TypedCALabel;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import io.github.contractautomata.catlib.operators.CompositionFunction;
import io.github.contractautomata.catlib.operators.OrchestrationSynthesisOperator;
import io.github.contractautomata.catlib.requirements.StrongAgreement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract class implementing the runtime environment of a contract automata orchestration.
 *
 * @author Davide Basile
 */
public abstract class RunnableOrchestration implements Runnable {

	public final static String stop_msg = "ORC_STOP";
	public final static String ack_msg= "ACK";
	public final static String choice_msg = "ORC_CHOICE";
	public final static String stop_choice = "CHOICE_STOP";
	public final static String check_msg = "ORC_CHECK";

	private final List<Integer> ports;
	private final List<String> addresses;
	private final Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> contract;
	private final Predicate<CALabel> pred;
	private State<String> currentState;
	private final OrchestratorAction act;


	public RunnableOrchestration(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req,
								 Predicate<CALabel> pred,
								 List<Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, TypedCALabel>>> contracts,
								 List<String> hosts, List<Integer> port, OrchestratorAction act) throws  ClassNotFoundException, IOException {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

//		if (!contracts.stream()
//				.allMatch(c->c.getTransition().parallelStream()
//						.map(ModalTransition::getLabel)
//						.allMatch(l -> l instanceof TypedCALabel)))
//			throw new IllegalArgumentException("A contract has no typed label");

		this.pred=pred;
		this.addresses = hosts;
		this.ports = port;
		this.act=act;
		checkCompatibility();

		CompositionFunction<String,State<String>,TypedCALabel,ModalTransition<String,Action,State<String>,TypedCALabel>,
				Automaton<String,Action,State<String>,ModalTransition<String,Action,State<String>,TypedCALabel>>> cf =
				new CompositionFunction<>
						(contracts, TypedCALabel::match,State::new,ModalTransition::new,TypedCALabel::new,Automaton::new,
								l->pred.negate().test(l));

		//conver to CALabel to use the synthesis
		Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> comp
				= new Automaton<>(cf.apply(Integer.MAX_VALUE)
				.getTransition().parallelStream()
				.map(t->new ModalTransition<>(t.getSource(),(CALabel)t.getLabel(),t.getTarget(),t.getModality()))
				.collect(Collectors.toSet()));

		contract = new OrchestrationSynthesisOperator<>(pred,req)
				.apply(comp);

	}

	public RunnableOrchestration(Automaton<String, Action, State<String>,ModalTransition<String,Action,State<String>,Label<Action>>> req,
								 Predicate<CALabel> pred,
								 Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> orchestration,
								 List<String> hosts, List<Integer> port, OrchestratorAction act) throws ClassNotFoundException, IOException {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		this.pred=pred;
		this.contract = orchestration;
		this.addresses = hosts;
		this.ports = port;
		this.act=act;
		checkCompatibility();
	}

	public Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> getContract() {
		return contract;
	}

	public boolean isEmptyOrchestration() {
		return contract==null;
	}

	public State<String> getCurrentState() {
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

		try (   AutoCloseableList<Socket> sockets = new AutoCloseableList<>();
				AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<>();
				AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<>())
		{
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
				List<ModalTransition<String,Action,State<String>,CALabel>> fs = new ArrayList<>(contract.getForwardStar(currentState));

				//check absence of deadlocks
				if (fs.isEmpty()&&!currentState.isFinalState())
					throw new RuntimeException("Deadlocked Orchestration!");


				//the choice on the transition to fire or to terminate is made beforehand
				final String choice;
				if (fs.size()>1 || (currentState.isFinalState() && fs.size()==1))
				{
					System.out.println("Orchestrator sending choice message");
					for (ObjectOutputStream o : oout) {
						o.writeObject(RunnableOrchestration.choice_msg);
						o.flush();
					}
					choice = choice(oout,oin);
				}
				else if (fs.size()==1)
					choice = fs.get(0).getLabel().getAction().getLabel();
				else
					choice="";//for initialization

				//check final state
				if (currentState.isFinalState() && (fs.isEmpty()||choice.equals(stop_choice)))
				{
					System.out.println("Orchestrator sending termination message");
					for (ObjectOutputStream o : oout) {
						o.flush();
						o.writeObject(stop_msg);
					}
					return;
				}

				//select a transition to fire
				ModalTransition<String,Action,State<String>,CALabel> t = fs.stream()
						.filter(tr->tr.getLabel().getAction().getLabel().equals(choice))
						.findAny()
						.orElseThrow(RuntimeException::new);


				if (t.getLabel().isRequest())
					throw new RuntimeException("The orchestration has unmatched requests!");

				if (t.getLabel().isOffer()&&(pred instanceof StrongAgreement))
					throw new RuntimeException("The orchestration has unmatched offers!");

				System.out.println("Orchestrator, selected transition is "+t);

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
	 */
	public abstract String choice(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException;

	public abstract String getChoiceType();

	private void checkCompatibility() throws IOException, ClassNotFoundException {
		for (int i=0;i<ports.size();i++) {
			try (Socket s = new Socket(InetAddress.getByName(addresses.get(i)), ports.get(i));
				 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				 ObjectInputStream ois = new ObjectInputStream(s.getInputStream()))
			{
				oos.writeObject(check_msg);
				oos.writeObject(this.getChoiceType());
				oos.writeObject(this.act.getActionType());

				String msg = (String) ois.readObject();
				if (!msg.equals(ack_msg)) {
					throw new IllegalArgumentException("Uncompatible type with service at "+s+" "+msg);
				}

			}

		}
	}

}