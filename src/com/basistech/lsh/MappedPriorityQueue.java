/*
  Copyright (c) 2010, Basis Technology Corp.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other 
  materials provided with the distribution.

  Neither the name of the Basis Technology Corp. nor the names of its contributors may be used to endorse or promote products derived from this software without specific 
  prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
