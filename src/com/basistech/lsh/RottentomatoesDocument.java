package com.basistech.lsh;

import java.util.List;

public class RottentomatoesDocument implements Document {

    private FeatureVector features;
    private String text;
    private String id;
    private List<String> topics;
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
    //private void init(Reader r) {
        //try {
        //    this.features = t.computeFeatures(r, true, false);
        //} catch (IOException e) {
        //    throw new RuntimeException(e);
        //}
    //}

    public RottentomatoesDocument(String str, int uid) {
        text=str;
        this.uid=uid;
        //init(new StringReader(str));
    }

    @Override
    public int[] getHash() {
        return hash;
    }

    @Override
    public void setHash(int[] hash) {
        this.hash=hash;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<String> getAnnotations() {
        return topics;
    }

    public void setAnnotations(List<String> topics) {
        this.topics = topics;
    }

    @Override
    public FeatureVector getFeatures() {
        return features;
    }

    @Override
    public void setFeatures(FeatureVector features) {
        this.features = features;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof RottentomatoesDocument)){
            return false;
        }
        return ((RottentomatoesDocument)other).uid==this.uid;
    }

    @Override
    public int hashCode(){
        return uid;
    }
}