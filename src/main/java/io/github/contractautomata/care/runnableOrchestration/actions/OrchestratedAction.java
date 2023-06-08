package io.github.contractautomata.care.runnableOrchestration.actions;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.label.TypedCALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

public interface OrchestratedAction {
	
	/**
	 *
	 * @param m1		the method of service to call
	 * @param oin		input from the orchestrator
	 * @param oout		output to the orchestrator
	 * @param t			transition of the contract selected to be fired
	 */
	void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, ModalTransition<String, Action, State<String>, TypedCALabel> t) throws Exception;

	String getActionType();
}
