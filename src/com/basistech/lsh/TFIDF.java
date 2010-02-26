package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class TFIDF {
    private HashMap<String, Integer> df;
    private Vocabulary vocab;	
    private HashMap<String, FeatureVector> tfidf;
    private int nDocs;

    public TFIDF() {
        df = new HashMap<String, Integer>();
        vocab = new Vocabulary();
        tfidf = new HashMap<String, FeatureVector>();
    }

    private class TFPair {
        public HashMap<String, Integer> tf;
        public int total;		
    }

    public TFPair computeTermFrequency(String f) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = null;
        HashMap<String, Integer> tf = new HashMap<String, Integer>(); 
        int totalCount = 0;
        while ((line = in.readLine()) != null) {
            String[] toks = line.split("\\s+");
            for (String tok : toks) {
                int count = tf.get(tok);
                tf.put(tok, count + 1);
                ++totalCount;
            }
        }
        TFPair tfp = new TFPair();
        tfp.tf = tf;
        tfp.total = totalCount;
        return tfp;
    }

    public void computeDocumentFrequency(String[] files) throws IOException {
        for (String f : files) {
            TFPair tfp = computeTermFrequency(f);
            for (String term : tfp.tf.keySet()) {
                int count = df.get(term);
                df.put(term, count + 1);
                vocab.put(term);
            }			
        }
        nDocs = files.length;
    }

    public void computeTFIDF(String[] files) throws IOException {
        for (String f : files) {
            TFPair tfp = computeTermFrequency(f);
            int tfTotal = tfp.total;
            FeatureVector fv = new FeatureVector();
            for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
                String tok = entry.getKey();
                int docCount = df.get(tok);
                double idf = LogN.value(nDocs) - LogN.value(docCount);
                int termCount = entry.getValue();
                double tf = termCount / (double)tfTotal;
                int termId = vocab.get(tok);
                fv.put(termId, tf*idf);				
            }
            tfidf.put(f, fv);
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
