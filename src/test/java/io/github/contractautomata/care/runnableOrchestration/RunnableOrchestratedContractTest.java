package io.github.contractautomata.care.runnableOrchestration;

import io.github.contractautomata.care.runnableOrchestration.actions.CentralisedOrchestratedAction;
import io.github.contractautomata.care.runnableOrchestration.choice.MajoritarianChoiceRunnableOrchestratedContract;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Strict.class)
public class RunnableOrchestratedContractTest {

    @Mock
    Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, CALabel>> contract;

    @Mock
    Object service;

    @Before
    public void setUp() throws Exception {

        when(contract.getTransition()).thenReturn(new HashSet<>());

        RunnableOrchestratedContract alice = new MajoritarianChoiceRunnableOrchestratedContract(contract,
                8080, service, new CentralisedOrchestratedAction());
    }

    @Test
    public void test1(){

    }
}