package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TThread {
    public static int recordingPeriod=100000;
    
    private int count=0;
    private int startTweet;
    private List<Tweet> tweets = new ArrayList<Tweet>();
    private boolean hasEntropy = false;
    private double entropy;
    private static double LOG2=Math.log(2);
    private HashSet<String> users = new HashSet<String>();
    private static int nextid=0;
    private int id;
    private TThread parent=null;
    
    public TThread(int startTweet){
        this.startTweet=startTweet;
        id=nextid;
        nextid++;
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
        hasEntropy=false;
    }
    
    public void absorb(TThread other){
        other.setParent(this);
        for(Tweet t: other.getTweets()){
            if(t.getUid()<startTweet+recordingPeriod){
                addTweet(t);
            }
        }
    }
    
    private void setParent(TThread parent){
        this.parent=parent;
    }

    //unsorted
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
        
        HashMap<String, Integer> unigramCount = new HashMap<String, Integer>();
        double totalCount = 0.0d;
        for (Tweet t : tweets) {
            for (String tok : t.getText().split("\\W+")) {
                Integer count = unigramCount.get(tok);
                if (count == null) {
                    count = 0;
                }
                unigramCount.put(tok, count + 1);
                ++totalCount;
            }
        }
        entropy=0;
        for (int count : unigramCount.values()) {
            double p = count / totalCount;
           entropy -= p * Math.log(p);
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
        entropy/=LOG2;
        hasEntropy = true;
        return entropy;
    }

    public int getId() {
        return id;
    }
    
    public TThread getRoot(){
        if(parent==null){
            return this;
        }else{
            parent=parent.getRoot();
            return parent;
        }
    }
    
}
