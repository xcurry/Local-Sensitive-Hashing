package com.basistech.lsh;

public class ResultPair {
    public ResultPair(Object result, int score) {
        this.result = result;
        this.score = score;
    }
    public Object result;
    public int score;
    
    public String toString() {
        return "(" + result.toString() + "," + score + ")";        
    }
}
