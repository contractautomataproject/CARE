package io.github.contractautomata.RunnableOrchestration.interfaces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import contractAutomata.automaton.transition.MSCATransition;

public interface ServiceAction {
	public default void invokeMethod(Object service, Method m1, ObjectInputStream oin, ObjectOutputStream oout, MSCATransition t ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, IOException {
		Class<?> c=m1.getParameterTypes()[0];
		Object req=m1.invoke(service,c.cast(oin.readObject()));
		oout.writeObject(req);
		oout.flush();
		
		if (t.getLabel().isRequest()) {
			//if the action is a request, the payload from the offerer will be received
			m1.invoke(service,c.cast(oin.readObject()));
		}
	}
}
