package io.github.contractautomata.care.runnableOrchestration;

/**
 * 
Rank: 1
Initial state: [0]
Final states: [[Stop]]
Transitions: 
([InsEuro],[!coffee],[Stop])
([0],[?dollar],[InsDollar])
([InsDollar],[!tea],[Stop])
([0],[?euro],[InsEuro])
 *
 * @author Davide Basile
 *
 */
public class Bob {
	
	//@offer
	public String coffee(Integer sugar) {
		if (sugar==null) {
			System.out.println("Bob is offering a coffee that was not required");
			return "coffe without sugar";
		}
		
		System.out.println("Bob is offering a coffee with sugar "+sugar);
		return "coffee with sugar";
	}

	//@offer
		public String tea(Integer sugar) {
			return null;
		}
		
	//@request
	public String euro(Integer money) {
		if (money==null) {
			System.out.println("Bob is producing the euro request");
			return  "3 euro";			
		}
		System.out.println("Bob has received "+money+" euro");
		return null;
	}

	//@request
	public String dollar(Integer money) {
		return null;
	}
	
}
