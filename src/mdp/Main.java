package mdp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import threadedSim.ISimulatorTask;
import threadedSim.Simulator;
import dataProvider.DataProvider;
import dataProvider.MovieLensDataProvider;
import matrixFactorization.action.InnerActionTaskFactory;
import matrixFactorization.action.InnerUserActionTaskFactory;
import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.measure.AvgRatingMeasureTaskFactory;
import matrixFactorization.measure.InnerMeasureTaskFactory;
import matrixFactorization.measure.MFRecommenderMeasureTaskFactory;
import matrixFactorization.util.ScoreCount;
import mdp.movie.MovieMDP;
import mdp.movie.MovieState;
import mdp.movie.PolicyPlayback;
import mdp.movie.ratingDist.AbsRatingDistProfile;
import mdp.movie.ratingDist.RatingDistProfile1;
import mdp.movie.ratingDist.RatingDistProfile2;

public class Main {
	private final static String CONFIG_PATH = "./config.properties";
	public static String DATA_BASE_NAME= "movielens1m";
	public static String DATA_BASE_URL = "localhost";
	public static String USER_NAME = "root";
	public static String PASSWORD = "root";
	
	public static int NB_CYCLE = 80;
	public static int PROFILE_SLICE = 19;
	public static int NB_USERS = 7000;
	public static int MIN_USER_RATING = 0;
	public static int MIN_ITEM_RATING = 0;
	public static int DIMENSION = 6;
	public static double GAMMA = 0.002;
	public static double LAMBDA = 0.01;
	public static Factory fact = Factory.InnerActionTaskFactory;
	//public static Factory fact = Factory.AvgRatingMeasureTaskFactory;
	
	private static boolean REPORT_TO_FILE = true;
	private static String REPORT_FILE_PATH = "results/optimalPolicy.txt";
	public static int POP_ITEMS_OFFSET = 200;
	public static int NUM_OF_POP_ITEMS = 6;
	public static int NUM_OF_QUESTIONS = 5;
	public static double epsilon = 0.02;
	public static int RATING_DIST = 0;
	private static AbsRatingDistProfile RATING_DIST_PROFILE = new RatingDistProfile1();
	
	public enum Factory {
		InnerActionTaskFactory,
		AvgRatingMeasureTaskFactory;
		
		public String toString() {
			String res = "";
			switch (this) {
			case InnerActionTaskFactory: res = "InnerActionTaskFactory";
				break;
			case AvgRatingMeasureTaskFactory: res = "AvgRatingMeasureTaskFactory";
				break;
			default:
				System.err.println("No such factory. Verify the name of the factory.");
				System.exit(1);
				break;
			}
			return res;
		}		
	}	
	
	public static void main(String[] args) {
		init();
		
		Set<Integer> profileSlices = new HashSet<Integer>();
		for(int i=1; i<= PROFILE_SLICE; i++) {
			profileSlices.add(i);
		}
		
		DataProvider dataProvider = new MovieLensDataProvider(NB_USERS, profileSlices);
		dataProvider.fetchData(MIN_USER_RATING, MIN_ITEM_RATING);
		
		Simulator<User> simulator = new Simulator<User>();
		ISimulatorTask<User> actionTask = null;
		MFRecommenderMeasureTaskFactory<User> measureTask = null;
		switch (fact) {
		case InnerActionTaskFactory: 
			actionTask = new InnerActionTaskFactory<User>();
			measureTask = new InnerMeasureTaskFactory<User>();
			break;
		case AvgRatingMeasureTaskFactory: 
			measureTask = new AvgRatingMeasureTaskFactory<User>();
			break;
		default:
			System.err.println("The factory was not recognized.");
			System.exit(1);	
		}
		if(actionTask != null) {
			simulator.addTask(actionTask);
		}
		if(measureTask != null) {
			simulator.addTask(measureTask);	
		}
		
		for(User u : User.USERS.values()){
			simulator.addPeer(u);
		}
		System.out.println("number of users : " + simulator.getPeers().size());
		for(int i = 0; i < NB_CYCLE; i++){
			simulator.cycle();
		}
		//System.exit(0);
		
		//AbsRatingDistProfile ratingDistProfile = new RatingDistProfile1();
		List<Item> popMovies = Item.getMostPopuarItems(POP_ITEMS_OFFSET, NUM_OF_POP_ITEMS);
		MDP mdp = new MovieMDP(new HashSet<User>(User.USERS.values()), popMovies, NUM_OF_QUESTIONS, RATING_DIST_PROFILE);
		MDPAlgorithm mdpAlgo = new ValueIteration(mdp);
		long start = System.currentTimeMillis();
		mdpAlgo.solve(epsilon);
		Map<MDPState, Set<MDPAction>> optimalPolicy = mdpAlgo.computeOptimalPolicy();
		long end = System.currentTimeMillis();
		System.out.println("MDP runtime = " + (end - start) / 1000 + " seconds." );
		
		int usersSize = 0;
		for(MDPState state : mdp.states) {
			usersSize += ((MovieState) state).getUsers().size();
		}
		System.out.println("Average number of users per state = " + (double)usersSize / (double)mdp.states.size());
		
		if(REPORT_TO_FILE) {
			reportToFile(optimalPolicy, REPORT_FILE_PATH);
		}
		
		Simulator<User> playbackSimulator = new Simulator<User>();
		actionTask = new InnerUserActionTaskFactory<User>();
		playbackSimulator.addTask(actionTask);
		measureTask = new InnerMeasureTaskFactory<User>();
		playbackSimulator.addTask(measureTask);
		PolicyPlayback ppb = new PolicyPlayback(optimalPolicy, popMovies, (MovieState)mdp.startState(), RATING_DIST_PROFILE);
		double avgSimProfileSize = 0;
		for(User u : User.USERS.values()) {		
			//System.out.println("User " + u.getPeerId() + " original profile:\n" + u.getProfile().toString());
			ScoreCount<Item> simProfile = ppb.getSimulatedProfile(u);
			u.setProfile(simProfile);
			avgSimProfileSize += simProfile.size();
			u.setCoordinate(new CartesianCoordinate(u.getPeerId()));
			u.bu = 0.0f;
			//System.out.println("User " + u.getPeerId() + " simulated profile:\n" + u.getProfile().toString());
			playbackSimulator.addPeer(u);
		}
		avgSimProfileSize /= (double) User.USERS.values().size();
		System.out.println("Average size of simulated profile = " + avgSimProfileSize);
		
		System.out.println("number of users : " + simulator.getPeers().size());
		for(int i = 0; i < NB_CYCLE; i++){
			playbackSimulator.cycle();
		}
		//printing the optimal policy
		/*for(MDPState state : optimalPolicy.keySet()) {
			System.out.println(state);
			for(MDPAction optimalAction : optimalPolicy.get(state)) {
				System.out.println("\t\t" + optimalAction);
			}
		}*/
		dataProvider.clear();
	}
	
	public static void reportToFile(Map<MDPState, Set<MDPAction>> optimalPolicy, String filePath) {
		System.out.println("Exporting the optimal policy to " + filePath);
		try {
			File file = new File(filePath);
			File parentDir = file.getParentFile();
			if(!parentDir.exists()) {
				parentDir.mkdirs();
			}
			BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
			for(MDPState state : optimalPolicy.keySet()) {
				wr.write(state.toString() + "\n");
				for(MDPAction optimalAction : optimalPolicy.get(state)) {
					wr.write("\t\t" + optimalAction + "\n");
				}
			}
			wr.close();
			System.out.println("Optimal policy exported to " + filePath + " successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void init() {
		try {
			Properties prop = new Properties();
			prop.load(new FileReader(CONFIG_PATH));
			DATA_BASE_URL = prop.getProperty("db.url");
			USER_NAME = prop.getProperty("db.username");
			PASSWORD = prop.getProperty("db.password");
			DATA_BASE_NAME = prop.getProperty("db.name");
			
			NB_CYCLE = Integer.parseInt(prop.getProperty("nbCycle"));
			PROFILE_SLICE = Integer.parseInt(prop.getProperty("profileSlice"));
			NB_USERS = Integer.parseInt(prop.getProperty("nbUsers"));
			DIMENSION = Integer.parseInt(prop.getProperty("dimension"));
			
			GAMMA = Double.parseDouble(prop.getProperty("gamma"));
			LAMBDA = Double.parseDouble(prop.getProperty("lambda"));
			
			REPORT_TO_FILE = Boolean.parseBoolean(prop.getProperty("reportToFile"));
			REPORT_FILE_PATH = prop.getProperty("reportFilePath");
			
			POP_ITEMS_OFFSET = Integer.parseInt(prop.getProperty("popItemsOffset"));
			NUM_OF_POP_ITEMS = Integer.parseInt(prop.getProperty("L"));
			NUM_OF_QUESTIONS = Integer.parseInt(prop.getProperty("K"));
			RATING_DIST = Integer.parseInt(prop.getProperty("ratingDist"));
			if(RATING_DIST == 1) {
				RATING_DIST_PROFILE = new RatingDistProfile1();
			} else if (RATING_DIST == 2) {
				RATING_DIST_PROFILE = new RatingDistProfile2();
			} else {
				throw new RuntimeException("ratingDist parameter can only be 1 or 2.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
