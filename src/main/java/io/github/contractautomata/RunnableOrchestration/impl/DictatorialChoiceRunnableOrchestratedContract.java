package io.github.contractautomata.RunnableOrchestration.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.contractautomata.RunnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.RunnableOrchestration.actions.OrchestratedAction;
import io.github.davidebasile.contractautomata.automaton.MSCA;
import io.github.davidebasile.contractautomata.automaton.state.CAState;

public class DictatorialChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract {
	
	public DictatorialChoiceRunnableOrchestratedContract(MSCA contract, int port, Object service, OrchestratedAction act) throws IOException {
		super(contract, port, service,act);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void choice(CAState currentState,ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
	}

	@Override
	public String getChoiceType() {
		return "Distributed";
	}
}
