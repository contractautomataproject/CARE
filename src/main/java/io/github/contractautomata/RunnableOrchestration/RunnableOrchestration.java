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

/**
 * Abstract class implementing an interpreter/orchestrator of a contract automata orchestration.
 * 
 * @author Davide Basile
 */
public abstract class RunnableOrchestration implements Runnable, Choice<String> {

	public final static String stop_msg = "ORC_STOP";
	public final static String choice_msg = "ORC_CHOICE";
	public final static String stop_choice = "CHOICE_STOP";
	private final List<Integer> port;
	private final List<String> hosts;
	private final MSCA contract;
	private final Predicate<MSCATransition> pred;
	private CAState currentState;

	public RunnableOrchestration(Automaton<String, BasicState,Transition<String, BasicState,Label>> req, 
			Predicate<MSCATransition> pred, List<MSCA> contracts, List<String> hosts, List<Integer> port) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		MSCA comp=new CompositionFunction().apply(contracts,pred.negate(),100);

		contract = new OrchestrationSynthesisOperator(pred,req).apply(comp); 

		if (contract==null)
			throw new IllegalArgumentException("No orchestration");

		System.out.println("The orchestration is : " + contract.toString());


		this.pred=pred;
		this.hosts = hosts;
		this.port = port;
		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);
	}

	public RunnableOrchestration(Automaton<String, BasicState,Transition<String, BasicState,Label>> req, 
			Predicate<MSCATransition> pred, MSCA orchestration, List<String> hosts, List<Integer> port) {
		super();

		if (hosts.size()!=port.size())
			throw new IllegalArgumentException();

		this.pred=pred;
		this.contract = orchestration; 
		this.hosts = hosts;
		this.port = port;
		this.currentState = contract.getStates().parallelStream()
				.filter(State::isInitial)
				.findAny()
				.orElseThrow(IllegalArgumentException::new);
	}
	
	public MSCA getContract() {
		return contract;
	}

	public CAState getCurrentState() {
		return currentState;
	}

	@Override
	public void run() {
		try (   AutoCloseableList<Socket> sockets = new AutoCloseableList<Socket>();
				AutoCloseableList<ObjectOutputStream> oout = new AutoCloseableList<ObjectOutputStream>();
				AutoCloseableList<ObjectInputStream> oin = new AutoCloseableList<ObjectInputStream>();)
		{
			//initialising i/o
			for (int i=0;i<port.size();i++) {
				Socket s = new Socket(InetAddress.getByName(hosts.get(i)), port.get(i));
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
				String choice;
				if (fs.size()>1 || currentState.isFinalstate())
				{

					System.out.println("Orchestrator sending choice message");
					for (ObjectOutputStream o : oout) {
						o.writeObject(choice_msg);
						o.flush();
					}
					List<String> choices = new ArrayList<>(); 
					for (int i=0;i<port.size();i++)
						choices.add((String) oin.get(i).readObject());
					choice = choice(choices);
				}
				else
					choice = fs.get(0).getLabel().getUnsignedAction();

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

				if (t.getLabel().isOffer())
					offer(t,oout,oin);
				else 
					match(t,oout,oin);
				currentState = t.getTarget();
			}

		}catch (Exception e) {
			RuntimeException re = new RuntimeException();
			re.addSuppressed(e);
			throw new RuntimeException();
		}
	}
	
	public void match(MSCATransition t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {
		// match: firstly interact with the requester
		oout.get(t.getLabel().getRequester()).writeObject(t.getLabel().getUnsignedAction());
		oout.get(t.getLabel().getRequester()).flush();
		oout.get(t.getLabel().getRequester()).writeObject(null);
		oout.get(t.getLabel().getRequester()).flush();
		
		Object rep_req = oin.get(t.getLabel().getRequester()).readObject();

		//forwarding the received requester payload to the offerer
		oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getUnsignedAction());
		oout.get(t.getLabel().getOfferer()).flush();
		oout.get(t.getLabel().getOfferer()).writeObject(rep_req);
		oout.get(t.getLabel().getOfferer()).flush();

		Object rep_off = oin.get(t.getLabel().getOfferer()).readObject();
		
		//forwarding the received  offerer payload to the requester
		oout.get(t.getLabel().getRequester()).writeObject(rep_off);
		oout.get(t.getLabel().getRequester()).flush();
	}
	
	public void offer(MSCATransition t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {
		//only invokes the offerer and then continue
		oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getUnsignedAction());
		oout.get(t.getLabel().getOfferer()).flush();
		oout.get(t.getLabel().getOfferer()).writeObject(null);
		oout.get(t.getLabel().getOfferer()).flush();
		oin.get(t.getLabel().getOfferer()).readObject();
	}
}


//	private String interactWith(MSCATransition t, AutoCloseableList<BufferedReader> in, 
//			AutoCloseableList<PrintWriter> out, 
//			String payload, String partner) throws IOException {
//		payload = (payload==null)?"()":payload;
//		Integer ind_partner = (partner.equals("offerer"))?t.getLabel().getOfferer():t.getLabel().getRequester();
//		out.get(ind_partner).println(t.getLabel().getUnsignedAction()
//				+payload);
//		String rep = in.get(ind_partner).readLine();
//		checkAnswer(rep,
//				(partner.equals("offerer"))?t.getLabel().getAction():t.getLabel().getCoAction(),
//						partner);
//		return rep;
//	}

//	private void checkAnswer(String rep, String expected, String partner) {
//		if (!rep.substring(0, rep.indexOf("(")).equals(expected))
//			new RuntimeException("Expecting \"" +expected+"\""+ System.lineSeparator()+
//					"Received  answer \""+rep.substring(0, rep.indexOf("(")) +
//					" from "+partner);
//	}
