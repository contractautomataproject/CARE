package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public interface ServiceChoice extends Choice<String> {
	public default void select(String action, ObjectOutputStream oout) throws IOException {
		String reply = choice(Arrays.asList(action));
		oout.writeObject(reply);
		oout.flush();
	}
}
