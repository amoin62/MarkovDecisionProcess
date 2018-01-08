package dataProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import matrixFactorization.data.Item;
import matrixFactorization.data.User;
import matrixFactorization.util.ScoreCount;

public class MovieLensDataProvider extends DataProvider{
	private Set<Integer> trainingSlices;
	 	
	public MovieLensDataProvider(int nbUsers, Set<Integer> training){
		super();
		this.trainingSlices = training;
	}
	
	public void fetchProfiles(int minUserRating, int minItemRating){
		try {
						
			System.out.println("Starting to fetch movies with more than " + minItemRating + " ratings.");
			int counter = 0;
			ResultSet movieRS = this.dbConnect.createStatement().executeQuery("SELECT movie, name FROM movies WHERE nb_ratings > " + minItemRating);
			//This map includes the movie_id and the number of ratings of the corresponding movie.
			while (movieRS.next()){
				int movieID = movieRS.getInt(1);
				String name = movieRS.getString(2);
				Item movie = Item.getItem(movieID);
				movie.setName(name);
				counter++;
				if(counter % 1000 == 0){
					System.out.println("Fetched " + counter + " movies with more than " + minItemRating + " ratings");
				}
			}
			movieRS.close();
			System.out.println("Fetched " + counter + " movies with more than " + minItemRating + " ratings");

			
			ResultSet userRS = this.dbConnect.createStatement().executeQuery("SELECT user FROM users WHERE nb_ratings >= " + minUserRating);
			PreparedStatement statementForRatings = this.dbConnect.prepareStatement("SELECT ratings.movie AS movie, rating, slice, movies.movie AS movie_name FROM ratings, movies WHERE movies.movie = ratings.movie AND user = ?");
			
			System.out.println("Creating users.");
			counter = 0;
			while(userRS.next()){
				int userID = userRS.getInt(1);
				User user = User.getUser(userID);
				ScoreCount<Item> profile = user.getProfile();
				ScoreCount<Item> scTest = user.getTestProfile();
				statementForRatings.setInt(1, userID);
				ResultSet rs = statementForRatings.executeQuery();
				while(rs.next()){
					int movieID = rs.getInt("movie");
					float score = rs.getFloat("rating");
					int slice = rs.getInt("slice");
					String movieName = rs.getString("movie_name");
					Item item = Item.getItem(movieID, movieName);
					if(item != null){
						if(this.trainingSlices.contains(slice)){
							profile.addValue(item, score);
							item.getProfile().addValue(user, score);
						}
						else{
							scTest.addValue(item, score);
							item.getTestProfile().addValue(user, score);
						}
					}
				}
				counter++;
				if(counter % 500 == 0){
					System.out.println("Created " + counter + " users.");
				}
				rs.close();
			}
			System.out.println("Created " + counter + " users.");
			statementForRatings.close();
			userRS.close();
		} catch (SQLException e) {
			System.out.println("Connection Problem occured!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void fetchTestProfiles() {
		
	}
	
	

}
