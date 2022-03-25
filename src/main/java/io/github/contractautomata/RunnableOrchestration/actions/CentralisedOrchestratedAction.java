package io.github.contractautomata.RunnableOrchestration.actions;

import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CentralisedOrchestratedAction implements OrchestratedAction {
	
	/**
	 * default implementation for a method invocation commanded by the centralised action orchestrator 
	 * 
	 * @param m1		the method of service to call
	 * @param oin		input from the orchestrator
	 * @param oout		output to the orchestrator
	 * @param t			transition of the contract selected to be fired
	 */
	public void invokeMethod(RunnableOrchestratedContract rc, Method m1, ObjectInputStream oin, ObjectOutputStream oout, ModalTransition<String, Action, State<String>, TypedCALabel> t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {
		Class<?> c=m1.getParameterTypes()[0];
		Object req=m1.invoke(rc.getService(),c.cast(oin.readObject()));
		oout.writeObject(req);
		oout.flush();
		
		if (t.getLabel().isRequest()) {
			//if the action is a request, the payload from the offerer will be received
			m1.invoke(rc.getService(),c.cast(oin.readObject()));
		}
	}
	

	@Override
	public String getActionType() {
		return "Centralised";
	}		

}
