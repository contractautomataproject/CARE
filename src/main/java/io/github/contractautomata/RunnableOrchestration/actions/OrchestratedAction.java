package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.davidebasile.contractautomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;

public interface OrchestratedAction {
	
	/**
	 * 
	 * @param service	the class implementing the contract
	 * @param m1		the method of service to call
	 * @param oin		input from the orchestrator
	 * @param oout		output to the orchestrator
	 * @param t			transition of the contract selected to be fired
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, MSCATransition t ) throws Exception;

	public String getActionType();
}
