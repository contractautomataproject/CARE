package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.contractautomata.RunnableOrchestration.AutoCloseableList;

public interface OrchestratorChoice {
	
	/**
	 * default implementation of a branch choice made by an orchestrator
	 * 
	 * @param oout	output to the services
	 * @param oin	input from the services
	 * @return	the selected action to fire
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public String select(AutoCloseableList<ObjectOutputStream> oout, AutoCloseableList<ObjectInputStream> oin) throws IOException, ClassNotFoundException;
}
