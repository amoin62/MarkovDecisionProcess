package matrixFactorization.action;

import java.util.concurrent.Semaphore;

import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;
import threadedSim.action.AActionTaskFactory;

public class InnerActionTaskFactory<U extends User> extends AActionTaskFactory<U>{

	@Override
	public Runnable createTask(U peer) {
		return new InnerActionTask<U>(peer, this.getSem());
	}
	
	public static class InnerActionTask<U extends User> extends MFRecommenderActionTask<U>{
		public static double mu = 3.6043;
		
		public InnerActionTask(U peer, Semaphore sem) {
			super(peer, sem);
		}
		
		@Override
		protected void doAction() {
			ScoreCount<Item> userProfile = this.peer.getProfile();
			CartesianCoordinate userCoor = this.peer.getCoordinate();
			for(Item i : userProfile.getItems()){
					Item item = i.getItem();
					double prediction = this.predict(this.peer,item);
					double error = userProfile.getValue(item) - prediction;
									
					float[] userPos = userCoor.getPosition();
					float[] itemPos = item.getCoordinate().getPosition();
					
					float[] userRes = new float[CartesianCoordinate.nbDimension];
					float[] itemRes = new float[CartesianCoordinate.nbDimension];
					
					for(int j=0; j<CartesianCoordinate.nbDimension;j++){
						userRes[j] = (float) (userPos[j] + gamma*(error*itemPos[j] - (lambda*userPos[j])));
						itemRes[j] = (float) (itemPos[j]+ gamma*(error*userPos[j] - (lambda*itemPos[j])));
					}
					
					this.peer.getCoordinate().setPosition(userRes);
					item.getCoordinate().setPosition(itemRes);
					
					float currentbu = this.peer.bu;
					float newbu = (float) (currentbu + gamma*(error - lambda * currentbu));
					this.peer.bu = newbu;
					float currentbi = item.bi;
					float newbi = (float) (currentbi + gamma*(error - lambda * currentbi));
					item.bi = newbi;
										
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
