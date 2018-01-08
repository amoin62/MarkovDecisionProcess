package mdp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class MDP {
	
	public Set<MDPState> states = new HashSet<MDPState>();
	
	public abstract MDPState startState();
	
	public abstract double discount();
	
	public void computeStates() {
		System.out.println("Computing MDP states.");
		Queue<MDPState> queue = new LinkedList<MDPState>();
		queue.add(this.startState());
		this.states.add(this.startState());
		int stateCounter = 1;
		while(!queue.isEmpty()) {
			MDPState state = queue.poll();
			for(MDPAction action : state.getActions()) {
				for(Object[] newStateProbReward : state.succProbReward(action)) {
					MDPState newState = (MDPState)newStateProbReward[0];
					if(!this.states.contains(newState)) {
						stateCounter += 1;
						if(stateCounter % 10000 == 0) {
							System.out.println("Number of states so far = " + stateCounter);
						}						
						queue.add(newState);
						this.states.add(newState);
					}
				}				
			}
		}
		System.out.println("Number of MDP states = " + this.states.size());
	}
}
