/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cdoersch
 */
public class MDSDDocument implements Document{
    private FeatureVector features;
    private String text;
    private String id;
    private List<String> topics;
    private int uid;
    private int[] hash;

    public MDSDDocument(String str, FeatureVector fv, int uid, String label) {
        text=str;
        this.uid=uid;
        features=fv;
        topics = new ArrayList<String>();
        topics.add(label);
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
        if(!(other instanceof MDSDDocument)){
            return false;
        }
        return ((MDSDDocument)other).uid==this.uid;
    }

    @Override
    public int hashCode(){
        return uid;
    }
}
