package matrixFactorization.measure;

import java.util.concurrent.Semaphore;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import threadedSim.measure.AMeasureTask;

public abstract class RecommenderMeasureTask<U extends User> extends AMeasureTask<U> {

	public RecommenderMeasureTask(U peer, Semaphore sem) {
		super(peer, sem);
	}

	public abstract double predict(U user, Item item);

}
