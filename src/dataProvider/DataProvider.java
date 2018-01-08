package dataProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import mdp.Main;



public abstract class DataProvider {
	protected static String DATA_BASE_URL;
	protected static String DATA_BASE_NAME;
	protected static String USER_NAME;
	protected static String PASSWORD;
	
	protected Connection dbConnect;
	
	protected DataProvider(){
		DATA_BASE_URL = Main.DATA_BASE_URL;
		DATA_BASE_NAME = Main.DATA_BASE_NAME;
		USER_NAME = Main.USER_NAME;
		PASSWORD = Main.PASSWORD;
		this.dbConnect = this.connectToDataBase();
	}
	
	protected Connection connectToDataBase() {
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://"
					+ DATA_BASE_URL + "/" + DATA_BASE_NAME, USER_NAME,
					PASSWORD);
		} catch (SQLClientInfoException e) {
			System.err
					.println("Program.connectToDataBase : connexion string not valid");
			System.err.println(e);
			return null;
		} catch (SQLTimeoutException e) {
			System.err
					.println("Program.connectToDataBase : timeout during connexion");
			System.err.println(e);
			return null;
		} catch (SQLException e) {
			System.err
					.println("Program.connectToDataBase : error during connexion");
			System.err.println(e);
			return null;
		}
		return con;
	}
	
	public void clear() {
		try {
			this.dbConnect.close();
		} catch (SQLException e) {
		}
	}

	public void fetchData(int minUserRating, int minItemRating){
		this.fetchProfiles(minUserRating, minItemRating);
		this.fetchTestProfiles();
	};
	
	public abstract void fetchProfiles(int minUserRating, int minItemRating);
	public abstract void fetchTestProfiles();

}
