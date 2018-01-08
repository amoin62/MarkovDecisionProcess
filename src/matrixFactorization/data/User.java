package matrixFactorization.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import threadedSim.peer.Peer;
import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.coordinate.IProvidesPosition;
import matrixFactorization.util.ScoreCount;


public class User extends Peer implements IProvidesPosition<CartesianCoordinate> {
	private static final float UNSET = -1f;
	public static Map<Integer, User> USERS = new HashMap<Integer,User>();
	private CartesianCoordinate coor;
	private CartesianCoordinate[] featureWeights;
	private volatile float meanSqrDist;
	public  volatile float meanAError;
	public volatile float std;
	private boolean busy;	
	private volatile int nbStdPredictions;
	private volatile int nbMAEPredictions;
	private volatile float alphaLinear;
	private volatile float betaLinear;
	public volatile float bu;
	public volatile float meanRating = UNSET;
	private ScoreCount<Item> profile;
	private ScoreCount<Item> testProfile;
	
	private List<User> coorNeighbors;
	private List<User> similarNeighbors;
	
	public static User getUser(int peerID) {
		User user = USERS.get(peerID);
		if(user != null) {
			return user;
		} else {
			user = new User(peerID);
		}
		return user;
	}
	
	private User(int peerID) {
		super(peerID);
		this.busy=false;
		this.nbStdPredictions = 0;
		this.nbMAEPredictions = 0;
		this.meanAError=0.0f;
		this.std=0.0f;
		this.meanSqrDist=0.0f;
		this.alphaLinear = 2.5f;
		this.betaLinear = 0.2f;
			
		this.coor = new CartesianCoordinate(peerID);
		this.featureWeights = new CartesianCoordinate[CartesianCoordinate.nbDimension];
		for(int i = 0; i < CartesianCoordinate.nbDimension; i++){
			featureWeights[i] = new CartesianCoordinate();
		}
		this.profile = new ScoreCount<Item>();
		this.testProfile = new ScoreCount<Item>();
		this.bu=0.0f;
		
		/*this.coorNeighbors = new ArrayList<U>();
		this.similarNeighbors = new ArrayList<U>();*/
		this.coorNeighbors = null;
		this.similarNeighbors = null;
		USERS.put(peerID, this);
	}
	
	public ScoreCount<Item> getTestProfile() {
		return testProfile;
	}

	public final boolean isBusy() {
		return busy;
	}

	public final void setBusy(boolean busy) {
		this.busy = busy;
	}

	public final float getMeanAError() {
		if(nbMAEPredictions > 0){
			float res = meanAError/(float)nbMAEPredictions;
			this.nbMAEPredictions=0;
			this.meanAError =0;
			return res;
		}else{
			return (float) 0.0;
		}
		
	}

	public final void reportMeanAError(double error) {
		this.nbMAEPredictions++;
		this.meanAError += Math.abs(error);
	}

	public final float getStd() {
		if(nbStdPredictions > 0) {
			float res = (float) Math.sqrt(std/(float)nbStdPredictions);
			this.nbStdPredictions = 0;
			this.std=0;
			return res;
		}
		else {
			return (float) 0.0;
		}
	}

	public final void reportStd(double error) {
		this.nbStdPredictions++;
		this.std += error*error;
	}

	public final double getMeanSqrDistance() {
		return meanSqrDist;
	}

	public final void setMeanSqrDistance(float meanDistance) {
		this.meanSqrDist = meanDistance;
	}

	@Override
	public CartesianCoordinate getCoordinate() {
		return this.coor;
	}

	@Override
	public void setCoordinate(CartesianCoordinate c) {
		this.coor = c;
	}
	
	public ScoreCount<Item> getProfile() {
		return profile;
	}
	
	public void setProfile(ScoreCount<Item> sc) {
		this.profile = sc;
	}
	
	public void setTestProfile(ScoreCount<Item> testSc) {
		this.testProfile = testSc;
	}
	
	public float getAlphaLinear() {
		return alphaLinear;
	}

	public void setAlphaLinear(float alphaLinear) {
		this.alphaLinear = alphaLinear;
	}
	
	public float getBetaLinear() {
		return betaLinear;
	}

	public void setBetaLinear(float betaLinear) {
		this.betaLinear = betaLinear;
	}
	
	public List<User> getCoorNeighbors() {
		return coorNeighbors;
	}

	public List<User> getSimilarNeighbors() {
		return similarNeighbors;
	}
	
	public synchronized float getMeanRating() {
		if(this.meanRating == UNSET) {
			this.meanRating = (float) this.profile.getAverage();
		}
		return meanRating;
	}
	
	public double getMeanSimOfCoorNeighbors() {
		double res = 0.0;
		for(User u : this.coorNeighbors) {
			res += this.getProfile().getSimilarity(u.getProfile());
		}
		if(this.coorNeighbors.size() != 0) {
			return res / (double)(this.coorNeighbors.size());
		}else{
			return 0.0;
		}
		
	}
	
	public double getMeanSimOfSimNeighbors() {
		double res = 0.0;
		for(User u : this.similarNeighbors) {
			res += this.getProfile().getSimilarity(u.getProfile());
		}
		if(this.similarNeighbors.size() != 0) {
			return res / (double)(this.similarNeighbors.size());
		} else {
			return 0.0;
		}
	}
	
	public double getMeanCosSimOfCoorNeighbors() {
		double res = 0.0;
		for(User u : this.coorNeighbors) {
			res += this.getProfile().getCosSimilarity(u.getProfile());
		}
		if(this.coorNeighbors.size() != 0) {
			return res / (double)(this.coorNeighbors.size());
		} else {
			return 0.0;
		}
	}
	
	public double getMeanCosSimOfSimNeighbors() {
		double res = 0.0;
		for(User u : this.similarNeighbors) {
			res += this.getProfile().getCosSimilarity(u.getProfile());
		}
		if(this.similarNeighbors.size() != 0) {
			return res / (double)(this.similarNeighbors.size());
		}else{
			return 0.0;
		}
	}
	
	public double getMeanUtilityOfCoorNeighbors() {
		double res = 0.0;
		for(User u : this.coorNeighbors) {
			res += this.getProfile().getUtility(u.getProfile());
		}
		if(this.coorNeighbors.size() != 0) {
			return res / (double)(this.coorNeighbors.size());
		} else {
			return 0.0;
		}
	}
	
	public double getMeanUtilityOfSimNeighbors() {
		double res = 0.0;
		for(User u : this.similarNeighbors) {
			res += this.getProfile().getUtility(u.getProfile());
		}
		if(this.similarNeighbors.size() != 0) {
			return res / (double)(this.similarNeighbors.size());
		} else {
			return 0.0;
		}
	}
	
	public double getMeanCosUtilityOfCoorNeighbors() {
		double res = 0.0;
		for(User u : this.coorNeighbors) {
			res += this.getProfile().getCosUtility(u.getProfile());
		}
		if(this.coorNeighbors.size() != 0) {
			return res / (double)(this.coorNeighbors.size());
		} else {
			return 0.0;
		}
	}
	
	public double getMeanCosUtilityOfSimNeighbors() {
		double res = 0.0;
		for(User u : this.similarNeighbors) {
			res += this.getProfile().getCosUtility(u.getProfile());
		}
		if(this.similarNeighbors.size() != 0) {
			return res / (double)(this.similarNeighbors.size());
		} else {
			return 0.0;
		}
	}
	protected synchronized User getUser() {
			while(this.isBusy()) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.setBusy(true);
			return this;
	}
	
	protected synchronized void releaseUser() {
			this.setBusy(false);
			this.notify();
	}

	public CartesianCoordinate[] getFeatureWeights() {
		return featureWeights;
	}

	public void setFeatureWeights(CartesianCoordinate[] featureWeights) {
		this.featureWeights = featureWeights;
	}
	
}
