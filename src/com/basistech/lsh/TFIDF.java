package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map.Entry;

//most of this class is probably broken--computeFeatures 
//and computeTermFrequency work, though
//TODO: make feature extractor interface
//TODO: add lexer interface & whitespace lexer
public class TFIDF {
    private HashMap<Integer, Integer> df;
    private Vocabulary vocab;	
    private HashMap<String, FeatureVector> tfidf;
    private int nDocs;
    private int totalWordCount;

    public TFIDF() {
        df = new HashMap<Integer, Integer>();
        vocab = new Vocabulary();
        tfidf = new HashMap<String, FeatureVector>();
    }

    private class TFPair {
        public HashMap<String, Integer> tf;
        public int total;		
    }

    public TFPair computeTermFrequency(Reader f) throws IOException {
        //TODO: add lexer interface & whitespace lexer
        BufferedReader in = new BufferedReader(f);
        String line = null;
        HashMap<String, Integer> tf = new HashMap<String, Integer>(); 
        int totalCount = 0;
        while ((line = in.readLine()) != null) {
            String[] toks = line.toLowerCase().split("\\W+");
            for (String tok : toks) {
                Integer count = tf.get(tok);
                if (count == null) {
                    count = 0;
                }
                tf.put(tok, count + 1);
                ++totalCount;
            }
        }
        TFPair tfp = new TFPair();
        tfp.tf = tf;
        tfp.total = totalCount;
        return tfp;
    }

    //public void computeDocumentFrequency(File[] files) throws IOException {
    //    for (File f : files) {
    //        TFPair tfp = computeTermFrequency(new FileReader(f));
    //        for (String term : tfp.tf.keySet()) {
    //            int termId = vocab.put(term);
    //            Integer count = df.get(termId);
    //            if (count == null) {
    //                count = 0;
    //            }
    //            df.put(termId, count + 1);
    //        }			
    //    }
    //    nDocs = files.length;
    //}

    public FeatureVector computeFeatures(Reader f, boolean useIDF, boolean updateDocCount) throws IOException {
        TFPair tfp = computeTermFrequency(f);
        int tfTotal = tfp.total;
        FeatureVector fv = new FeatureVector();
        //int wordCount=0;
        if(useIDF&&updateDocCount){
            for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
                String tok = entry.getKey();
                //int termCount = entry.getValue();
                int termId = vocab.put(tok);
                Integer count = df.get(termId);
                if (count == null) {
                    count = 0;
                }
                df.put(termId, count + 1);
                //wordCount+=termCount;
            }
        }
        totalWordCount+=tfp.total;
        nDocs=nDocs+1;
        for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
            String tok = entry.getKey();
            int termCount = entry.getValue();
            double tf = termCount / (double)tfTotal;/// (termCount+.5+1.5*tfp.total*nDocs/totalWordCount);
            int termId = vocab.put(tok);
            if(useIDF){
                Integer docCount = df.get(termId);
                double dbDocCount=0;
                if(docCount==null){
                    dbDocCount=.5;
                }else{
                    dbDocCount=docCount;
                }
                double idf = LogN.value(nDocs) - Math.log(dbDocCount)/Math.log(2);//Math.log(totalWordCount/(double) docCount)/Math.log(totalWordCount+1);
                fv.put(termId, tf*idf);
            }else{
            	fv.put(termId, tf);
            }
        }
        return fv;
    }
    
    /*      double tf = termCount / (double)tfTotal;
            int termId = vocab.get(tok);
            if(useIDF){
                int docCount = df.get(termId);
                double idf = LogN.value(nDocs) - LogN.value(docCount);
                fv.put(termId, tf*idf);
            }else{
                fv.put(termId, tf);
            }
     */

    public void computeTFIDF(File[] files) throws IOException {
        for (File f : files) {
            tfidf.put(f.getName(), computeFeatures(new FileReader(f), true, true));
        }
        df = null;
    }

    public int getNDocs() {
        return nDocs;
    }

    public HashMap<String, FeatureVector> getTfidf() {
        return tfidf;
    }

    public Vocabulary getVocab() {
        return vocab;
    }
}
