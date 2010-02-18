package com.basistech.lsh;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

public class ResultSet {
	private static class ScoreCompareDesc implements Comparator<ResultPair> {
		public int compare(ResultPair o1, ResultPair o2) {
			if (o1.score > o2.score) {
				return -1;
			} else if (o1.score == o2.score) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	
	private int capacity;
	private PriorityQueue<ResultPair> scoreCache;
	private Map<Object, Integer> scoreValues;
	private static ScoreCompareDesc cmp;
	
	static {
		cmp = new ScoreCompareDesc();
	}
	
	public ResultSet(int capacity) {
		this.capacity = capacity;
		scoreCache = new PriorityQueue<ResultPair>(capacity, cmp);
		scoreValues = new HashMap<Object, Integer>();		
	}
	
	public void add(Object result, int score) {
		Integer value = scoreValues.get(result);
		while (value == null || score < value) {
			push(result, score);
			while (scoreCache.size() > capacity) {
				pop();
			}
		}
	}

	private void pop() {
		ResultPair top = scoreCache.poll();
		scoreValues.remove(top.result);
		refreshCache();
	}
	
	private void push(Object result, int score) {
		scoreValues.put(result, score);
		scoreCache.add(new ResultPair(result, score));
	}
		
	private void refreshCache() {
		ResultPair top = scoreCache.peek();
		Integer value = scoreValues.get(top.result); 
		while (value != top.score) { 
			top.score = value; 
			scoreCache.poll();
			scoreCache.add(top);
			top = scoreCache.peek();
			value = scoreValues.get(top.result); 
		}				
	}
	
	public void merge(ResultSet other) {
		for (Entry<Object, Integer> entry : other.scoreValues.entrySet()){
			add(entry.getKey(), entry.getValue());			
		}		
	}
	
	public void clear() {
		scoreValues.clear();
		scoreCache.clear();
	}	
}
