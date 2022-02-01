package io.github.contractautomata.RunnableOrchestration.Uniform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import contractAutomata.automaton.MSCA;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;

public class UniformChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract {

	public UniformChoiceRunnableOrchestratedContract(MSCA contract, int port, Object service) throws IOException {
		super(contract, port, service);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void select(ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
//		oout.writeObject(null);
//		oout.flush();
	}

}
