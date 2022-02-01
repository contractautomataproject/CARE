package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;
import io.github.contractautomata.RunnableOrchestration.RunnableOrchestration;

public interface OrchestratorChoice extends Choice<String> {
	
	public default String select(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin, List<Integer> port) throws IOException, ClassNotFoundException {
		System.out.println("Orchestrator sending choice message");
		for (ObjectOutputStream o : oout) {
			o.writeObject(RunnableOrchestration.choice_msg);
			o.flush();
		}
		List<String> choices = new ArrayList<>(); 
		for (int i=0;i<port.size();i++)
			choices.add((String) oin.get(i).readObject());
		return choice(choices);
	}
}
