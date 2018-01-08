package mdp;

import java.util.HashMap;
import java.util.Map;

public class ValueIteration extends MDPAlgorithm {

	public ValueIteration(MDP mdp) {
		super(mdp);
	}

	@Override
	public void solve(double epsilon) {
		this.mdp.computeStates();
		int numIters = 0;
		while(true) {
			Map<MDPState, Double> valueMap = new HashMap<MDPState, Double>();
			for (MDPState state : this.mdp.states) {
				double maxQ = 0;
				for(MDPAction action : state.getActions()) {
					double Q = this.computeQ(state, action);
					maxQ = Math.max(maxQ, Q);
				}
				valueMap.put(state, maxQ);
			}
			numIters++;	
			double diff = 0.0;
			for(MDPState state : valueMap.keySet()) {
				double newValue = valueMap.get(state);
				double oldValue = this.optimalValues.getOrDefault(state, 0.0);
				diff = Math.max(diff, Math.abs(newValue - oldValue));
			}
			System.out.println("Iteration " + numIters + " diff = " + diff);
			if(diff <= epsilon) {
				break;
			}
			this.optimalValues = valueMap;
		}
	}

}
