package com.basistech.lsh;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

//TODO: rename to sthg like MappedPriorityQueue
public class ResultSet {
    private static class ScoreCompareDesc implements Comparator<ResultPair> {
        public int compare(ResultPair p1, ResultPair p2) {
            if (p1.score > p2.score) {
                return -1;
            } else if (p1.score == p2.score) {
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
        ResultPair top = scoreCache.peek();
        if (top != null && score > top.score && scoreValues.size() >= capacity) {
            return;
        }
        Integer value = scoreValues.get(result);
        if (value == null || score < value) {
            push(result, score);
            while (scoreValues.size() > capacity) {
                pop();
            }
        }
    }

    private void pop() {
        ResultPair top = scoreCache.poll();
        scoreValues.remove(top.result);
        updateCache();
    }

    private void push(Object result, int score) {
        scoreValues.put(result, score);
        scoreCache.add(new ResultPair(result, score));
        updateCache();
    }

    private void updateCache() {
        ResultPair top = scoreCache.peek();
        Integer value = scoreValues.get(top.result);
        while (value != null && value != top.score) { 
            scoreCache.poll();
            top = scoreCache.peek();
            value = scoreValues.get(top.result); 
        }				
    }

    public void merge(ResultSet other) {
        for (Entry<Object, Integer> entry : other.scoreValues.entrySet()){
            add(entry.getKey(), entry.getValue());			
        }
    }

    public void merge(Map<Object, Integer> m) {
        for (Entry<Object, Integer> entry : m.entrySet()){
            add(entry.getKey(), entry.getValue());                      
        }
    }

    
    public void clear() {
        scoreValues.clear();
        scoreCache.clear();
    }	
    
    public String toString() {
        return "cache: " + scoreCache.toString() + "\n"
            + "values: " + scoreValues.toString();        
    }
}
