package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface ServiceChoice {
	
	public void select(ObjectOutputStream oout, ObjectInputStream oin) throws IOException;
}
