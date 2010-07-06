package com.basistech.lsh;

import java.util.List;

public interface Document {
    public String getText();
    /*
     * Most DocStores will not initialize this property--use a Featurizer to
     * set it.  
     */
    public FeatureVector getFeatures();
    public void setFeatures(FeatureVector fv);
    public List<String> getAnnotations();
}
