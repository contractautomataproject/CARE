package io.github.contractautomata.care.runnableOrchestration.choice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestratedContract;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratedAction;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

public class DictatorialChoiceRunnableOrchestratedContract extends RunnableOrchestratedContract {
	
	public DictatorialChoiceRunnableOrchestratedContract(Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, CALabel>> contract,
														 int port, Object service, OrchestratedAction act) throws IOException {
		super(contract, port, service,act);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void choice(State<String> currentState,ObjectOutputStream oout, ObjectInputStream oin) throws IOException {
	}

	@Override
	public String getChoiceType() {
		return "Distributed";
	}
}
