package com.basistech.lsh;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\tkn_sgm",english);
        //docs.enqueueDir("/basis/users/cdoersch/data/tdt5/data/tkn_sgm",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        int nDocs = 278108;//docs.getDocCount();
        System.out.println("Found "+nDocs+" documents");
        docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.off_topic.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.off_topic.v2.0");
        
        ArrayList<Boolean> isnew_ground = new ArrayList<Boolean>();
        HashSet<String> labelset = new HashSet<String>();
        
        ArrayList<Double> nearestNeighbor = new ArrayList<Double>();
        nearestNeighbor.add(1.0);
        int dimension=13;
        int maxPerBucket = Math.max(2,(int)(.5*nDocs/Math.pow(2, dimension)));
        int nTables = (int)Math.ceil(
                               Math.log(.025)/
                      (Math.log(1-Math.pow(.8,(double)dimension/2))+
                              Math.log(1+Math.pow(.8,(double)dimension/2)))
            );
        
        //PetrovicLSH lsh = new PetrovicLSH(dimension, maxPerBucket, nTables,1000);
        ArrayList<Document> prevDocs = new ArrayList<Document>();
        Document firstDoc = docs.nextDoc(); 
        //lsh.add(firstDoc);
        prevDocs.add(firstDoc);
        
        isnew_ground.add(true);
        labelset.addAll(firstDoc.getTopics());
        File f = new File("distances.log");
        try{
            PrintStream fw=new PrintStream(f);
            for(int i=1; i<nDocs; i++){
                if(i%1000==0){
                    System.out.println("Processing document "+i);
                }
                Document currDoc = docs.nextDoc();
                if(currDoc.getTopics().size()==0){
                    continue;
                }
                //if(i%3==0){
                //    continue;
                //}
                //ResultSet<Document> res = lsh.search(currDoc, 1);
                ResultPair<Document> bestDoc = new ResultPair<Document>(null,Double.NEGATIVE_INFINITY);
                int z = 0;
                for(Document d:prevDocs){
                    double score=CosineSimilarity.value(d.getFeatures(),currDoc.getFeatures());
                    if(score>bestDoc.score){
                        bestDoc = new ResultPair<Document>(d,score);
                    }
                }
                //List<ResultPair<Document>> resultList = res.popResults();
                
                nearestNeighbor.add(1-bestDoc.score);
                if(bestDoc.score>.9){
                    fw.println("------currDoc------"+currDoc.getText()+"\n\n");
                    fw.println("------bestDoc------"+bestDoc.result.getText()+"\n\n");
                    fw.println("Cosine Similarity:"+CosineSimilarity.value(currDoc.getFeatures(), bestDoc.result.getFeatures()));
                }
                isnew_ground.add(!labelset.containsAll(currDoc.getTopics()));
                labelset.addAll(currDoc.getTopics());
                
                if(isnew_ground.get(nearestNeighbor.size()-1)){
                    fw.println("new:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }else{
                    fw.println("old:"+nearestNeighbor.get(nearestNeighbor.size()-1));
                }
                prevDocs.add(currDoc);
                
            }
        }catch(IOException e){throw new RuntimeException(e);}
        System.out.println(isnew_ground.size()+" Documents added to LSH");
        
        PRPlot plot = new PRPlot(isnew_ground,nearestNeighbor);
    }

}
