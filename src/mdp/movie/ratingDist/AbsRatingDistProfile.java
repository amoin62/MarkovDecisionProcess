package mdp.movie.ratingDist;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsRatingDistProfile {
	public static final int RANGE_NOT_FOUND = -1;
	public List<Range> ranges = new ArrayList<>();
	
	public AbsRatingDistProfile() {
		this.setRanges();
	}
	
	public int getRangeIndex(double value) {
		int index = RANGE_NOT_FOUND;
		for(int i = 0; i < this.ranges.size(); i++) {
			if(this.ranges.get(i).contains(value)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	protected abstract void setRanges();
}
