package mdp;

import java.util.List;
import java.util.Set;

public interface MDPState {
	
	public boolean isEnd();
	
	public Set<MDPAction> getActions();
	
	public double getReward(MDPAction action);
	
	/**
	 * Computes the transition probability from the current state to a new state and the reward associated with it.
	 * @param current state of the MDP.
	 * @param action action to be taken from the current state of the MDP.
	 * @return Returns an array of (newState, transProb, reward).
	 */
	public abstract List<Object[]> succProbReward(MDPAction action);

}
