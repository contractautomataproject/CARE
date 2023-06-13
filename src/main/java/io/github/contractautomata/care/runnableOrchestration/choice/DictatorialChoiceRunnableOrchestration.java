package io.github.contractautomata.care.runnableOrchestration.choice;

import io.github.contractautomata.care.label.TypedCALabel;
import io.github.contractautomata.care.runnableOrchestration.AutoCloseableList;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Orchestration class resolving choices by assigning 
 * a uniform distribution and picking one, with 
 *  50% chance of terminating when possible.
 * 
 * @author Davide Basile
 *
 */
public class DictatorialChoiceRunnableOrchestration extends RunnableOrchestration  {

	private final Random generator;

	public DictatorialChoiceRunnableOrchestration(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req,
												  Predicate<CALabel> pred,
												  List<Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, TypedCALabel>>> contracts,
												  List<String> hosts, List<Integer> port, OrchestratorAction act) throws UnknownHostException, ClassNotFoundException, IOException {
		super(req, pred, contracts, hosts, port,act);
		generator = new Random();
	}

	public DictatorialChoiceRunnableOrchestration(Predicate<CALabel> pred,
												  Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, CALabel>> orchestration,
												  List<String> hosts, List<Integer> port, OrchestratorAction act) throws UnknownHostException, ClassNotFoundException, IOException {
		super(pred, orchestration, hosts, port,act);
		generator = new Random();
	}
	
	@Override
	/**
	 *  implementation of a branch choice made solely by the orchestrator
	 * 
	 * @param oout	output to the services
	 * @param oin	input from the services
	 * @return	the selected action to fire
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public String choice(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {
		
		List<ModalTransition<String,Action,State<String>,CALabel>> fs = new ArrayList<>(this.getContract().getForwardStar(this.getCurrentState()));
		
		if (this.getCurrentState().isFinalState())
		{
			int n=generator.nextInt(2);
			if (n==0||fs.size()==0)
				return RunnableOrchestration.stop_choice;
		}
		
		int n= generator.nextInt(fs.size());
		return fs.get(n).getLabel().getAction().getLabel();

	}
	
	@Override
	public String getChoiceType() {
		return "Dictatorial";
	}

}
