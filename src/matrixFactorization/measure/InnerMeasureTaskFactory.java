package matrixFactorization.measure;

import java.util.concurrent.Semaphore;

import matrixFactorization.action.InnerActionTaskFactory.InnerActionTask;
import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;

public class InnerMeasureTaskFactory<U extends User> extends MFRecommenderMeasureTaskFactory<U> {

	@Override
	public Runnable createTask(U peer) {
		return new InnerMeasureTask<U>(peer,this.getSem());
	}
	
	public static class InnerMeasureTask<U extends User> extends RecommenderMeasureTask<U> {
		public static double mu = InnerActionTask.mu;
		
		public InnerMeasureTask(U peer, Semaphore sem) {
			super(peer, sem);
		}

		@Override
		public double predict(U user, Item item) {
			double inner = user.getCoordinate().innerProduct(item.getCoordinate().getPosition());
			double res = mu + user.bu + item.bi + inner;
			return  res;
		}

		@Override
		protected void doMeasure() {
			ScoreCount<Item> testProfile = this.peer.getTestProfile();
			for(Item item : testProfile.getItems()){
				double error = testProfile.getValue(item) - this.predict(this.peer, item);
				synchronized (MFRecommenderMeasureTaskFactory.class) {
					nbPredicted++;
					this.peer.reportMeanAError(Math.abs(error));
					mAError += Math.abs(error);
					this.peer.reportStd(error);
					stdDev += error*error;
				}	
			}
		}
		
	} 
	
	@Override
	public String printPar() {
		return "";
	}

}
