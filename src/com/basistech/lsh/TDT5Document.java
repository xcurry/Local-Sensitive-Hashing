package com.basistech.lsh;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class TDT5Document implements Document{
	private FeatureVector features;
	private String text;
	private String id;
	private List<String> topics;
	public static TFIDF t;
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
            this.features = t.computeFeatures(r, true, false);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
	}
	
	public TDT5Document(String str){
	    //text=str;
	    init(new StringReader(str));
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
	
	
	
}
