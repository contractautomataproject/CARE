package io.github.contractautomata.RunnableOrchestration.Uniform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.interfaces.CentralisedOrchestratedAction;

public class UniformChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract implements CentralisedOrchestratedAction {

	public UniformChoiceRunnableOrchestratedContract(MSCA contract, int port, Object service) throws IOException {
		super(contract, port, service);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void choice(ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
//		oout.writeObject(null);
//		oout.flush();
	}


	@Override
	public void invokeMethod(Object service, Method m1, ObjectInputStream oin, ObjectOutputStream oout,
			MSCATransition t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, IOException {
		this.invokeMethod(service, m1, oin, oout, t);
		
	}

}
