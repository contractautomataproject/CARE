package io.github.contractautomata.care.runnableOrchestration.choice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.contractautomata.care.label.TypedCALabel;
import io.github.contractautomata.care.runnableOrchestration.AutoCloseableList;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.care.runnableOrchestration.actions.OrchestratorAction;
import io.github.contractautomata.catlib.automaton.Automaton;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

/**
 * each choice is solved by asking the services, and selecting the (or one of the) 
 * most frequent choice
 *
 * @author Davide Basile
 *
 */
public class MajoritarianChoiceRunnableOrchestration extends RunnableOrchestration {


	public MajoritarianChoiceRunnableOrchestration(Automaton<String, Action, State<String>, ModalTransition<String, Action, State<String>, Label<Action>>> req,
												   Predicate<CALabel> pred,
												   List<Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>, TypedCALabel>>> contracts,
												   List<String> hosts, List<Integer> port, OrchestratorAction act) throws UnknownHostException, ClassNotFoundException, IOException {
		super(req, pred, contracts, hosts, port, act);
	}



	public MajoritarianChoiceRunnableOrchestration(Predicate<CALabel> pred,
												   Automaton<String, Action, State<String>, ModalTransition<String,Action,State<String>,CALabel>> orchestration,
												   List<String> hosts, List<Integer> port, OrchestratorAction act) throws ClassNotFoundException, IOException {
		super(pred,orchestration,hosts,port,act);

	}


	@Override
	public String choice(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin)
			throws IOException, ClassNotFoundException {

		//computing services that can choose (those involved in one next transition)
		Set<Integer> toInvoke = this.getContract()
				.getForwardStar(this.getCurrentState()).stream()
				.flatMap(t->t.getLabel().isOffer()?Stream.of(t.getLabel().getOfferer())
						:Stream.of(t.getLabel().getOfferer(),t.getLabel().getRequester()))
				.collect(Collectors.toSet());

		//asking to the services either to choose or to skip
		for (int i=0;i<oout.size();i++){
			ObjectOutputStream oos = oout.get(i);
			oos.writeObject(toInvoke.contains(i)?RunnableOrchestration.choice_msg:null);
			oout.get(i).flush();
		}

		//computing and sending the possible choices
		String[] toChoose = this.getContract()
				.getForwardStar(this.getCurrentState()).stream()
				.map(t->t.getLabel().getAction().getLabel())
				.toArray(String[]::new);
		for (Integer i : toInvoke) {
			oout.get(i).writeObject(toChoose);
			oout.get(i).flush();
		}

		//receiving the choice of each service
		List<String> choices = new ArrayList<>(toInvoke.size());
		for (Integer i : toInvoke) {
			String selection = (String) oin.get(i).readObject();
			if ((selection.equals(RunnableOrchestration.stop_choice)&&this.getCurrentState().isFinalState())
					|| Arrays.stream(toChoose).anyMatch(x->x.equals(selection)))
				choices.add(selection);
		}

		return	choices.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet().stream()
				.max(Comparator.comparingInt(x -> x.getValue().intValue())).orElseThrow(RuntimeException::new).getKey();

	}

	@Override
	public String getChoiceType() {
		return "Majoritarian";
	}



}
