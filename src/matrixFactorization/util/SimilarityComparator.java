package matrixFactorization.util;

import java.util.Comparator;

import matrixFactorization.data.User;

public class SimilarityComparator<U extends User> implements Comparator<U> {
	private U ref;
	
	public SimilarityComparator(U ref) {
		super();
		this.ref = ref;
	}

	@Override
	public int compare(U u1, U u2) {
		double sim1 = u1.getProfile().getCosUtility(ref.getProfile());
		double sim2 = u2.getProfile().getCosUtility(ref.getProfile());
		if (sim1 > sim2) {
			return 1;
		} else if (sim1 < sim2) {
			return -1;
		} else {
			return 0;
		} 
	}

}
