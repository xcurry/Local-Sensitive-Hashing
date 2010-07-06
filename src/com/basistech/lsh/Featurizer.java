/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
public abstract class Featurizer {
    public abstract void deriveAndAddFeatures(Document doc);
    public abstract FSDParser getParser();

    public static int[] stringToInt(String doc, FSDParser parser, Vocabulary vocab){
        String[] tokens=parser.parse(doc);
        int[] ret = new int[tokens.length];
        for(int i = 0; i<tokens.length; i++){
            ret[i]=vocab.put(tokens[i]);
        }
        return ret;
    }
}
