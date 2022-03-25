package io.github.contractautomata.RunnableOrchestration.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.RunnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.Automaton;
import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;
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
	public void choice(State<String> currentState, ObjectOutputStream oout, ObjectInputStream oin) throws IOException, ClassNotFoundException {

		//receive message from orchestrator on whether to choose or skip
		String action = (String) oin.readObject();
		
		System.out.println("received "+action);
		
		if (action==null) //skip
			return;
		
		//receiving the possible choices
		String[] toChoose = (String[]) oin.readObject();
		
		String select =select(currentState, toChoose);
		
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
		if (currentState.isFinalState()&&generator.nextInt(2)==0) //50% chance of terminating
			return RunnableOrchestration.stop_choice; 
		else		
			return toChoose[generator.nextInt(toChoose.length)];
	}

	@Override
	public String getChoiceType() {
		return "Majoritarian";
	}
}
