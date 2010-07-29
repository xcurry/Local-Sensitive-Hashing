package com.basistech.lsh;

public interface OnlineLearner {
    public void train(FeatureVector feats, int label);
    public int predict(FeatureVector feats);
    public void finish();
    public double predictMargin(FeatureVector feats);
    public double getPositiveThreshold(); 
    public String getName();
    public String print(Vocabulary vocab);
}
