package com.basistech.lsh;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestTwitter {

    /**
     * @param args
     */
    private static int recordingPeriod=10000;//100000;
    public static void main(String[] args) {
        
        TwitterDocStore docs = new TwitterDocStore();
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\tmp",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\twitter\\split",null);
        docs.enqueueDir("/Users/jwp/dev/data/twitter/split",null);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        //int nDocs = 96378557;//docs.getDocCount();
        int nDocs = 3603638; // just split/1
        
        
        System.out.println("Found "+nDocs+" documents");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.off_topic.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.off_topic.v2.0");
        
        //ArrayList<Boolean> isnew_ground = new ArrayList<Boolean>();
        //HashSet<String> labelset = new HashSet<String>();
        
        //ArrayList<Double> nearestNeighbor = new ArrayList<Double>();
        //nearestNeighbor.add(1.0);
        int dimension=13;
        int maxPerBucket = Math.max(2,(int)(.1*nDocs/Math.pow(2, dimension)));
        int nTables = (int)Math.ceil(
                               Math.log(.025)/
                      (Math.log(1-Math.pow(.8,(double)dimension/2))+
                              Math.log(1+Math.pow(.8,(double)dimension/2)))
            );
        System.out.println("bucket sz: " + maxPerBucket);
        System.out.println("n tables: " + nTables);
        PetrovicLSH lsh = new PetrovicLSH(dimension, maxPerBucket, nTables,2000);
        //Document firstDoc = docs.nextDoc(); 
        //lsh.add(firstDoc);
        
        //isnew_ground.add(true);
        //labelset.addAll(firstDoc.getTopics());
        File f = new File("distances.log");
        LinkedList<TThread> recentThreads = new LinkedList<TThread>();
        ResultSet<TThread> fastestThreads = new ResultSet<TThread>(20);
        ArrayList<Tweet> annotatedDocs = new ArrayList<Tweet>();
        try{
            PrintStream fw=new PrintStream(f);
            for(int i=1; i<nDocs; i++){
                if(i%10000==0){
                    System.out.println("Processing document "+i);
                }
                Document currDoc = docs.nextDoc();
                
                if(i>=30000){
                    break;
                }
                ResultSet<Document> res = lsh.search(currDoc, 1);
                List<ResultPair<Document>> resultList = res.popResults();
                TThread added;
                if(resultList.size()>0){
                    ResultPair<Document> bestDoc=resultList.get(0);
                    if(bestDoc.score>.500001){
                        added=((Tweet)bestDoc.result).getTThread();
                        fw.println("old:"+bestDoc.score);
                        if(bestDoc.score>.9){
                            fw.println("------currDoc------\n"+currDoc.getText()+"\n\n");
                            fw.println("------bestDoc------\n"+bestDoc.result.getText()+"\n\n");
                            fw.println("Cosine Similarity:"+CosineSimilarity.value(currDoc.getFeatures(), bestDoc.result.getFeatures()));
                        }
                    }else{
                        fw.println("new:"+bestDoc.score);
                        added=new TThread(i);
                    }
                }else{
                    fw.println("new:N/A");
                    added=new TThread(i);
                }
                ((Tweet)currDoc).setTThread(added);
                if(i-added.getStartTweet()<=recordingPeriod){
                    added.addTweet((Tweet)currDoc);
                }
                
                recentThreads.offer(added);
                if(recentThreads.size()>recordingPeriod){
                    TThread judged = recentThreads.poll();
                    addThread(judged,fastestThreads);
                }
                
                lsh.add(currDoc);
                
                if(currDoc.getAnnotations().size()>0){
                    annotatedDocs.add((Tweet)currDoc);
                }
                
            }
        }catch(IOException e){throw new RuntimeException(e);}
        
        while(recentThreads.size()>0){
            TThread judged = recentThreads.poll();
            addThread(judged,fastestThreads);
        }
        
        for(ResultPair<TThread> t: fastestThreads.popResults()){
            printThread(t.result);
        }
        
        Collections.sort(annotatedDocs);
        
        double average_precision=0.0;
        int event_docs=0;
        int total_docs=0;
        for(int i=0; i<annotatedDocs.size(); i++){
            total_docs++;
            if(is_event(annotatedDocs.get(i))){
                event_docs++;
                average_precision+=event_docs/(double)total_docs;
            }
        }
        average_precision/=event_docs;
        System.out.println("Average Precision:"+average_precision);
        
        System.exit(0);
        
    }
        
    public static boolean is_event(Tweet t){
        return t.getAnnotations().get(0).equals("Event");
    }
    
    public static void printThread(TThread res){
        System.out.println("----------Post Count:"+res.getCount()+"----------");
        int i = 0;
        for(Tweet t: res.getTweets()){
            System.out.println(t.getText());
            i++;
            if(i==10){
                break;
            }
        }
    }
    
    public static void addThread(TThread t, ResultSet<TThread> fastestThreads){
        //System.out.println(t.getEntropy());
        if(t.getEntropy()>3.5 && t.getStartTweet()>recordingPeriod){
            fastestThreads.add(t,t.getCount());
        }
    }

}
