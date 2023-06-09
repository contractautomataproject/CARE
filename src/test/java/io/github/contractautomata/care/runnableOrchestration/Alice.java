package io.github.contractautomata.care.runnableOrchestration;

/**
 * 
Rank: 1
Initial state: [0]
Final states: [[Drink]]
Transitions: 
([InsDollar],[?tea],[Drink])
([InsEuro],[?coffee],[Drink])
([0],[!dollar],[InsDollar])
([0],[!euro],[InsEuro])
 *
 * @author Davide Basile
 *
 */
public class Alice {

	//@request
	public Integer coffee(String arg) {
		if (arg!=null) {
			System.out.println("Alice has received the payload "+arg);
			return null;
		}
		else 
		{
			System.out.println("Alice is requiring a coffee");
			return  3;
		}
	}

	//@request
	public Integer tea(String arg) {
		return null;
	}

	//@offer
	public Integer euro(String arg) {
		if (arg!=null) {
			System.out.println("Alice has received the request "+arg+" and will perform the offer");
			return  3;
		}
		else
		{
			System.out.println("Skipping offer");
			return null;
		}

	}

	//@offer
	public Integer dollar(String arg) {
		return null;
	}
}
