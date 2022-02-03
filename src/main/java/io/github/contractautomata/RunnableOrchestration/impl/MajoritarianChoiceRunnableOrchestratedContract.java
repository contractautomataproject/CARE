package io.github.contractautomata.RunnableOrchestration.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.RunnableOrchestration.actions.CentralisedOrchestratedAction;

/**
 * The service when asked upon send its branch/termination choice, 
 * by assigning  a uniform distribution and picking one, with 
 *  50% chance of terminating when possible.
 *  
 * @author Davide Basile
 *
 */
public class MajoritarianChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract implements CentralisedOrchestratedAction  {

	private final Random generator;

	public MajoritarianChoiceRunnableOrchestratedContract(MSCA contract, int port, Object service) throws IOException {
		super(contract, port, service);
		generator = new Random();
	}

	@Override
	public void choice(ObjectOutputStream oout, ObjectInputStream oin) throws IOException, ClassNotFoundException {

		//receive message from orchestrator on whether to choose or skip
		String action = (String) oin.readObject();
		
		System.out.println("received "+action);
		
		if (action==null) //skip
			return;
		
		//receiving the possible choices
		String[] toChoose = (String[]) oin.readObject();
		
		String select =select(toChoose);
		
		//sending the selected choice
		oout.writeObject(select);
		oout.flush();		
	}

	@Override
	public void invokeMethod(Object service, Method m1, ObjectInputStream oin, ObjectOutputStream oout,
			MSCATransition t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
	ClassNotFoundException, IOException {
		CentralisedOrchestratedAction.super.invokeMethod(service, m1, oin, oout, t);

	}
	
	/**
	 * To override for changing policy of selection
	 * 
	 * @param toChoose  the list of possible choices
	 * @return the choice made to be communicated to the orchestrator
	 */
	public String select(String[] toChoose) {
		if (this.getCurrentState().isFinalstate()&&generator.nextInt(2)==0) //50% chance of terminating
			return RunnableOrchestration.stop_choice; 
		else		
			return toChoose[generator.nextInt(toChoose.length)];
	}

}