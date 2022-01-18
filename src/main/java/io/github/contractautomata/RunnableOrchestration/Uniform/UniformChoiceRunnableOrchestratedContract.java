package io.github.contractautomata.RunnableOrchestration.Uniform;

import java.io.IOException;
import java.util.List;

import contractAutomata.automaton.MSCA;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;

public class UniformChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract {

	public UniformChoiceRunnableOrchestratedContract(MSCA contract, int port, Object service) throws IOException {
		super(contract, port, service);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String choice(List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}


}
