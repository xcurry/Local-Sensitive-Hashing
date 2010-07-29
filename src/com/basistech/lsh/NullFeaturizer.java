/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
public class NullFeaturizer extends Featurizer{
    private Vocabulary vocab;
    public NullFeaturizer(Vocabulary vocab){this.vocab = vocab;}
    @Override public void deriveAndAddFeatures(Document doc) {}
    @Override public FSDParser getParser() {return null;}
    @Override public Vocabulary getVocabulary() {return vocab;}
}
