package mdp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MDPAlgorithm {
	
	public Map<MDPState, Double> optimalValues = new HashMap<>();
	public final MDP mdp;
	
	public MDPAlgorithm(MDP mdp) {
		this.mdp = mdp;
	}
	
	public double computeQ(MDPState state, MDPAction action) {
		//Double stateValue = this.optimalValues.get(state);
		double Q = 0.0;
		double discount = this.mdp.discount();
		for (Object[] newStateProbReward : state.succProbReward(action)) {
			MDPState newState = (MDPState)newStateProbReward[0];
			double prob = (double)newStateProbReward[1];
			double reward = (double)newStateProbReward[2];
			
			/*if(!newState.isEnd()) {
				System.out.println("New state " + newState + " is not an end state.");
			}*/
			/*String newStateValueString = (this.optimalValues.get(newState) == null) ? "null" : this.optimalValues.get(newState).toString();
			System.out.println(newStateValueString);*/
			
			double newStateValue = this.optimalValues.getOrDefault(newState, 0.0);
			/*if(newStateValue != 0) {
				System.out.println("new state = " + newState + "\tnewStateValue = " + newStateValue);			
			}*/
			Q += prob * (reward + discount * newStateValue);
		}
		return Q;
	}
	
	public Map<MDPState, Set<MDPAction>> computeOptimalPolicy() {
		System.out.println("Computing the optimal policy");
		Map<MDPState, Set<MDPAction>> optimalPolicy = new HashMap<>();
		for(MDPState state : this.mdp.states) {
			double maxValue = -1.0;
			for(MDPAction action : state.getActions()) {
				double value = this.computeQ(state, action);
				//System.out.println("Q Value = " + value);
				if(value >= maxValue) {
					Set<MDPAction> optimalActions = optimalPolicy.get(state);
					if(optimalActions == null) {
						optimalActions = new HashSet<MDPAction>();
						optimalPolicy.put(state, optimalActions);
					}
					if(value > maxValue) {
						optimalActions.clear();
					}
					optimalActions.add(action);
					maxValue = value;
				}
			}
		}
		System.out.println("Optimal policy computed.");
		return optimalPolicy;
	}
	
	public abstract void solve(double epsilon);
}
