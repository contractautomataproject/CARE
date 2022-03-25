package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;

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
	public void doAction(RunnableOrchestration ro, ModalTransition<String, Action, State<String>, ? extends CALabel> t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException ;
	
	public String getActionType();
}

