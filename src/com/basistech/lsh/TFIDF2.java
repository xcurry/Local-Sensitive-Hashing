/*
  Copyright (c) 2010, Basis Technology Corp.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other 
  materials provided with the distribution.

  Neither the name of the Basis Technology Corp. nor the names of its contributors may be used to endorse or promote products derived from this software without specific 
  prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.basistech.lsh;

import java.util.HashMap;
import java.util.Map.Entry;

//most of this class is probably broken--computeFeatures 
//and computeTermFrequency work, though
public class TFIDF2 extends Featurizer{
    private HashMap<Integer, Integer> df;
    private Vocabulary vocab;
    private int nDocs;
    
    //if false, give featurevectors that are raw counts. 
    private boolean giveProportions=true;
    //idf-weight the featurevectors.  Not compatible with giveProportions==false.
    private boolean useIDF=false;
    private FSDParser parser = new CommonWordRemovalParser();

    public TFIDF2() {
        df = new HashMap<Integer, Integer>();
        vocab = new Vocabulary();
    }

    @Override
    public Vocabulary getVocabulary() {
        return vocab;
    }

    @Override
    public FSDParser getParser() {
        return parser;
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

    public void trainIDF(DocStore docs){
        Document doc = null;
        while((doc = docs.nextDoc())!=null){
            addToIDF(doc.getText());
        }
    }
    
    private void addToIDF(String text){
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

    private TFPair computeTermFrequency(String str){
        HashMap<String, Integer> tf = new HashMap<String, Integer>(); 
        int totalCount = 0;
        String[] toks = parser.parse(str);
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

    private FeatureVector computeFeatures(String f){
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
                    dbDocCount=nDocs/(double)10000;//.5;
                }else{
                    dbDocCount=docCount+nDocs/(double)10000;
                }
                double idf = LogN.value(nDocs) - Math.log(docCount)/Math.log(2);
                fv.put(termId, tf*idf);
            }else{
            	fv.put(termId, tf);
            }
        }
        return fv;
    }

    @Override
    public void deriveAndAddFeatures(Document doc) {
        doc.setFeatures(computeFeatures(doc.getText()));
    }

}
