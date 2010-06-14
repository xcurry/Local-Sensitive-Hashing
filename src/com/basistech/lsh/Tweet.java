package com.basistech.lsh;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class Tweet implements Document{
	private FeatureVector features;
	private String text;
	private String id;
	private List<String> topics;
	private TThread tthread = null;

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
	
	public Tweet(String str, FeatureVector fv){
	    //text=str;
	    features=fv;
	    text=str;
	}

	public TThread getTThread() {
        return tthread;
    }

    public void setTThread(TThread tthread) {
        this.tthread = tthread;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
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
}
