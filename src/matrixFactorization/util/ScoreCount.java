package matrixFactorization.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ScoreCount<C> implements Cloneable {
	public static final double NO_RATING = 0;
	private Map<C, Float> map;

	private float norm;

	private boolean normUpToDate;

	private float sum;

	private boolean sumUpToDate;

	public ScoreCount() {
		this.map = new HashMap<C, Float>();
		this.normUpToDate = false;
		this.sumUpToDate = false;
	}

	public ScoreCount(Map<C, Float> m) {
		this.map = m;
		this.normUpToDate = false;
		this.sumUpToDate = false;
	}
	
	public ScoreCount(ScoreCount<C> sc) {
		ScoreCount<C> copy = new ScoreCount<C>();
		copy.norm = sc.norm;
		copy.normUpToDate = sc.normUpToDate;
		copy.sum = sc.sum;
		copy.sumUpToDate = sc.sumUpToDate;
		copy.map = new HashMap<C, Float>(sc.map);
	}

	public final void addValue(C c, float d) {
		this.normUpToDate = false;
		this.sumUpToDate = false;
		Float currentCount = this.map.get(c);
		if (currentCount == null) {
			this.map.put(c, d);
		} else {
			this.map.put(c, currentCount + d);
		}
	}

	public final void add(ScoreCount<C> a) {
		this.normUpToDate = false;
		this.sumUpToDate = false;
		for (Entry<C, Float> en : a.map.entrySet()) {
			Float currentCount = this.map.get(en.getKey());
			if (currentCount == null) {
				this.map.put(en.getKey(), en.getValue());
			} else {
				this.map.put(en.getKey(), currentCount + en.getValue());
			}
		}
	}

	public final void setValue(C c, float d) {
		this.normUpToDate = false;
		this.sumUpToDate = false;
		this.map.put(c, d);
	}
	
	//This function computes the Pearson correlation between two users.
	public final double getSimilarity(ScoreCount<C> sc) {
		double similarity = 0;
		double thisDiv = 0;
		double scDiv = 0;
		double thisAverage = this.getAverage();
		double scAverage = sc.getAverage();
		for (Entry<C, Float> en : this.map.entrySet()) {
			double scScore = sc.getValue(en.getKey());
			if (scScore != NO_RATING) {
				double thisCentered = en.getValue() - thisAverage;
				double scCentered = scScore - scAverage;
				similarity += thisCentered * scCentered;
				thisDiv += thisCentered * thisCentered;
				scDiv += scCentered * scCentered;
			}
		}
		if (thisDiv == 0 || scDiv == 0) {
			return 0;
		} else {
			return similarity / (Math.sqrt(thisDiv * scDiv));
		}
	}
	
	public final double getUtility(ScoreCount<C> sc){
		return this.getSimilarity(sc)*this.getCertainty(sc);
	}
	public final double getCosSimilarity(ScoreCount<C> sc){
		double similarity = 0;
		double thisDiv = 0;
		double scDiv = 0;
		for(Entry<C,Float> en: this.map.entrySet()){
			double scScore=sc.getValue(en.getKey());
			if(scScore!=NO_RATING){
				double thisCos = en.getValue();
				double scCos= scScore;
				similarity += thisCos * scCos;
				thisDiv += thisCos * thisCos;
				scDiv += scCos * scCos;
			}
		}
		if (thisDiv == 0 || scDiv == 0) {
			return 0;
		} else {
			return similarity / (Math.sqrt(thisDiv * scDiv));
		}
	}
	
	public final double getCosUtility(ScoreCount<C> sc){
		return this.getCosSimilarity(sc) * this.getCertainty(sc);
	}
	
	public final double getCertainty(ScoreCount<C> sc) {
		//return 1.0;
		//return (1/Math.sqrt((double)sc.getItems().size()));
		//return Math.sqrt(this.getNbCommon(sc));
		//return (1/Math.sqrt((double)sc.getItems().size()))* Math.sqrt(this.getNbCommon(sc));
		//return this.getNbCommon(sc) / ((double) this.size());
		/*
		if (getNbCommon(sc)!= 0)
			return Math.log1p(getNbCommon(sc))+ 0.05;
			else
				return 0.0;*/
			//if (getNbCommon(sc)!= 0)
			//return Math.log10(getNbCommon(sc))+0.05;
			return (double)getNbCommon(sc)/30.0;
			//return (double)sc.getItems().size()/40;
			//return 1.0;
	}
	
	public final int getNbCommon(ScoreCount<C> sc) {
		int res = 0;
		for (C c : this.map.keySet()) {
			if (sc.contains(c)) {
				res++;
			}
		}
		return res;
	}

	public final void removeValue(C c, float d) {
		this.normUpToDate = false;
		this.sumUpToDate = false;
		Float currentCount = this.map.get(c);
		if (currentCount != null) {
			float newCount = currentCount - d;
			if (newCount == 0) {
				this.map.remove(c);
			} else {
				this.map.put(c, newCount);
			}
		}
	}

	public final float getValue(C c) {
		Float val = this.map.get(c);
		if (val == null) {
			return 0;
		} else {
			return val;
		}
	}

	public final float getValueObject(Object c) {
		Float val = this.map.get(c);
		if (val == null) {
			return 0;
		} else {
			return val;
		}
	}

	public final List<C> getItems() {
		return new ArrayList<C>(this.map.keySet());
	}

	public List<C> getSortedItems() {
		List<C> res = new ArrayList<C>();
		if (!this.map.isEmpty()) {
			List<Entry<C, Float>> enl = new ArrayList<Entry<C, Float>>(
					this.map.size());
			for (Entry<C, Float> en : this.map.entrySet()) {
				enl.add(en);
			}
			Collections.sort(enl,
					Collections.reverseOrder((new EntryComparator())));
			for (Entry<C, Float> en : enl) {
				res.add(en.getKey());
			}
		}
		return res;
	}

	public final Map<C, Float> getMap() {
		return this.map;
	}

	public final float getNorm() {
		if (!this.normUpToDate) {
			this.norm = 0;
			for (Float d : this.map.values()) {
				this.norm += Math.pow(d, 2);
			}
			this.norm = (float) Math.sqrt(this.norm);
			this.normUpToDate = true;
		}
		return this.norm;
	}
	
	public final double getAverage() {
		return this.getSum() / this.size();
	}

	public final float getSum() {
		if (!this.sumUpToDate) {
			this.sum = 0;
			for (Float d : this.map.values()) {
				this.sum += d;
			}
			this.sumUpToDate = true;
		}
		return this.sum;
	}

	public final double getScalar(ScoreCount<C> sc) {
		double scalar = 0;
		for (Entry<C, Float> m1e : this.map.entrySet()) {
			double m2score = sc.getValue(m1e.getKey());
			scalar += m2score * m1e.getValue();
		}
		return scalar;
	}

	public final double getCos(ScoreCount<C> sc) {
		double n1 = this.getNorm();
		if (n1 == 0) {
			return 0;
		}
		double n2 = sc.getNorm();
		if (n2 == 0) {
			return 0;
		}
		return this.getScalar(sc) / (n1 * n2);
	}

	public final int size() {
		return this.map.size();
	}

	public final void removeFloor(double val) {
		this.normUpToDate = false;
		this.sumUpToDate = false;
		List<C> remove = new ArrayList<C>();
		for (C c : this.map.keySet()) {
			if (this.map.get(c) < val) {
				remove.add(c);
			}
		}
		for (C c : remove) {
			this.map.remove(c);
		}
	}

	public final void trimToSize(int nb) {
		if (nb < this.size()) {
			this.normUpToDate = false;
			this.sumUpToDate = false;
			List<Entry<C, Float>> enl = new ArrayList<Entry<C, Float>>(
					this.map.entrySet());
			Collections.sort(enl, new EntryComparator());
			for (Entry<C, Float> en : enl.subList(0, this.size() - nb)) {
				this.map.remove(en.getKey());
			}
		}
	}

	public final void trimToSize(int nb, Collection<C> protect) {
		if (nb < this.size()) {
			this.normUpToDate = false;
			this.sumUpToDate = false;
			List<Entry<C, Float>> enl = new ArrayList<Entry<C, Float>>(
					this.map.size());
			for (Entry<C, Float> en : this.map.entrySet()) {
				if (!protect.contains(en.getKey())) {
					enl.add(en);
				}
			}
			Collections.sort(enl, new EntryComparator());
			for (Entry<C, Float> en : enl.subList(0, this.size() - nb)) {
				this.map.remove(en.getKey());
			}
		}
	}

	public final boolean contains(Object o) {
		return this.map.containsKey(o);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (!this.map.isEmpty()
				&& this.map.keySet().iterator().next() instanceof Comparable) {
			List<C> lc = new ArrayList<C>(this.map.keySet());
			Collections.sort((List<Comparable>) lc);
			for (C c : lc) {
				sb.append(c + " " + this.map.get(c) + "\n");
			}
		} else {
			for (Entry<C, Float> e : this.map.entrySet()) {
				sb.append(e.getKey() + " " + e.getValue() + "\n");
			}
		}
		return sb.toString();
	}

	public final C getRandomBasedOnScore() {
		double s = this.getSum();
		double rand = Math.random() * s;
		for (Entry<C, Float> en : this.map.entrySet()) {
			if (en.getValue() > rand) {
				return en.getKey();
			} else {
				rand -= en.getValue();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		ScoreCount<C> clone = null;
		try {
			clone = (ScoreCount<C>) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("error in ScoreCount clone");
			System.err.println(e);
			System.exit(-1);
		}
		clone.map = new HashMap<C, Float>(this.map);
		return clone;
	}

	public double getMax() {
		double max = Double.MIN_VALUE;
		for (Float val : this.map.values()) {
			if (val > max) {
				max = val;
			}
		}
		return max;
	}

	public double getMin() {
		double min = Double.MAX_VALUE;
		for (Float val : this.map.values()) {
			if (val < min) {
				min = val;
			}
		}
		return min;
	}

	public void remove(C c) {
		if (this.map.remove(c) != null) {
			this.normUpToDate = false;
			this.sumUpToDate = false;
		}
	}

	public void clear() {
		this.map.clear();
		this.normUpToDate = false;
		this.sumUpToDate = false;
	}

	public static void main(String[] args) {
		ScoreCount<String> s1 = new ScoreCount<String>();
		ScoreCount<String> s2 = new ScoreCount<String>();
		s1.setValue("haha", 12f);
		s2.setValue("haha", -4.5f);
		s1.setValue("hoho", 2f);
		s2.setValue("huhu", 1.33f);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s1.getScalar(s2));
		System.out.println(s1.getCos(s2));
	}
}
