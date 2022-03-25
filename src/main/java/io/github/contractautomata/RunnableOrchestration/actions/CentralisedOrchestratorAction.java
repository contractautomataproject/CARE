package io.github.contractautomata.RunnableOrchestration.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.label.TypedCALabel;
import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;
import io.github.contractautomataproject.catlib.automaton.state.State;
import io.github.contractautomataproject.catlib.automaton.transition.ModalTransition;

public class CentralisedOrchestratorAction  implements OrchestratorAction {

	/**
	 * Default implementation of a centralised action in an orchestration.
	 * 
	 * The method of the requester is invoked twice: 
	 * firstly passing no argument, it generates a value  
	 * that is passed as parameter to the offerer method, 
	 * which in turn produces a value that is finally passed 
	 * as argument to the requester method, thus fulfilling 
	 * the request.
	 * 
	 * @param t			the transition to fire
	 * @param oout		outputs to the services
	 * @param oin		inputs from the services
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 */
	public void doAction(RunnableOrchestration ro, ModalTransition<String, Action, State<String>, TypedCALabel> t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {

		if (t.getLabel().isMatch())
		{
			// match: firstly interact with the requester
			oout.get(t.getLabel().getRequester()).writeObject(t.getLabel().getAction().getLabel());
			oout.get(t.getLabel().getRequester()).flush();
			oout.get(t.getLabel().getRequester()).writeObject(null);
			oout.get(t.getLabel().getRequester()).flush();

			Object rep_req = oin.get(t.getLabel().getRequester()).readObject();

			//forwarding the received requester payload to the offerer
			oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getAction().getLabel());
			oout.get(t.getLabel().getOfferer()).flush();
			oout.get(t.getLabel().getOfferer()).writeObject(rep_req);
			oout.get(t.getLabel().getOfferer()).flush();

			Object rep_off = oin.get(t.getLabel().getOfferer()).readObject();

			//forwarding the received  offerer payload to the requester
			oout.get(t.getLabel().getRequester()).writeObject(rep_off);
			oout.get(t.getLabel().getRequester()).flush();
		}
		else 
			if (t.getLabel().isOffer()){
			//only invokes the offerer and then continue
			oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getAction().getLabel());
			oout.get(t.getLabel().getOfferer()).flush();
			oout.get(t.getLabel().getOfferer()).writeObject(null);
			oout.get(t.getLabel().getOfferer()).flush();
			oin.get(t.getLabel().getOfferer()).readObject();
		}
		else throw new IllegalArgumentException("The transition is not an offer nor a request");
	}		
	
	@Override
	public String getActionType() {
		return "Centralised";
	}		
}

