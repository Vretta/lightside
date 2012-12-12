package edu.cmu.side.genesis.model;
import edu.cmu.side.plugin.SIDEPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class OrderedPluginMap implements SortedMap<SIDEPlugin, Map<String, String>>{

	private List<SIDEPlugin> ordering = new ArrayList<SIDEPlugin>();
	private Map<SIDEPlugin, Map<String, String>> configurations = new HashMap<SIDEPlugin, Map<String, String>>();
	
	public int getOrdering(Object s){
		return ordering.indexOf((SIDEPlugin)s);
	}
	
	private OrderedPluginComparator comparator = new OrderedPluginComparator(this);
	
	@Override
	public void clear() {
		ordering.clear();
		configurations.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return configurations.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return configurations.containsValue(value);
	}

	@Override
	public Map<String, String> get(Object key) {
		return configurations.get(key);
	}

	@Override
	public boolean isEmpty() {
		return configurations.isEmpty();
	}

	@Override
	public Map<String, String> put(SIDEPlugin key, Map<String, String> value) {
		System.out.println(key.toString() + " Being added to OrderedPluginMap 52");
		ordering.add(key);
		configurations.put(key, value);
		System.out.println(size() + " elements in map OPM55");
		return value;
	}

	@Override
	public void putAll(
			Map<? extends SIDEPlugin, ? extends Map<String, String>> m) {
		for(SIDEPlugin k : m.keySet()){
			ordering.add(k);
			configurations.put(k, m.get(k));
		}
	}

	@Override
	public Map<String, String> remove(Object key) {
		Map<String, String> value = configurations.get(key);
		ordering.remove(key);
		configurations.remove(key);
		return value;
	}

	@Override
	public int size() {
		System.out.println(configurations.size() + " elements in map OPM77");
		return configurations.size();
	}

	@Override
	public Comparator<? super SIDEPlugin> comparator() {
		return comparator;
	}

	@Override
	public Set<java.util.Map.Entry<SIDEPlugin, Map<String, String>>> entrySet() {
		return configurations.entrySet();
	}

	@Override
	public SIDEPlugin firstKey() {
		return ordering.get(0);
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> headMap(SIDEPlugin arg0) {
		OrderedPluginMap sub = new OrderedPluginMap();
		for(int i = 0; i < ordering.size(); i++){
			if(ordering.get(i).equals(arg0)){
				return sub;
			}else{
				sub.put(ordering.get(i), configurations.get(ordering.get(i)));				
			}
		}
		return sub;
	}

	@Override
	public Set<SIDEPlugin> keySet() {
		return configurations.keySet();
	}

	@Override
	public SIDEPlugin lastKey() {
		return ordering.get(ordering.size()-1);
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> subMap(SIDEPlugin first, SIDEPlugin last) {
		OrderedPluginMap sub = new OrderedPluginMap();
		boolean start = false;
		for(int i = 0; i < ordering.size(); i++){
			if(!start && first.equals(ordering.get(i))){
				start = true;
			}
			if(start){
				if(ordering.get(i).equals(last)){
					return sub;
				}else{
					sub.put(ordering.get(i), configurations.get(ordering.get(i)));				
				}				
			}
		}
		return sub;
	}

	@Override
	public SortedMap<SIDEPlugin, Map<String, String>> tailMap(SIDEPlugin arg0) {
		OrderedPluginMap sub = new OrderedPluginMap();
		for(int i = ordering.size()-1; i >= 0; i--){
			sub.put(ordering.get(i), configurations.get(ordering.get(i)));				
			if(ordering.get(i).equals(arg0)){
				return sub;
			}
		}
		return sub;
	}

	@Override
	public Collection<Map<String, String>> values() {
		return configurations.values();
	}

}

class OrderedPluginComparator<SIDEPlugin> implements Comparator<SIDEPlugin>{

	private OrderedPluginMap map;
	public OrderedPluginComparator(OrderedPluginMap m){
		map = m;
	}
	
	@Override
	public int compare(SIDEPlugin arg0, SIDEPlugin arg1) {
		return map.getOrdering(arg0)-map.getOrdering(arg1);
	}
	
}