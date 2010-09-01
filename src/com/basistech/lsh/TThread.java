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
    private static FSDParser parser = new NonwordSplitParser();
    
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
            for (String tok : parser.parse(t.getText())) {
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
