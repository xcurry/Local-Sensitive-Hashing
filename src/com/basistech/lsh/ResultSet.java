package com.basistech.lsh;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Keep track of the objects with the highest score.
 * Probably not efficient for large capacities.  
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
    private HashMap<T,Double> currentContents = new HashMap<T,Double>();
    private static ScoreCompareDesc cmp;

    static {
        cmp = new ScoreCompareDesc();
    }

    public ResultSet(int capacity) {
        this.capacity = capacity;
        scoreCache = new PriorityQueue<ResultPair<T>>(capacity, cmp);
    }

    public void add(T result, double score) {
        ResultPair<T> toadd = new ResultPair<T>(result, score);
        if(currentContents.containsKey(result)){
            if(score<=currentContents.get(result).doubleValue()){
                return;
            }else{
                currentContents.remove(result);
                //ResultPair's are equal if their results are equal
                scoreCache.remove(toadd);
            }
        }
        ResultPair<T> bot = scoreCache.peek();
        if (bot != null && score <= bot.score && scoreCache.size() == capacity) {
            return;
        }
        if(result instanceof TThread){
            System.out.println("added tthread with entropy "+((TThread)result).getEntropy()+" and count "+((TThread)result).getCount());
        }
        if(scoreCache.size()==capacity){
            currentContents.remove(scoreCache.poll().result);
        }
        scoreCache.add(toadd);
        currentContents.put(toadd.result,toadd.score);
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
