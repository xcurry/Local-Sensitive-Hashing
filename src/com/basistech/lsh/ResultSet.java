package com.basistech.lsh;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Keep track of the objects with the highest score.  
 * @author cdoersch
 *
 * @param <T>
 */
public class ResultSet <T> {
    private static class ScoreCompareDesc implements Comparator<ResultPair<?>> {
        public int compare(ResultPair<?> p1, ResultPair<?> p2) {
            if (p1.score > p2.score) {
                return 1;
            } else if (p1.score == p2.score) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private int capacity;
    private PriorityQueue<ResultPair<T>> scoreCache;
    private static ScoreCompareDesc cmp;

    static {
        cmp = new ScoreCompareDesc();
    }

    public ResultSet(int capacity) {
        this.capacity = capacity;
        scoreCache = new PriorityQueue<ResultPair<T>>(capacity, cmp);
    }

    public void add(T result, double score) {
        ResultPair<T> bot = scoreCache.peek();
        if (bot != null && score <= bot.score && scoreCache.size() == capacity) {
            return;
        }
        if(scoreCache.size()==capacity){
            scoreCache.poll();
        }
        scoreCache.add(new ResultPair<T>(result, score));
    }

    public ResultPair<T> worst() {
        return scoreCache.peek();
    }
    
    public void clear() {
        scoreCache.clear();
    }	
    
    public int numResults(){
        return scoreCache.size();
    }
    
    @SuppressWarnings("unchecked")
    public List<ResultPair<T>> popResults(){
        int numEls=scoreCache.size();
        ResultPair<T>[] results = (ResultPair<T>[]) new ResultPair[numEls];
        for(int i=numEls-1; i>=0; i--){
            results[i]= scoreCache.poll();
        }
        return Arrays.asList(results);
    }
    
    public String toString() {
        return "cache: " + scoreCache.toString();        
    }
}
