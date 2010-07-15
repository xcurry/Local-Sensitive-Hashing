/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
public abstract class Featurizer implements Cloneable{
    public abstract void deriveAndAddFeatures(Document doc);
    public abstract FSDParser getParser();
    public abstract Vocabulary getVocabulary();
    @Override
    public Featurizer clone(){try{
        return (Featurizer)super.clone();
    }catch(CloneNotSupportedException e){throw new RuntimeException(e);}}

    public static int[] stringToInt(String doc, FSDParser parser, Vocabulary vocab){
        String[] tokens=parser.parse(doc);
        int[] ret = new int[tokens.length];
        for(int i = 0; i<tokens.length; i++){
            ret[i]=vocab.put(tokens[i]);
        }
        return ret;
    }
}
