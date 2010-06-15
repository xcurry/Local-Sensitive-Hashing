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

    public TFIDF2() {
        df = new HashMap<Integer, Integer>();
        vocab = new Vocabulary();
    }

    private class TFPair {
        public HashMap<String, Integer> tf;
        public int total;		
    }
    
    public void addToIDF(String text){
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
    }

    public TFPair computeTermFrequency(String str){
        //TODO: add lexer interface & whitespace lexer
        HashMap<String, Integer> tf = new HashMap<String, Integer>(); 
        int totalCount = 0;
        String[] toks = str.toLowerCase().split("\\W+");
        //System.out.println(str+"\n\n");
        //for(int i=0; i<toks.length; i++){
        //    System.out.println(i+":"+toks[i]);
        //}
        //try{
        //    Thread.sleep(5);
        //}catch(Exception e){}
        for (String tok : toks) {
            if("".equals(tok)){
                continue;
            }
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

    public FeatureVector computeFeatures(String f, boolean useIDF){
        TFPair tfp = computeTermFrequency(f);
        int tfTotal = tfp.total;
        FeatureVector fv = new FeatureVector();
        nDocs=nDocs+1;
        for (Entry<String, Integer> entry : tfp.tf.entrySet()) {
            String tok = entry.getKey();
            int termCount = entry.getValue();
            double tf = termCount;// / (double)tfTotal;
            int termId = vocab.put(tok);
            if(useIDF){
                Integer docCount = df.get(termId);
                double dbDocCount=0;
                if(docCount==null){
                    dbDocCount=.5;
                }else{
                    dbDocCount=docCount;
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
