package mdp.movie;

import matrixFactorization.data.Item;
import mdp.MDPAction;

public class MovieAction implements MDPAction {
	private final Item movie;
	
	public MovieAction(Item movie) {
		this.movie = movie;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((movie == null) ? 0 : movie.hashCode());
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
		MovieAction other = (MovieAction) obj;
		if (movie == null) {
			if (other.movie != null)
				return false;
		} else if (!movie.equals(other.movie))
			return false;
		return true;
	}

	public Item getMovie() {
		return movie;
	}
	
	@Override
	public String toString() {
		return this.movie.getName();
	}
}
