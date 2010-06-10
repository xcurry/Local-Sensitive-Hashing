package com.basistech.lsh;

public class ResultPair<T> {
    public ResultPair(T result, double score) {
        this.result = result;
        this.score = score;
    }
    public T result;
    public double score;
    
    public String toString() {
        return "(" + result.toString() + "," + score + ")";        
    }
}
