package matrixFactorization.measure;

import matrixFactorization.data.User;
import threadedSim.Simulator;
import threadedSim.measure.AMeasureTaskFactory;

public abstract class MFRecommenderMeasureTaskFactory<U extends User> extends AMeasureTaskFactory<U> {
	protected static volatile int nbPredicted=0;
	protected static volatile double stdDev=0;
	protected static volatile double mAError=0;
	@Override
	public String getResult() {
		mAError = mAError/nbPredicted;
		stdDev = Math.sqrt(stdDev/nbPredicted);
		String res = "Number of Predicted Ratings		Standard Deviation		Mean Absolute Error \n";
		res += nbPredicted + "					"+ stdDev +"		"+mAError;	
		MFRecommenderMeasureTaskFactory.mAError=0;
		MFRecommenderMeasureTaskFactory.stdDev=0;
		MFRecommenderMeasureTaskFactory.nbPredicted=0;
		return res;
	}
	
	@Override
	public final Object executeTask(Simulator<U> sim) {
		for (U u : sim.getPeers()) {
			u.getMeanAError();
			u.getStd();
			this.addTask(u);
		}
		this.waitFinished();
		return this.getResult();
	}

	public abstract String printPar();

}
