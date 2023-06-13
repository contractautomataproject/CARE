package io.github.contractautomata.care.runnableOrchestration.choice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import io.github.contractautomata.care.exceptions.ContractViolationException;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
/**
 * The service when asked upon send its branch/termination choice, 
 * by assigning  a uniform distribution and picking one, with 
 *  50% chance of terminating when possible.
 *  
 * @author Davide Basile
 *
 */
public class MajoritarianChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract   {

	private final Random generator;

	public MajoritarianChoiceRunnableOrchestratedContract(Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, CALabel>> contract,
														  int port, Object service, OrchestratedAction act) throws IOException {
		super(contract, port, service, act);
		generator = new Random();
	}

	@Override
	public synchronized void choice(State<String> currentState, ObjectOutputStream oout, ObjectInputStream oin) throws IOException, ClassNotFoundException {

		//receive message from orchestrator on whether to choose or skip
		String action = (String) oin.readObject();

		System.out.println("received "+action);
		
		if (action==null) //skip
			return;
		
		//receiving the possible choices
		String[] toChoose = (String[]) oin.readObject();

		System.out.println("received "+ Arrays.toString(toChoose));

		String select =select(currentState, toChoose);

		System.out.println("replying "+ select);
		//sending the selected choice
		oout.writeObject(select);
		oout.flush();
	}


	/**
	 * To override for changing policy of selection
	 * 
	 * @param toChoose  the list of possible choices
	 * @return the choice made to be communicated to the orchestrator
	 */
	public String select(State<String> currentState, String[] toChoose) {
		if (currentState.isFinalState()&&(toChoose.length==0||generator.nextInt(2)==0)) //50% chance of terminating
			return RunnableOrchestration.stop_choice; 
		else if (!currentState.isFinalState()&&toChoose.length==0)
			throw new RuntimeException();
		else
			return toChoose[generator.nextInt(toChoose.length)];
	}

	@Override
	public String getChoiceType() {
		return "Majoritarian";
	}
}
