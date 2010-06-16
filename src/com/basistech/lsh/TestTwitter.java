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
    private static int recordingPeriod=100000;
    public static void main(String[] args) {
        
        File logFile = new File("/u1/fsd/data/twitter/threads2.log");
        boolean printThreadMembership=true;
        boolean printSummary=false;
        
        TwitterDocStore docs = new TwitterDocStore();
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\tmp",english);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\twitter\\split",null);
        //docs.enqueueDir("/Users/jwp/dev/data/twitter/split",null);
        docs.enqueueDir("/u1/fsd/data/twitter/split",null);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        //nDocs only affects the sizes of the buckets in the LSH.
        int nDocs = 96378557;//docs.getDocCount();
        
        System.out.println("Found "+nDocs+" documents");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.off_topic.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.off_topic.v2.0");
        
        int tweetsToProcess=nDocs;//5000000;
        
        
        int dimension=13;
        int maxPerBucket = Math.max(2,(int)(.001*nDocs/Math.pow(2, dimension)));
        int nTables = (int)Math.ceil(
                               Math.log(.025)/
                      (Math.log(1-Math.pow(.8,(double)dimension/2))+
                              Math.log(1+Math.pow(.8,(double)dimension/2)))
            );
        System.out.println("bucket sz: " + maxPerBucket);
        System.out.println("n tables: " + nTables);
        PetrovicLSH lsh = new PetrovicLSH(dimension, maxPerBucket, nTables,2000);
        
        LinkedList<TThread> recentThreads = new LinkedList<TThread>();
        ResultSet<TThread> fastestThreads = new ResultSet<TThread>(20);
        ArrayList<Tweet> annotatedDocs = new ArrayList<Tweet>();
        try{
            //create log file
            PrintStream fw=new PrintStream(logFile);
            Document currDoc;
            int docNo=0;
            //loop through every tweet we have on file; currDoc is the current one.
            while((currDoc = docs.nextDoc())!=null){
                
                if(docNo%10000==0){
                    System.out.println("Processing document "+docNo);
                }
                
                //don't process every document--there's too many
                //note that we may run out of tweets before docNo reaches 
                //nDocs, because not all tweets in the files are actually returned.
                if(docNo>=tweetsToProcess){
                    break;
                }
                
                //use the lsh to find documents similar to currDoc 
                ResultSet<Document> res = lsh.search(currDoc, 1);
                List<ResultPair<Document>> resultList = res.popResults();
                TThread toAddTo;
                
                //if there were collisions in the LSH
                if(resultList.size()>0){
                    ResultPair<Document> bestDoc=resultList.get(0);
                    if(bestDoc.score>.500001){
                        //Now we're certain we have found a document with high cosine similarity. 
                        //Its thread is the one we'll modify
                        toAddTo=((Tweet)bestDoc.result).getTThread();
                        
                        //fw.println("old:"+bestDoc.score);
                        //if(bestDoc.score>.9){
                        //    fw.println("------currDoc------\n"+currDoc.getText()+"\n\n");
                        //    fw.println("------bestDoc------\n"+bestDoc.result.getText()+"\n\n");
                        //    fw.println("Cosine Similarity:"+CosineSimilarity.value(currDoc.getFeatures(), bestDoc.result.getFeatures()));
                        //}
                    }else{
                        //we found other tweets, but they were too far in cosine distance.
                        //we'll start a new thread
                        //fw.println("new:"+bestDoc.score);
                        toAddTo=new TThread(docNo);
                    }
                }else{
                    //we didn't collide with any documents, start a new thread
                    //fw.println("new:N/A");
                    toAddTo=new TThread(docNo);
                }
                ((Tweet)currDoc).setTThread(toAddTo);
                
                //we only keep track of the posts that were made during the
                //thread's recording period, because at the end of the day
                //we care only about the thread's growth near the beginning
                //of its existence.
                if(docNo-toAddTo.getStartTweet()<=recordingPeriod){
                    toAddTo.addTweet((Tweet)currDoc);
                }
                
                //we won't actually know whether to add this thread to the result
                //set until after the thread's recording period is over.  THerefore, we
                //add it to a queue to be processed later.
                recentThreads.offer(toAddTo);
                
                //if there's threads added to recentThreads whose recordingPeriod's
                //are guaranteed to be over (they've been in there for recordingPeriod
                //posts), then add them to the results if they're interesting enough
                if(recentThreads.size()>recordingPeriod){
                    TThread judged = recentThreads.poll();
                    addThreadIfEvent(judged,fastestThreads);
                }
                
                lsh.add(currDoc);
                
                if(currDoc.getAnnotations().size()>0){
                    annotatedDocs.add((Tweet)currDoc);
                }
                
                if(printThreadMembership){
                    fw.println(currDoc);
                }
                docNo++;
                
            }
        
            
            //we're done with all the documents we're going to process.
            //Go through the threads that we haven't yet had a chance
            //to add to the final ResultSet.
            while(recentThreads.size()>0){
                TThread judged = recentThreads.poll();
                addThreadIfEvent(judged,fastestThreads);
            }
            
            for(ResultPair<TThread> t: fastestThreads.popResults()){
                if(printSummary)
                    printThread(t.result,fw);
            }
            
        }catch(IOException e){throw new RuntimeException(e);}
        
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
    
    public static void printThread(TThread res, PrintStream writer){
        writer.println("----------Post Count:"+res.getCount()+"----------");
        int i = 0;
        for(Tweet t: res.getTweets()){
            writer.println(t.getText());
            i++;
            if(i==10){
                break;
            }
        }
    }
    
    public static void addThreadIfEvent(TThread t, ResultSet<TThread> fastestThreads){
        //System.out.println(t.getEntropy());
        if(t.getEntropy()>3.5 && t.getStartTweet()>recordingPeriod){
            fastestThreads.add(t,t.getCount());
        }
    }

}
