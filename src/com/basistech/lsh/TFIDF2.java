package com.basistech.lsh;

import java.util.HashMap;
import java.util.Map.Entry;

//most of this class is probably broken--computeFeatures 
//and computeTermFrequency work, though
//TODO: make feature extractor interface
//TODO: add lexer interface & whitespace lexer
public class TFIDF2 {
    private HashMap<Integer, Integer> df;
    private Vocabulary vocab;	
    private int nDocs;
    
    //if false, give featurevectors that are raw counts. 
    private boolean giveProportions=true;
    //idf-weight the featurevectors.  Not compatible with giveProportions==false.
    private boolean useIDF=false;

    public TFIDF2() {
        df = new HashMap<Integer, Integer>();
        vocab = new Vocabulary();
    }

    public void setGiveProportions(boolean giveProportions) {
        this.giveProportions = giveProportions;
        if(!giveProportions){
            useIDF=false;
        }
    }

    public void setUseIDF(boolean useIDF) {
        this.useIDF = useIDF;
        if(useIDF){
            giveProportions=true;
        }
    }



    private class TFPair {
        public HashMap<String, Integer> tf;
        public int total;		
    }
    
    public void addToIDF(String text){
        if(!useIDF){
            throw new RuntimeException("cannot add to the IDF of a TFIDF2 that's not using IDF");
        }
        TFPair tfp = computeTermFrequency(text);
        for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
            String tok = entry.getKey();
            int termId = vocab.put(tok);
            Integer count = df.get(termId);
            if (count == null) {
                count = 0;
            }
            df.put(termId, count + 1);
        }
        nDocs=nDocs+1;
    }

    public TFPair computeTermFrequency(String str){
        //TODO: add lexer interface & whitespace lexer
        HashMap<String, Integer> tf = new HashMap<String, Integer>(); 
        int totalCount = 0;
        String[] toks = str.split("\\W+");
        for (String tok : toks) {
            Integer count = tf.get(tok);
            if (count == null) {
                count = 0;
            }
            tf.put(tok, count + 1);
            ++totalCount;
        }
        TFPair tfp = new TFPair();
        tfp.tf = tf;
        tfp.total = totalCount;
        return tfp;
    }

    public FeatureVector computeFeatures(String f){
        TFPair tfp = computeTermFrequency(f);
        int tfTotal = tfp.total;
        FeatureVector fv = new FeatureVector();
        for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
            String tok = entry.getKey();
            int termCount = entry.getValue();
            double tf = termCount;
            if(giveProportions){
                tf /= (double)tfTotal;
            }
            int termId = vocab.put(tok);
            if(useIDF){
                Integer docCount = df.get(termId);
                double dbDocCount=0;
                if(docCount==null){
                    dbDocCount=.5;//nDocs/(double)10000;
                }else{
                    dbDocCount=docCount;//+nDocs/(double)10000;
                }
                double idf = LogN.value(nDocs) - Math.log(dbDocCount)/Math.log(2);
                fv.put(termId, tf*idf);
            }else{
            	fv.put(termId, tf);
            }
        }
        return fv;
    }
}
