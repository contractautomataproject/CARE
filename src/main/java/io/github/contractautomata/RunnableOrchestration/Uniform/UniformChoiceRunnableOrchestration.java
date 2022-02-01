package io.github.contractautomata.RunnableOrchestration.Uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import contractAutomata.automaton.Automaton;
import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.label.Label;
import contractAutomata.automaton.state.BasicState;
import contractAutomata.automaton.transition.MSCATransition;
import contractAutomata.automaton.transition.Transition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;

/**
 * Abstract orchestration class resolving choices by assigning 
 * a uniform distribution and picking one. 
 * When in a final state, there is 50% chance of terminating.
 * 
 * @author Davide Basile
 *
 */
public class UniformChoiceRunnableOrchestration extends RunnableOrchestration {

	private final Random generator;

	public UniformChoiceRunnableOrchestration(Automaton<String, BasicState,Transition<String, BasicState,Label>> req, 
			Predicate<MSCATransition> pred, List<MSCA> contracts, List<String> hosts, List<Integer> port) {
		super(req, pred, contracts, hosts, port);
		generator = new Random();
	}

	@Override
	public String choice(List<String> args)
	{
		List<MSCATransition> fs = new ArrayList<>(this.getContract().getForwardStar(this.getCurrentState()));
		
		if (this.getCurrentState().isFinalstate())
		{
			int n=generator.nextInt(2);
			if (n==0||fs.size()==0)
				return RunnableOrchestration.stop_choice;
		}
		
		int n= generator.nextInt(fs.size());
		return fs.get(n).getLabel().getUnsignedAction();
	}

}
