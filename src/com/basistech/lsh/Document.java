package com.basistech.lsh;

import java.util.List;

public interface Document {
    public String getText();
    public FeatureVector getFeatures();
    public List<String> getTopics();
}
