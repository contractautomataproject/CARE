package io.github.contractautomata.label;

import io.github.contractautomataproject.catlib.automaton.label.CALabel;
import io.github.contractautomataproject.catlib.automaton.label.Label;
import io.github.contractautomataproject.catlib.automaton.label.action.Action;

/**
 * a CALabel annotated with parameter type and return value type for matching types
 * 
 * @author Davide Basile
 *
 */
public class TypedCALabel extends CALabel{
	private final Class<?> param;
	private final Class<?> value;

	public TypedCALabel(CALabel label, Class<?> param, Class<?> value) {
		super(label.getLabel());
		this.param=param;
		this.value=value;
	}
	

	@Override
	public boolean match(Label<Action> label) {
		if (label instanceof TypedCALabel)
		{
			TypedCALabel typedlabel = (TypedCALabel) label;
			return super.match(label) && 
					typedlabel.param.isAssignableFrom(this.value) &&
					this.param.isAssignableFrom(typedlabel.value);
		}
		else
			throw new IllegalArgumentException();
	}


}
