package io.github.contractautomata.label;

import io.github.davidebasile.contractautomata.automaton.label.CALabel;
import io.github.davidebasile.contractautomata.automaton.label.Label;

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
		super(label,label.getRank(),0);
		this.param=param;
		this.value=value;
	}
	

	@Override
	public boolean match(Label<String> label) {
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
