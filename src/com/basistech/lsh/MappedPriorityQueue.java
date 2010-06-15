package com.basistech.lsh;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

//This class is probably broken.
@Deprecated
public class MappedPriorityQueue <T> {
    private static class ScoreCompareDesc implements Comparator<ResultPair<?>> {
        public int compare(ResultPair<?> p1, ResultPair<?> p2) {
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
    private PriorityQueue<ResultPair<T>> scoreCache;
    private Map<T, Double> scoreValues;
    private static ScoreCompareDesc cmp;

    static {
        cmp = new ScoreCompareDesc();
    }

    public MappedPriorityQueue(int capacity) {
        this.capacity = capacity;
        scoreCache = new PriorityQueue<ResultPair<T>>(capacity, cmp);
        scoreValues = new HashMap<T, Double>();		
    }

    public void add(T result, double score) {
        ResultPair<T> top = scoreCache.peek();
        if (top != null && score > top.score && scoreValues.size() >= capacity) {
            return;
        }
        Double value = scoreValues.get(result);
        if (value == null || score < value) {
            push(result, score);
            while (scoreValues.size() > capacity) {
                pop();
            }
        }
    }

    public T pop() {
        ResultPair<T> top = scoreCache.poll();
        if(top==null){
            return null;
        }
        scoreValues.remove(top.result);
        updateCache();
        return top.result;
    }

    private void push(T result, double score) {
        scoreValues.put(result, score);
        scoreCache.add(new ResultPair<T>(result, score));
        updateCache();
    }

    private void updateCache() {
        ResultPair<T> top = scoreCache.peek();
        Double value = scoreValues.get(top.result);
        while (value != null && value != top.score) { 
            scoreCache.poll();
            top = scoreCache.peek();
            value = scoreValues.get(top.result); 
        }				
    }

    public void merge(MappedPriorityQueue<T> other) {
        for (Entry<T, Double> entry : other.scoreValues.entrySet()){
            add(entry.getKey(), entry.getValue());			
        }
    }

    public void merge(Map<T, Double> m) {
        for (Entry<T, Double> entry : m.entrySet()){
            add(entry.getKey(), entry.getValue());                      
        }
    }

    public ResultPair<T> top() {
        return scoreCache.peek();
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
