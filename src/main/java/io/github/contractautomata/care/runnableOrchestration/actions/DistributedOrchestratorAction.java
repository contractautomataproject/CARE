package io.github.contractautomata.care.runnableOrchestration.actions;

import io.github.contractautomata.care.runnableOrchestration.AutoCloseableList;
import io.github.contractautomata.care.runnableOrchestration.RunnableOrchestration;
import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.action.Action;
import io.github.contractautomata.catlib.automaton.state.State;
import io.github.contractautomata.catlib.automaton.transition.ModalTransition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DistributedOrchestratorAction  implements OrchestratorAction {
	
	/**
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
	 * 
	 */
	public void doAction(RunnableOrchestration ro, ModalTransition<String, Action, State<String>, ? extends CALabel> t, AutoCloseableList<ObjectOutputStream> oout,
						 AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException {

		//send action to the offerer
		oout.get(t.getLabel().getOfferer()).writeObject(t.getLabel().getAction().getLabel());
		oout.get(t.getLabel().getOfferer()).flush();

		if (t.getLabel().isMatch())
		{
			//send action to requester
			oout.get(t.getLabel().getRequester()).writeObject(t.getLabel().getAction().getLabel());
			oout.get(t.getLabel().getRequester()).flush();
			
			//send type to the offerer
			oout.get(t.getLabel().getOfferer()).writeObject("match");
			oout.get(t.getLabel().getOfferer()).flush();
			
			//receive the port where the offerer is waiting for the requester
			int port = (Integer) oin.get(t.getLabel().getOfferer()).readObject();
			
			//send address and port of the offerer to the requester
			oout.get(t.getLabel().getRequester()).writeObject(ro.getAddresses().get(t.getLabel().getOfferer()));
			oout.get(t.getLabel().getRequester()).flush();
			oout.get(t.getLabel().getRequester()).writeObject(port);
			oout.get(t.getLabel().getRequester()).flush();
			
			//wait ack from the requester
			oin.get(t.getLabel().getRequester()).readObject();

		}
		else if (t.getLabel().isOffer()){
			//send type to the offerer
			oout.get(t.getLabel().getOfferer()).writeObject("offer");
			oout.get(t.getLabel().getOfferer()).flush();

			//only invokes the offerer without a request and then continue
			oout.get(t.getLabel().getOfferer()).writeObject(null);
			oout.get(t.getLabel().getOfferer()).flush();
			oin.get(t.getLabel().getOfferer()).readObject();
		}
		else throw new IllegalArgumentException("The transition is not an offer nor a request");
	}

	@Override
	public String getActionType() {
		return "Distributed";
	}		
}

