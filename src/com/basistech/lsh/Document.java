package com.basistech.lsh;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class Document {
	private FeatureVector features;
	private String text;
	private String id;
	private List<String> topics;
	private static TFIDF t;
	static{
	    t=new TFIDF();
	}

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
	
	private void init(Reader r){
        try{
            this.features = t.computeFeatures(r, false);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
	}
	
	public Document(String str){
	    //text=str;
	    init(new StringReader(str));
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
