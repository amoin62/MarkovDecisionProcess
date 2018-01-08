package matrixFactorization.action;

import java.util.concurrent.Semaphore;

import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;
import mdp.Main;
import threadedSim.action.ALocalActionTask;

public abstract class MFRecommenderActionTask<U extends User> extends ALocalActionTask<U>{
	protected static double gamma = Main.GAMMA;
	protected static double lambda = Main.LAMBDA;
	public MFRecommenderActionTask(U peer, Semaphore sem) {
		super(peer, sem);
	}

	@Override
	protected void doAction() {
		ScoreCount<Item> userProfile = this.peer.getProfile();
		CartesianCoordinate userCoor = this.peer.getCoordinate();
		for(Item i : userProfile.getItems()){
			synchronized(i){
				Item item = i.getItem();
				double prediction = this.predict(this.peer,item);
				double error = userProfile.getValue(item) - prediction;
				double del = this.delta(this.peer, item, prediction);
				
				float[] userPos = userCoor.getPosition();
				float[] itemPos = item.getCoordinate().getPosition();
				
				float[] userRes = new float[CartesianCoordinate.nbDimension];
				float[] itemRes = new float[CartesianCoordinate.nbDimension];
				
				for(int j=0; j<CartesianCoordinate.nbDimension;j++){
					userRes[j] = (float) (userPos[j] + gamma*(error*del*(itemPos[j]-userPos[j]) - lambda*userPos[j]));
					itemRes[j] = (float) (itemPos[j]+ gamma*(error*del*(userPos[j]-itemPos[j]) - lambda*itemPos[j]));
				}
				
				this.peer.getCoordinate().setPosition(userRes);
				item.getCoordinate().setPosition(itemRes);
				item.releaseItem();
			}
		}
	}
	
	public abstract double predict(U user, Item item);
	//we pass the prediction as a parameter to avoid double calculation of predict(U user, Item item).
	public abstract double delta(U user, Item item, double prediction);

}
