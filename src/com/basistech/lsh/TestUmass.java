package com.basistech.lsh;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class TestUmass {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        TDT5DocStore docs = new TDT5DocStore();
        FilenameFilter english = new FilenameFilter(){
            public boolean accept(File dir, String name){
                return name.contains("ENG");
            }
        };
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\tmp",english);
        docs.enqueueDir(ComputeEnvironment.getDataDirectory()+"/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("/home/cdoersch/data/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("/basis/users/cdoersch/data/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        docs.loadDocTopics(ComputeEnvironment.getDataDirectory()+"/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        docs.setAnnotatedDocsOnly(true);
        //docs.loadDocTopics("/home/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        int nDocs = docs.getDocCount();
        System.out.println("Found "+nDocs+" documents");
        TFIDF2 tfidf = new TFIDF2();
        tfidf.setGiveProportions(true);
        tfidf.setUseIDF(true);
        tfidf.trainIDF(docs);
        Featurizer feats = tfidf;

        docs.reset();
        
        ArrayList<Boolean> isnew_ground = new ArrayList<Boolean>();
        HashSet<String> labelset = new HashSet<String>();
        
        ArrayList<Double> nearestNeighbor = new ArrayList<Double>();
        nearestNeighbor.add(1.0);
        
        ArrayList<Document> prevDocs = new ArrayList<Document>();
        Document firstDoc = docs.nextDoc();
        feats.deriveAndAddFeatures(firstDoc);
        prevDocs.add(firstDoc);
        
        isnew_ground.add(true);
        labelset.addAll(firstDoc.getAnnotations());
        File f = new File("distances.log");
        try{
            PrintStream fw=new PrintStream(f);
            for(int i=1; i<nDocs; i++){
                if(i%1000==0){
                    System.out.println("Processing document "+i);
                }
                Document currDoc = docs.nextDoc();
                if(currDoc.getAnnotations().size()==0){
                    continue;
                }
                feats.deriveAndAddFeatures(currDoc);
                ResultPair<Document> bestDoc = new ResultPair<Document>(null,Double.NEGATIVE_INFINITY);
                for(Document d:prevDocs){
                    double score=CosineSimilarity.value(d.getFeatures(),currDoc.getFeatures());
                    if(score>bestDoc.score){
                        bestDoc = new ResultPair<Document>(d,score);
                    }
                }
                
                nearestNeighbor.add(1-bestDoc.score);
                if(bestDoc.score>.9){
                    fw.println("------currDoc------"+currDoc.getText()+"\n\n");
                    fw.println("------bestDoc------"+bestDoc.result.getText()+"\n\n");
                    fw.println("Cosine Similarity:"+CosineSimilarity.value(currDoc.getFeatures(), bestDoc.result.getFeatures()));
                }
                isnew_ground.add(!labelset.containsAll(currDoc.getAnnotations()));
                labelset.addAll(currDoc.getAnnotations());
                
                if(isnew_ground.get(nearestNeighbor.size()-1)){
                    fw.println("new:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }else{
                    fw.println("old:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }
                prevDocs.add(currDoc);
                
            }
        }catch(IOException e){throw new RuntimeException(e);}
        System.out.println(isnew_ground.size()+" Documents added to UMass");
        
        new PRPlot(isnew_ground,nearestNeighbor);
    }

}
