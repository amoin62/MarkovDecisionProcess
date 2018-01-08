package matrixFactorization.util;

import java.util.Comparator;

import matrixFactorization.data.User;

public class CoorComparator<U extends User> implements Comparator<U> {
	private U ref;
	public CoorComparator(U ref) {
		super();
		this.ref = ref;
	}
	@Override
	public int compare(U u1, U u2) {
		double dist1 = u1.getCoordinate().distance(ref.getCoordinate());
		double dist2 = u2.getCoordinate().distance(ref.getCoordinate());
		if (dist1 > dist2) {
			return 1;
		} else if (dist1 < dist2) {
			return -1;
		} else {
			return 0;
		} 
	}

}
