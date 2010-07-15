/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
public class HMMFeaturizer extends Featurizer{

    private HMM hmm;
    private Vocabulary vocab;
    private FSDParser parser;

    public HMMFeaturizer(HMM hmm, Vocabulary vocab, FSDParser parser) {
        this.hmm = hmm;
        this.vocab = vocab;
        this.parser = parser;
    }

    @Override
    public Vocabulary getVocabulary() {
        return vocab;
    }

    @Override
    public void deriveAndAddFeatures(Document doc){
        doc.setFeatures(getFeatures(doc.getText()));
    }

    private FeatureVector getFeatures(String doc){
        int[] intvals = stringToInt(doc,parser,vocab);
        return hmm.getFeatures(intvals);
    }

    @Override
    public FSDParser getParser() {
        return parser;
    }

    @Override
    public HMMFeaturizer clone(){
        HMM hmmClone = hmm.clone();
        return new HMMFeaturizer(hmmClone,vocab,parser);
    }

}
