package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class TThread {
    private int count=0;
    private int startTweet;
    private List<Tweet> tweets = new ArrayList<Tweet>();
    private boolean hasEntropy = false;
    private double entropy;
    private static double LOG2=Math.log(2);
    private HashSet<String> users = new HashSet<String>();
    
    public TThread(int startTweet){
        this.startTweet=startTweet;
        
    }

    public int getStartTweet() {
        return startTweet;
    }

    public void addTweet(Tweet t){
        if(!users.contains(t.getUser())){
            count++;
            users.add(t.getUser());
        }
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
//        FeatureVector fv = new FeatureVector();
//        double total=0;
//        for(Tweet t: tweets){
//            total+=fv.merge(t.getFeatures());
//        }
//        entropy=0;
//        for(double d: fv.values()){
//            entropy-=d/total*Math.log(d/total);
//        }
        HashMap<String, Integer> tf = new HashMap<String, Integer>();
        double totalCount = 0.0d;
        for(Tweet t: tweets){
            String text = t.getText();
            for (String tok : text.split("\\s+")) {
                Integer count = tf.get(tok);
                if (count == null) {
                    count = 0;
                }
                tf.put(tok, count + 1);
                ++totalCount;
            }
        }
        entropy=0;
        for (Integer count : tf.values()) {
            double p = count / totalCount;
            entropy-= p * Math.log(p);
        }
        entropy/=LOG2;
        hasEntropy = true;
        return entropy;
    }
    
}
