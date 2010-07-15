package com.basistech.lsh;

import java.util.List;

public class Tweet implements Document, Comparable<Tweet> {

    private FeatureVector features;
    private String text;
    private String id;
    private List<String> topics;
    private TThread tthread = null;
    private String user;
    private String dateTime;
    private int uid;
    private int[] hash;

    //public Document(FeatureVector features) {
    //	super();
    //	this.features = features;
    //}
    //public Document(File f){
    //    try{
    //        init(new FileReader(f));
    //    }catch(Exception e){
    //        throw new RuntimeException(e);
    //    }
    //}
    public Tweet(String str, String user, String date, int uid) {
        //text=str;
        features = null;
        text = str;
        this.user = user;
        dateTime = date;
        this.uid = uid;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof Tweet)){
            return false;
        }
        return ((Tweet)other).uid==this.uid;
    }

    @Override
    public int hashCode(){
        return uid;
    }

    @Override
    public int[] getHash() {
        return hash;
    }

    @Override
    public void setHash(int[] hash) {
        this.hash=hash;
    }


    public TThread getTThread() {
        return tthread.getRoot();
    }

    public void setTThread(TThread tthread) {
        this.tthread=tthread;
    }

    public int getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAnnotations() {
        return topics;
    }

    public void setAnnotations(List<String> topics) {
        this.topics = topics;
    }

    public FeatureVector getFeatures() {
        return features;
    }

    public void setFeatures(FeatureVector features) {
        this.features = features;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int compareTo(Tweet other) {
        if (other.getTThread().getEntropy() > 3.5 && tthread.getEntropy() <= 3.5) {
            return 1;
        }
        if (other.getTThread().getEntropy() <= 3.5 && tthread.getEntropy() > 3.5) {
            return -1;
        }
        return other.getTThread().getCount() - tthread.getCount();
    }

    public String toString() {
        return dateTime + "\t" + getTThread().getId() + "\t" + text;
    }
}
