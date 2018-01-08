package mdp.movie;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;
import mdp.MDPAction;
import mdp.MDPState;
import mdp.movie.ratingDist.AbsRatingDistProfile;

public class PolicyPlayback {
		private static final int NOT_FOUND = -1;
		private final Map<MDPState, Set<MDPAction>> optimalPolicy;
		private final MovieState initialState;
		private final List<Item> items;
		private final AbsRatingDistProfile ratingDistProfile;
		
		public PolicyPlayback(Map<MDPState, Set<MDPAction>> optimalPolicy, List<Item> items, MovieState initialState, AbsRatingDistProfile ratingDistProfile) {
			this.optimalPolicy = optimalPolicy;
			this.initialState = initialState;
			this.items = items;
			this.ratingDistProfile = ratingDistProfile;
		}
		
		public ScoreCount<Item> getSimulatedProfile(User user) {
			ScoreCount<Item> simProfile = new ScoreCount<Item>();
			this.getSimulatedProfile(initialState, user, simProfile);
			return simProfile;
		}
		
		private void getSimulatedProfile(MovieState state, User user, ScoreCount<Item> simulatedProfile) {
			
			Set<MDPAction> actions = this.optimalPolicy.get(state);
			if(!state.isEnd() && actions != null && actions.size() > 0) { // If the state exists in the optimal policy and the action set is not empty.
				Item movie = ((MovieAction)actions.iterator().next()).getMovie();
				int movieIndex = this.getMovieIndex(movie);
				float rating = user.getProfile().getValue(movie);
				if(rating != 0) {
					simulatedProfile.addValue(movie, rating);
				}
				double[] ratings = state.getRatings();
				double[] newRatings = Arrays.copyOf(ratings, ratings.length);
				int rangeIndex = this.ratingDistProfile.getRangeIndex(rating);
				newRatings[movieIndex] = rangeIndex + 1;
				MovieState newState = new MovieState(this.items.toArray(new Item[this.items.size()]), newRatings, null, 0, null);
				this.getSimulatedProfile(newState, user, simulatedProfile);
			}			
		}
		
		public int getMovieIndex(Item movie) {
			int index = NOT_FOUND;
			for (int i = 0; i < this.items.size(); i++) {
				if(movie.equals(this.items.get(i))) {
					index = i;
					break;
				}
			}
			return index;
		}
}
