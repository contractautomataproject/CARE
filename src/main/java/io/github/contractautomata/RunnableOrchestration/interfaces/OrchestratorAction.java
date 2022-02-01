package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import contractAutomata.automaton.transition.MSCATransition;
import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;

public interface OrchestratorAction {

	/**
	 * default implementation of an action in an orchestration
	 * 
	 * @param t			the transition to fire
	 * @param oout		outputs to the services
	 * @param oin		inputs from the services
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public default void doAction(MSCATransition t, AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {

		if (t.getLabel().isMatch())
		{
			// match: firstly interact with the requester
			oout.get(t.getLabel().getRequester()).writeObject(t.getLabel().getUnsignedAction());
			oout.get(t.getLabel().getRequester()).flush();
			oout.get(t.getLabel().getRequester()).writeObject(null);
			oout.get(t.getLabel().getRequester()).flush();

			Object rep_req = oin.get(t.getLabel().getRequester()).readObject();

			//forwarding the received requester payload to the offerer
			oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getUnsignedAction());
			oout.get(t.getLabel().getOfferer()).flush();
			oout.get(t.getLabel().getOfferer()).writeObject(rep_req);
			oout.get(t.getLabel().getOfferer()).flush();

			Object rep_off = oin.get(t.getLabel().getOfferer()).readObject();

			//forwarding the received  offerer payload to the requester
			oout.get(t.getLabel().getRequester()).writeObject(rep_off);
			oout.get(t.getLabel().getRequester()).flush();
		}
		else if (t.getLabel().isOffer()){
			//only invokes the offerer and then continue
			oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getUnsignedAction());
			oout.get(t.getLabel().getOfferer()).flush();
			oout.get(t.getLabel().getOfferer()).writeObject(null);
			oout.get(t.getLabel().getOfferer()).flush();
			oin.get(t.getLabel().getOfferer()).readObject();
		}
		else throw new IllegalArgumentException("The transition is not an offer nor a request");
	}		
}

