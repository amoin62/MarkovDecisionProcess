package mdp.movie.ratingDist;

public class Range {
	public final double start;
	public final boolean isStartIncluded;
	public final double end;
	public final boolean isEndIncluded;
	
	public Range(double start, boolean isStartIncluded, double end, boolean isEndIncluded) {
		this.start = start;
		this.isStartIncluded = isStartIncluded;
		this.end = end;
		this.isEndIncluded = isEndIncluded;
	}
	
	public boolean contains(double value) {
		boolean contains = false;
		if(((isStartIncluded && value >= start) || (!isStartIncluded && value > start))
				&& ((isEndIncluded && value <= end) || (!isEndIncluded && value < start))) {
					contains = true;
				}
		return contains;
	}
}
