package io.github.contractautomata.RunnableOrchestration.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.RunnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.Automaton;
import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.Label;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;
import io.github.contractautomataproject.catlib.automaton.transition.Transition;

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

	public DictatorialChoiceRunnableOrchestration(Automaton<String, Action, State<String>, Transition<String, Action, State<String>, Label<Action>>> req,
												  Predicate<CALabel> pred,
												  List<Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, CALabel>>> contracts,
												  List<String> hosts, List<Integer> port, OrchestratorAction act) throws UnknownHostException, ClassNotFoundException, IOException {
		super(req, pred, contracts, hosts, port,act);
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
		
		List<ModalTransition<String,Action,State<String>, TypedCALabel>> fs = new ArrayList<>(this.getContract().getForwardStar(this.getCurrentState()));
		
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
		return "Distributed";
	}

}
