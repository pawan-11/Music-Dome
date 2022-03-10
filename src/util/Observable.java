package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

abstract public class Observable {

	protected HashMap<String, ArrayList<Observer>> observers; //TODO: could make it Class instead of class string
	protected HashMap<String, Integer> lists;
	
	public Observable() {
		observers = new HashMap<String, ArrayList<Observer>>();
		lists = new HashMap<String, Integer>();
		create_lists();
	}
	
	abstract public void create_lists();
	
	protected void create_list(String type, int max_observers) {
		observers.put(type, new ArrayList<Observer>());
		lists.put(type, max_observers);
	}
	
	public void addObserver(String type, Observer o) {
		if (observers.containsKey(type)) {
			if (lists.get(type).intValue() == observers.get(type).size())
				observers.get(type).remove(0);
			observers.get(type).add(o);
		}
	}
	
	public void clearObservers() {
		Set<String> types = observers.keySet();
		for (String type: types)
			observers.get(type).clear();
	}
	
	public void clearObservers(String type) {
		observers.get(type).clear();
	}
	
	public void changed(String msg) {
		Set<String> types = observers.keySet();
		for (String type: types)
			for (Observer o: observers.get(type))
				o.update(msg);
	}
}
