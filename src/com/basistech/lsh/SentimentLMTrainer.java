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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.io.File;

/**
 *
 * @author cdoersch
 */
public class SentimentLMTrainer {
    public static final String SENTIMENT_POSITIVE="SentimentLMTrainer.SENTIMENT_POSITIVE";
    public static final String SENTIMENT_NEGATIVE="SentimentLMTrainer.SENTIMENT_NEGATIVE";
    public static AROW getTrainedAROW(Featurizer feat){
        RottentomatoesDocStore docStore = new RottentomatoesDocStore(new File(ComputeEnvironment.getDataDirectory(),"rottentomatoes"));
        return getTrainedAROW(docStore,feat);
    }

    /**
     * This method never uses reset() on the docstore, so documents already iterated
     * from the docstore won't be used for training (useful for train/test sets)
     */
    public static AROW getTrainedAROW(DocStore docStore, Featurizer feat){
        AROW arow = new AROW();
        train(arow,docStore,feat);
        return arow;
    }
    
    public static NaiveBayes getTrainedNaiveBayes(DocStore docStore, Featurizer feat){
        NaiveBayes nb = new NaiveBayes();
        train(nb,docStore,feat);
        return nb;
    }

    public static AvgPerceptron getTrainedAP(DocStore docStore, Featurizer feat){
        AvgPerceptron ap = new AvgPerceptron();
        train(ap,docStore,feat);
        return ap;
    }

    public static void train(OnlineLearner learner, DocStore docStore, Featurizer feat){
        Document doc;
        while((doc = docStore.nextDoc())!=null){
            feat.deriveAndAddFeatures(doc);
            int label=readLabel(doc);
            learner.train(doc.getFeatures(), label);
        }
        learner.finish();
    }

    private static int readLabel(Document doc){
        return doc.getAnnotations().get(0).equals(SENTIMENT_POSITIVE)?1:0;
    }

    public static void rottenTomatoes(){
        TFIDF2 f = new TFIDF2();
        f.setGiveProportions(false);
        RottentomatoesDocStore docStore = new RottentomatoesDocStore(new File(ComputeEnvironment.getDataDirectory(),"rottentomatoes"));
        Document[] testset = new Document[1000];
        for(int i = 0; i<1000; i++){
            testset[i]=docStore.nextDoc();
            f.deriveAndAddFeatures(testset[i]);
        }
        OnlineLearner learner = getTrainedAP(docStore,f);
        int acc = 0;
        for(int i = 0; i<1000; i++){
            if(learner.predict(testset[i].getFeatures())==readLabel(testset[i])){
                acc++;
            }
        }
        System.out.println("Accuracy:"+acc/1000.0d);
        System.out.println(learner.print(f.getVocabulary()));
    }

    public static void amazon(){
        File lmdir = new File(ComputeEnvironment.getDataDirectory(),"processed_lm");
        MDSDReader trainDocs = new MDSDReader(new File(lmdir,"books.shfld.mdsd.train"));
        MDSDReader testDocs = new MDSDReader(new File(lmdir,"books.shfld.mdsd.test"),
                trainDocs.getFeaturizer().getVocabulary());
        Featurizer f = trainDocs.getFeaturizer();
        OnlineLearner learner = getTrainedNaiveBayes(trainDocs,f);
        int acc = 0;
        int count = 0;
        Document doc;
        while((doc=testDocs.nextDoc())!=null){
            if(learner.predict(doc.getFeatures())==readLabel(doc)){
                acc++;
            }
            if(count%10==0){
                System.out.println(count);
            }
            count++;
        }
        System.out.println("Accuracy:"+acc/(double)count);
        //System.out.println(learner.print(f.getVocabulary()));
    }

    public static void main(String[] args){
        amazon();
    }
}
