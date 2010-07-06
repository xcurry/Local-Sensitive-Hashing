package com.basistech.lsh;

import java.util.List;

public class TDT5Document implements Document {

    private FeatureVector features;
    private String text;
    private String id;
    private List<String> topics;
    private int uid;

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

    public TDT5Document(String str, int uid) {
        text=str;
        //init(new StringReader(str));
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
        if(!(other instanceof TDT5Document)){
            return false;
        }
        return ((TDT5Document)other).uid==this.uid;
    }

    @Override
    public int hashCode(){
        return uid;
    }
}
