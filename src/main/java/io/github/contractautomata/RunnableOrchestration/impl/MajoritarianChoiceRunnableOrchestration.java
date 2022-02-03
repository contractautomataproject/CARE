package io.github.contractautomata.RunnableOrchestration.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import contractAutomata.automaton.Automaton;
import contractAutomata.automaton.MSCA;
import contractAutomata.automaton.label.Label;
import contractAutomata.automaton.state.BasicState;
import contractAutomata.automaton.transition.MSCATransition;
import contractAutomata.automaton.transition.Transition;
import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.RunnableOrchestration.actions.CentralisedOrchestratorAction;

/**
 * each choice is solved by asking the services, and selecting the (or one of the) 
 * most frequent choice
 * 
 * @author Davide Basile
 *
 */
public class MajoritarianChoiceRunnableOrchestration extends RunnableOrchestration implements CentralisedOrchestratorAction{

	public MajoritarianChoiceRunnableOrchestration(Automaton<String, BasicState, Transition<String, BasicState, Label>> req,
			Predicate<MSCATransition> pred, List<MSCA> contracts, List<String> hosts, List<Integer> port) {
		super(req, pred, contracts, hosts, port);
	}

	@Override
	public String choice(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin)
			throws IOException, ClassNotFoundException {

		//computing services that can choose (those involved in one next transition)
		Set<Integer> toInvoke = this.getContract()
		.getForwardStar(this.getCurrentState()).stream()
		.flatMap(t->t.getLabel().isOffer()?Stream.of(t.getLabel().getOfferer())
				:Stream.of(t.getLabel().getOfferer(),t.getLabel().getRequester()))
		.distinct().collect(Collectors.toSet());
		
		//asking either to choose or skip to the services
		for (int i=0;i<oout.size();i++){
			ObjectOutputStream oos = oout.get(i);
			oos.writeObject(toInvoke.contains(i)?RunnableOrchestration.choice_msg:null);
			oout.get(i).flush();
		}
		
		//computing and sending the possible choices
		String[] toChoose = this.getContract()
				.getForwardStar(this.getCurrentState()).stream()
				.map(t->t.getLabel().getUnsignedAction())
				.toArray(String[]::new);
		for (Integer i : toInvoke) {
			oout.get(i).writeObject(toChoose);
			oout.get(i).flush();
		}
		
		//receiving the choice of each service
		List<String> choices = new ArrayList<>(); 
		for (int i=0;i<oin.size();i++)
			choices.add((String) oin.get(i).readObject());
		
		return	choices.stream()
		.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
		.entrySet().stream()
		.max((x,y)->x.getValue().intValue()-y.getValue().intValue()).orElseThrow(RuntimeException::new).getKey();
		
	}

	@Override
	public void doAction(MSCATransition t, AutoCloseableList<ObjectOutputStream> oout,
			AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {
		CentralisedOrchestratorAction.super.doAction(t, oout, oin);
		
	}

}
