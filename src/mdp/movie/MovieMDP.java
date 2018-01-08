package mdp.movie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import mdp.MDP;
import mdp.MDPState;
import mdp.movie.ratingDist.AbsRatingDistProfile;

public class MovieMDP extends MDP {
	private static final double UNQUESTIONNED = -1;
	private final Set<User> users;
	private final List<Item> items;
	private final int k;
	private final AbsRatingDistProfile ratingDistProfile;
	
	public MovieMDP(Set<User> users, List<Item> items, int k, AbsRatingDistProfile ratingDistProfile) {
		this.users = new HashSet<>(users);
		this.items = new ArrayList<>(items);
		this.k = k;
		this.ratingDistProfile = ratingDistProfile;
	}
	
	@Override
	public MDPState startState() {
		double[] ratings = new double[items.size()];
		for(int i = 0; i < ratings.length; i++) {
			ratings[i] = UNQUESTIONNED;
		}
		return new MovieState(items.toArray(new Item[items.size()]), ratings, users, this.k, this.ratingDistProfile);
	}

	@Override
	public double discount() {
		return 1.0;
	}
	
	public Set<User> getUsers() {
		return users;
	}

	public List<Item> getItems() {
		return items;
	}

}
