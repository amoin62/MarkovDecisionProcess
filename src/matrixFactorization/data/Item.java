package matrixFactorization.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import matrixFactorization.coordinate.CartesianCoordinate;
import matrixFactorization.coordinate.IProvidesPosition;
import matrixFactorization.util.ScoreCount;

public class Item implements IProvidesPosition<CartesianCoordinate> {
	private static final float UNSET = -1f;
	private static Map<Item, Item> factory = new HashMap<Item, Item>();

	public static String noName = "noItemName";
	
	private int num;

	private String name;

	public void setName(String name) {
		this.name = name;
	}
	
	public float bi;	
	private int popularity;	
	private float meanRating;
	private CartesianCoordinate coordinate;	
	private boolean busy;	
	private ScoreCount<User> profile;
	private ScoreCount<User> testProfile;
	
	@SuppressWarnings("unused")
	private Item() {
		
	}
	
	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	static synchronized public Item getItem(int n, String val) {
		Item t = new Item(n, val);
		Item ret = factory.get(t);
		if (ret == null) {
			factory.put(t, t);
			return t;
		} else {
			return ret;
		}
	}

	static synchronized public Item getItem(int n) {
		return getItem(n, noName);
	}

	static synchronized public Item getTempItem(int n, String val) {
		Item t = new Item(n, val);
		Item ret = factory.get(t);
		if (ret == null) {
			factory.put(t, t);
			return t;
		} else {
			return ret;
		}
	}

	static synchronized public Item getTempItem(int n) {
		return getTempItem(n, "" + n);
	}
	
	public static List<Item> getMostPopuarItems(int offset, int k) {
		List<Item> itemsList = new ArrayList<>(factory.keySet());
		Collections.sort(itemsList, new Comparator<Item>() {

			@Override
			public int compare(Item o1, Item o2) {
				int o1NumOfRatings = o1.getNumOfRating();
				int o2NumOfRatings = o2.getNumOfRating();
				int comparison = 0;
				if(o1NumOfRatings > o2NumOfRatings) {
					comparison = -1;
				} else if (o1NumOfRatings < o2NumOfRatings) {
					comparison = 1;
				}
				return comparison;
			}
		});
		int modifiedK = Math.min(itemsList.size(), k);
		List<Item> popularKItems = itemsList.subList(offset, offset + modifiedK);
		for(int i = 0; i < popularKItems.size(); i++) {
			Item item = popularKItems.get(i);
			System.out.println(i + "\t" + item.getName() + "\t" + item.getNumOfRating());
		}
		return popularKItems;
	}

	protected Item(int n, String val) {
		this.busy=false;
		this.num = n;
		this.name = val;
		this.popularity = 0;
		this.coordinate = new CartesianCoordinate(n);
		this.profile = new ScoreCount<>();
		this.testProfile = new ScoreCount<>();
		this.meanRating = UNSET;
		this.bi = 0.0f;
	}
	
	protected Item(int n) {
		this(n, "");
	}

	public ScoreCount<User> getProfile() {
		return this.profile;
	}
	
	public ScoreCount<User> getTestProfile() {
		return this.testProfile;
	}
	
	public synchronized float getMeanRating() {
		if(this.meanRating == UNSET) {
			this.meanRating = (float) this.profile.getAverage();
		}
		return meanRating;
	}
	
	public synchronized int getNumOfRating() {
		return this.profile.size();
	}
	
	public CartesianCoordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(CartesianCoordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	public static Map<Item, Item> getFactory() {
		return factory;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof Item) {
			return this.num == ((Item) obj).num;
		}
		return false;
	}

	@Override
	public final int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + num;
		return result;
	}

	@Override
	public final String toString() {
		if (this.name == Item.noName) {
			return "" + this.num;
		} else {
			return this.name;
		}
	}

	public final int getPopularity() {
		return popularity;
	}

	public synchronized final void setPopularity(int popularity) {
		this.popularity = popularity;
	}

	public synchronized final void incPopularity() {
		this.popularity++;
	}

	public final int getNum() {
		return this.num;
	}
	
		
	public synchronized Item getItem(){
			while(this.isBusy()){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.setBusy(true);
			return this;
	}
	
	public synchronized void releaseItem(){
			this.setBusy(false);
			this.notify();
	}
	
	public float getBi() {
		return bi;
	}

	public void setBi(float bi) {
		this.bi = bi;
	}
	
	public String getName(){
		return this.name;
	}

}
