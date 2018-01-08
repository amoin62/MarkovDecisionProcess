package mdp.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import mdp.MDPAction;
import mdp.MDPState;
import mdp.movie.ratingDist.AbsRatingDistProfile;
import mdp.movie.ratingDist.Range;

public class MovieState implements MDPState {
	private static final double UNKQUESTIONNED_RATING = -1;
	private static final double UNKNOWN_RATING = 0;
	private static final int USERS_SIZE_THRESHOLD = 100;
	private static final double RATER_THRESHOLD = 0.1;
	private final int k;
	
	private final Item[] movies;
	private final double[] ratings;
	private final Set<User> users;
	private final AbsRatingDistProfile ratingDistProfile;
	
	public MovieState(Item[] movies, double[] ratings, Set<User> users, int k, AbsRatingDistProfile ratingDistProfile) {
		this.movies = Arrays.copyOf(movies, movies.length);
		this.ratings = Arrays.copyOf(ratings, ratings.length);
		this.users = (users != null) ? new HashSet<>(users) : null;
		this.k = k;
		this.ratingDistProfile = ratingDistProfile;
	}

	@Override
	public boolean isEnd() {
		int knownCounter = 0;
		int unQuestionnedCounter = 0;
		for (double rating : this.ratings) {
			if(rating == UNKQUESTIONNED_RATING) {
				unQuestionnedCounter++;
			} else if (rating != UNKNOWN_RATING) {
				knownCounter++;
			}
		}
		
		boolean isEnd = (knownCounter == this.k || 
				unQuestionnedCounter == 0 ||
				(this.users != null && this.users.size() < USERS_SIZE_THRESHOLD)) ? true : false;
		return isEnd;
	}
	
	@Override
	public Set<MDPAction> getActions() {
		Set<MDPAction> actions = new HashSet<>();
		for(int i = 0; i < this.ratings.length; i++) {
			if(this.ratings[i] == UNKQUESTIONNED_RATING) {
				actions.add(new MovieAction(this.movies[i]));
			}
		}
		return actions;
	}
	
	public double[] getRatings() {
		return ratings;
	}

	public Set<User> getUsers() {
		return users;
	}

	public Item[] getMovies() {
		return movies;
	}
	
	@Override
	public List<Object[]> succProbReward(MDPAction action) {
		List<Object[]> results = new ArrayList<>();
		if(this.isEnd()) {
			return results;
		}
		
		Item movie = ((MovieAction)action).getMovie();
		int[] nmRangeRatings = new int[this.ratingDistProfile.ranges.size() + 1];
		for(int i = 0; i < nmRangeRatings.length; i++) {
			nmRangeRatings[i] = 0;
		}
		
		@SuppressWarnings("unchecked")
		Set<User>[] newStateUsers = new Set[nmRangeRatings.length];
		for(int i = 0; i < newStateUsers.length; i++) {
			newStateUsers[i] = new HashSet<User>();
		}
		
		for(User user : this.users) {
			double rating = user.getProfile().getValue(movie);
			if(rating == UNKNOWN_RATING) { // The user has not rated the movie
				nmRangeRatings[0] = nmRangeRatings[0] + 1;
				newStateUsers[0].add(user); // just for information. This value is never used.
			} else {
				for(int i = 0; i < this.ratingDistProfile.ranges.size(); i++) {
					Range activeRange = this.ratingDistProfile.ranges.get(i);
					if(activeRange.contains(rating)) {
						nmRangeRatings[i + 1] = nmRangeRatings[i + 1]  + 1;
						newStateUsers[i + 1].add(user);
					}
				}
			}
		}
		
		int movieIndex = 0;
		for(int i = 0; i < this.movies.length; i++) {
			if(this.movies[i].equals(movie)) {
				movieIndex = i;
				break;
			}
		}
		
		double reward = this.getReward(nmRangeRatings);
		for(int i = 0; i < nmRangeRatings.length; i++) {
			double[] newRatings = Arrays.copyOf(this.ratings, this.ratings.length);
			newRatings[movieIndex] = i;
			Set<User> newUsers = (i == 0) ? this.users : newStateUsers[i];
			MovieState newState = new MovieState(this.movies, newRatings, newUsers, this.k, this.ratingDistProfile);
			
			Object[] result = new Object[3];
			result[0] = newState;
			result[1] = (double) nmRangeRatings[i] / this.users.size(); //Prob
			result[2] = (i == 0) ? 0 : reward;
			results.add(result);
			
			/*System.out.println("number of users in the state = " + this.users.size());
			System.out.println("Action = " + movie.toString());
			System.out.println("state = " + result[0]);
			System.out.println("probability = " + result[1]);
			System.out.println("reward = " + result[2]);*/
		}
		return results;
	}

	@Override
	public double getReward(MDPAction action) {
		double reward = 0;
		Item movie = ((MovieAction)action).getMovie();
		int users = 0;
		int lovers = 0;
		int haters = 0;
		for(User user : this.users) {
			double rating = user.getProfile().getValue(movie);
			double avgUserRating = user.getMeanRating();
			if(rating != UNKNOWN_RATING && rating - avgUserRating > 0) {
				lovers++;
				users++;
			} else if (rating != UNKNOWN_RATING && rating - avgUserRating <= 0) {
				haters++;
				users++;
			}
		}
		if(users >= RATER_THRESHOLD * this.users.size()) {
			double pHater = (double)haters/users;
			double pLover = (double)lovers/users;
			reward = - (pHater * Math.log(pHater)) - (pLover * Math.log(pLover));
		}
		return reward;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ratings);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MovieState other = (MovieState) obj;
		if (!Arrays.equals(ratings, other.ratings))
			return false;
		return true;
	}

	public double getReward2(MDPAction action) {
		double reward = 0;
		Item movie = ((MovieAction)action).getMovie();
		int users = 0;
		int lovers = 0;
		int neutrals = 0;
		int haters = 0;
		for(User user : this.users) {
			double rating = user.getProfile().getValue(movie);
			if(rating > 0 && rating < 2.5) {
				haters++;
				users++;
			} else if (rating >= 2.5 && rating <= 3.5) {
				neutrals++;
				users++;
			} else if (rating > 3.5) {
				lovers++;
				users++;
			}
		}
		if(users >= RATER_THRESHOLD * this.users.size()) {
			double pHater = (double)haters/users;
			double pLover = (double)lovers/users;
			double pNeutral = (double)neutrals/users;
			reward = - (pHater * Math.log1p(pHater)) - (pLover * Math.log1p(pLover)) - (pNeutral * Math.log1p(pNeutral));
		}
		return reward;
	}
	
	public double getReward(int[] nmRangeRatings) {
		double reward = 0.0;
		int totalRatings = 0;
		for(int i = 1; i < nmRangeRatings.length; i++) {
			totalRatings += nmRangeRatings[i];
		}
		
		if(totalRatings != 0) {
			for(int i = 1; i < nmRangeRatings.length; i++) {
				double prob = (double) nmRangeRatings[i] / totalRatings;
				if(prob != 0) {
					reward -= (prob * Math.log(prob));
				}
			}
		}
		
		return reward;
	}
	
	@Override
	public String toString() {
		String string = "[";
		for(int i = 0; i < this.ratings.length; i++) {
			string += this.ratings[i] + ",";
		}
		string = (string.endsWith(",")) ? string.substring(0, string.length() - 1) : string; // remove ","
		string += "]";
		return string;
	}
	
}
