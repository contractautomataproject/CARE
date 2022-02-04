package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;

public class CentralisedOrchestratedAction implements OrchestratedAction {
	
	/**
	 * default implementation for a method invocation commanded by the centralised action orchestrator 
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
	public void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, MSCATransition t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {
		Class<?> c=m1.getParameterTypes()[0];
		Object req=m1.invoke(rc.getService(),c.cast(oin.readObject()));
		oout.writeObject(req);
		oout.flush();
		
		if (t.getLabel().isRequest()) {
			//if the action is a request, the payload from the offerer will be received
			m1.invoke(rc.getService(),c.cast(oin.readObject()));
		}
	}
}
