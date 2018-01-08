package matrixFactorization.measure;

import java.util.concurrent.Semaphore;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;

public class AvgRatingMeasureTaskFactory<U extends User> extends MFRecommenderMeasureTaskFactory<U> {
	private static final double AVG_MOVIELENS1M_RATING = 3.58;

	@Override
	public Runnable createTask(U peer) {
		return new AvgRatingTask<U>(peer,this.getSem());
	}
	
	public static class AvgRatingTask<U extends User> extends RecommenderMeasureTask<U> {
				
		public AvgRatingTask(U peer, Semaphore sem) {
			super(peer, sem);
		}

		@Override
		public double predict(U user, Item item) {
			double res = item.getMeanRating();
			if(Double.isNaN(res)) {
				res = AVG_MOVIELENS1M_RATING;
			}
			/*if(item.getNum() == 1291)
				System.out.println("Movie = " + item.getNum() + " prediction = " + res);*/
			return res;
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
