package mdp.movie.ratingDist;

public class RatingDistProfile1 extends AbsRatingDistProfile {

	@Override
	protected void setRanges() {
		Range range1 = new Range(0.0, false, 2.5, false);
		Range range2 = new Range(2.5, true, 3.5, true);
		Range range3 = new Range(3.5, false, 5.0, true);
		this.ranges.add(range1);
		this.ranges.add(range2);
		this.ranges.add(range3);		
	}

}
