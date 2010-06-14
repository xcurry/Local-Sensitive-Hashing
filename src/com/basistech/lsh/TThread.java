package com.basistech.lsh;

import java.util.ArrayList;
import java.util.List;

public class TThread {
    private int count=0;
    private int startTweet;
    private List<Tweet> tweets = new ArrayList<Tweet>();
    private boolean hasEntropy = false;
    private double entropy;
    private static double LOG2=Math.log(2);
    
    public TThread(int startTweet){
        this.startTweet=startTweet;
        
    }

    public int getStartTweet() {
        return startTweet;
    }

    public void addTweet(Tweet t){
        count++;
        tweets.add(t);
        if(hasEntropy){
            System.out.println("warning: invalidating entropy");
        }
        hasEntropy=false;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }
    
    public int getCount(){
        return count;
    }
    
    public double getEntropy(){
        if(hasEntropy){
            return entropy;
        }
        FeatureVector fv = new FeatureVector();
        double total=0;
        for(Tweet t: tweets){
            total+=fv.merge(t.getFeatures());
        }
        entropy=0;
        for(double d: fv.values()){
            entropy-=d/total*Math.log(d/total);
        }
        entropy/=LOG2;
        hasEntropy = true;
        return entropy;
    }
    
}
