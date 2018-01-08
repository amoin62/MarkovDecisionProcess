package matrixFactorization.util;

import java.util.Comparator;
import java.util.Map.Entry;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EntryComparator implements
		Comparator<Entry<?, ? extends Comparable>> {

	public int compare(Entry<?, ? extends Comparable> arg0,
			Entry<?, ? extends Comparable> arg1) {
		return arg0.getValue().compareTo(arg1.getValue());
	}
}
