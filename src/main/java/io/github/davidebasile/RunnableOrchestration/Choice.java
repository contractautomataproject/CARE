package io.github.davidebasile.RunnableOrchestration;

import java.util.List;

public interface Choice<T> {
	/**
	 * this method abstracts from choices performed by a service (e.g., which branch or 
	 * termination) when enquired by the orchestrator.
	 * 
	 * @param args the arguments
	 * @return the choice made
	 */
	public abstract T choice(List<String> args);
}
