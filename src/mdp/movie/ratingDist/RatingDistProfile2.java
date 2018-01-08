package mdp.movie.ratingDist;

public class RatingDistProfile2 extends AbsRatingDistProfile {
	@Override
	protected void setRanges() {
		Range range1 = new Range(0.0, false, 3.0, true);
		Range range2 = new Range(3.0, false, 5.0, true);
		this.ranges.add(range1);
		this.ranges.add(range2);	
	}
}
