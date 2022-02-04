package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;

public interface OrchestratorAction {

	/**
	 * 
	 * @param t			the transition to fire
	 * @param oout		outputs to the services
	 * @param oin		inputs from the services
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 */
	public void doAction(RunnableOrchestration ro, MSCATransition t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException ; 
	
}

