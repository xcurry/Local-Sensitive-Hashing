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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestTDT5 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("TestTDT5, compiled on: "+ComputeEnvironment.getCompilationDate());

        long start = System.currentTimeMillis();
        
        TDT5DocStore docs = new TDT5DocStore();
        FilenameFilter english = new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                return name.contains("ENG");
            }
        };
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\tmp",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\tkn_sgm",english);
        docs.enqueueDir(ComputeEnvironment.getDataDirectory()+"/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("/home/cdoersch/data/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("/basis/users/cdoersch/data/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        docs.loadDocTopics(ComputeEnvironment.getDataDirectory()+"/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.setUnAnnotatedDocsOnly(true);
        docs.setUnAnnotatedDocsOnly(true);
        //docs.setDocCount(6366);//TDT5 annotated set
        //docs.loadDocTopics("/home/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        int nDocs = docs.getDocCount();
        System.out.println("Found "+nDocs+" documents");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.off_topic.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.off_topic.v2.0");

        HMMTrainer trainer;
        if(ComputeEnvironment.isCluster()){
            trainer = new HMMTrainer(48);
        }else{
            trainer = new HMMTrainer(1);
        }
        trainer.useCorpus(docs);
        HMMFeaturizer hmm = trainer.getTrainedHMM(80);
        Featurizer feats = hmm;
        //TFIDF2 tfidf = new TFIDF2();
        //tfidf.setGiveProportions(true);
        //tfidf.setUseIDF(true);
        //tfidf.trainIDF(docs);
        //Featurizer feats = tfidf;
        
        docs.reset();
        docs.setAnnotatedDocsOnly(true);
        
        ArrayList<Boolean> isnew_ground = new ArrayList<Boolean>();
        HashSet<String> labelset = new HashSet<String>();
        
        ArrayList<Double> nearestNeighbor = new ArrayList<Double>();
        int dimension=13;
        int nDocs2=270000;
        int maxPerBucket = Math.max(2,(int)(.5*nDocs2/Math.pow(2, dimension)));
        Integer cap=ComputeEnvironment.getBucketSizeCap();
        if(cap!=null&&maxPerBucket>cap){
            maxPerBucket = cap;
        }
        double cosSim=.8;
        double pColl=1. - Math.acos(cosSim)/Math.PI;
        int nTables = (int)Math.ceil(
                               Math.log(.025)/
                      (Math.log(1-Math.pow(pColl,(double)dimension)))
            );
        System.out.println(nTables);
        PetrovicLSH lsh = new PetrovicLSH(dimension, maxPerBucket, nTables,1000);
        //to run the experiment with 7 dimensions, replace the above line with this one:
        //PetrovicLSH lsh = new PetrovicLSH(7, maxPerBucket, nTables,0);
        ParallelDocumentAnalyzer pda = new ParallelDocumentAnalyzer(lsh, feats, docs, 6);
        
        File f = new File(ComputeEnvironment.getVarDirectory(),"distances.log");
        try{
            PrintStream fw=new PrintStream(f);
            Document currDoc;
            int i = 0;
            while((currDoc=pda.nextDoc())!=null){
                if(i%10==0){
                    System.out.println("Processing document "+i);
                    System.out.flush();
                }
                //if(i==1000){
                //    double[] a = new double[2000000000];
                //}
                //if you disable the document analyzer, you need to un-comment this line.
                //feats.deriveAndAddFeatures(currDoc);
                ResultSet<Document> res = lsh.search(currDoc, 1);
                List<ResultPair<Document>> resultList = res.popResults();
                if(resultList.size()>0){
                    ResultPair<Document> bestDoc=resultList.get(0);
                    nearestNeighbor.add(1-bestDoc.score);
                    if(bestDoc.score>.9){
                        fw.println("------currDoc------"+currDoc.getText()+"\n\n");
                        fw.println("------bestDoc------"+bestDoc.result.getText()+"\n\n");
                        fw.println("Cosine Similarity:"+CosineSimilarity.value(currDoc.getFeatures(), bestDoc.result.getFeatures()));
                    }
                }else{
                    nearestNeighbor.add(1.0);
                }
                
                isnew_ground.add(!labelset.containsAll(currDoc.getAnnotations()));
                labelset.addAll(currDoc.getAnnotations());
                
                if(isnew_ground.get(nearestNeighbor.size()-1)){
                    fw.println("new:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }else{
                    fw.println("old:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }
                lsh.add(currDoc);
                i++;
            }
        }catch(IOException e){throw new RuntimeException(e);}
        System.out.println(isnew_ground.size()+" Documents added to LSH");
        
        PRPlot.writeChart(isnew_ground,nearestNeighbor,
                new File(ComputeEnvironment.getVarDirectory(),"perf_chart.png").getAbsolutePath());

        long end = System.currentTimeMillis();

        long diff = end-start;
        System.out.println("time: " + diff/3600000+ "h "+(diff%3600000)/60000 + "m "+(diff%60000)/1000 +"s");

        System.exit(0);
    }

}
