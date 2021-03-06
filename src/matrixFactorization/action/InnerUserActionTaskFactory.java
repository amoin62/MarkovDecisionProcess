package matrixFactorization.action;

import java.util.concurrent.Semaphore;

import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;
import threadedSim.action.AActionTaskFactory;

public class InnerUserActionTaskFactory<U extends User> extends AActionTaskFactory<U> {
	
	@Override
	public Runnable createTask(U peer) {
		return new InnerUserActionTask<U>(peer, this.getSem());
	}
	
	public static class InnerUserActionTask<U extends User> extends MFRecommenderActionTask<U>{
		public static double mu = 3.6043;
		
		public InnerUserActionTask(U peer, Semaphore sem) {
			super(peer, sem);
		}
		
		@Override
		protected void doAction() {
			ScoreCount<Item> userProfile = this.peer.getProfile();
			CartesianCoordinate userCoor = this.peer.getCoordinate();
			for(Item i : userProfile.getItems()) {
					Item item = i.getItem();
					double prediction = this.predict(this.peer,item);
					double error = userProfile.getValue(item) - prediction;
									
					float[] userPos = userCoor.getPosition();
					float[] itemPos = item.getCoordinate().getPosition();
					
					float[] userRes = new float[CartesianCoordinate.nbDimension];
					
					for(int j=0; j<CartesianCoordinate.nbDimension;j++){
						userRes[j] = (float) (userPos[j] + gamma*(error*itemPos[j]-(lambda*userPos[j])));
					}
					
					this.peer.getCoordinate().setPosition(userRes);
					
					float currentbu = this.peer.bu;
					float newbu = (float) (currentbu + gamma*(error - lambda * currentbu));
					this.peer.bu = newbu;
										
					item.releaseItem();
			}
		}

		@Override
		public double predict(U user, Item item) {
			//double res = user.getCoordinate().innerProduct(item.getCoordinate().getPosition());
			double res = mu + user.bu + item.bi + user.getCoordinate().innerProduct(item.getCoordinate().getPosition());
			return res;
		}

		@Override
		public double delta(U user, Item item, double prediction) {
			return 1.0;
		}
		
	}
}
