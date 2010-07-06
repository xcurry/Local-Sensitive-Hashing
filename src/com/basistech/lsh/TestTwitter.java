package com.basistech.lsh;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestTwitter {

    /**
     * @param args
     */
    public static void main(String[] args) {
        double threshold = Double.valueOf(args[0]);
        System.out.println("TestTwitter, compiled on: "+ComputeEnvironment.getCompilationDate());
        
        String logFileName = ComputeEnvironment.getDataDirectory()+"/twitter/threads2"+threshold+".log";
        //String logFileName = "/home/cdoersch/threads.log";
        boolean printThreadMembership=true;
        boolean printSummary=false;
        
        TwitterDocStore docs = new TwitterDocStore();
        //docs.addDirToIDF("/home/cdoersch/data/twitter/idf",null);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\tmp",english);
        //docs.enqueueDir("/home/cdoersch/data/twitter/split",null);
        //docs.enqueueDir("/Users/jwp/dev/data/twitter/split",null);
        docs.enqueueDir(ComputeEnvironment.getDataDirectory()+"/twitter/split",null);
        //docs.enqueueDir("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\data\\mttkn_sgm",english);
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.topic_rel.v2.0");
        //nDocs only affects the sizes of the buckets in the LSH.
        int nDocs = 96378531;//docs.getDocCount();
        
        System.out.println("Found "+nDocs+" documents");
        //docs.loadDocTopics("C:\\cygwin\\home\\cdoersch\\data\\tdt5\\LDC2006T19\\tdt5_topic_annot\\data\\annotations\\topic_relevance\\TDT2004.off_topic.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        //docs.loadDocTopics("/basis/users/cdoersch/data/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.off_topic.v2.0");
        
        int tweetsToProcess=nDocs;//
        
        //how far do we look when merging threads?
        int neighborhoodSize=100;
        //double threshold=.500001;
        //how long do we wait before deciding a thread won't be merged again?
        int mergeWait=1000000;

        TFIDF2 featurizer = new TFIDF2();
        featurizer.setUseIDF(true);
        TwitterDocStore idftrain = new TwitterDocStore();
        idftrain.enqueueDir(ComputeEnvironment.getDataDirectory()+"/twitter/idf",null);
        //idftrain.enqueueDir("/home/cdoersch/data/twitter/idf",null);
        featurizer.trainIDF(idftrain);
        
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
        
        LinkedList<Tweet> recentThreads = new LinkedList<Tweet>();
        LinkedList<Tweet> kindaRecentThreads = new LinkedList<Tweet>();
        ResultSet<TThread> fastestThreads = new ResultSet<TThread>(20);
        ArrayList<Tweet> annotatedDocs = new ArrayList<Tweet>();
        try{
            //create log file
            BufferedWriter fw = 
                new BufferedWriter(new OutputStreamWriter
                        (new FileOutputStream(logFileName), "UTF-8"));
            //PrintStream fw=System.out;//new PrintStream(logFile);
            //if(false){throw new IOException();}
            Tweet currDoc;
            //loop through every tweet we have on file; currDoc is the current one.
            while((currDoc = docs.nextDoc())!=null){
                
                if(currDoc.getUid()%10000==0){
                    System.out.println("Processing document "+currDoc.getUid());
                    System.out.flush();
                }
                
                //don't process every document--there's too many
                //note that we may run out of tweets before docNo reaches 
                //nDocs, because not all tweets in the files are actually returned.
                if(currDoc.getUid()>=tweetsToProcess){
                    break;
                }
               
                featurizer.deriveAndAddFeatures(currDoc);
                
                //use the lsh to find documents similar to currDoc 
                ResultSet<Document> res = lsh.search(currDoc, neighborhoodSize);
                List<ResultPair<Document>> resultList = res.popResults();
                TThread toAddTo;
                boolean toAddToIsOld=false;
                
                //if there were collisions in the LSH
                if(resultList.size()>0){
                    ResultPair<Document> bestDoc=resultList.get(0);
                    if(bestDoc.score>threshold){
                        //Now we're certain we have found a document with high cosine similarity. 
                        //Its thread is the one we'll modify
                        toAddTo=((Tweet)bestDoc.result).getTThread();
                        toAddToIsOld=true;
                        
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
                        toAddTo=new TThread(currDoc.getUid());
                    }
                }else{
                    //we didn't collide with any documents, start a new thread
                    //fw.println("new:N/A");
                    toAddTo=new TThread(currDoc.getUid());
                }
                ((Tweet)currDoc).setTThread(toAddTo);
                
                //we only keep track of the posts that were made during the
                //thread's recording period, because at the end of the day
                //we care only about the thread's growth near the beginning
                //of its existence.
                if(currDoc.getUid()-toAddTo.getStartTweet()<=TThread.recordingPeriod){
                    toAddTo.addTweet((Tweet)currDoc);
                }
                
                //if currDoc is close to documents in other threads, merge 
                //the threads.  Note that this may change the "growth rate"
                //of a thread even after its TThread.recordingPeriod is over--that's
                //why we add everything to the oldest version of the thread,
                //and then re-add the oldest version to recentThreads (described
                //next)
                if(toAddToIsOld){
                    for(int i=1; i<resultList.size(); i++){
                        if(resultList.get(i).score<threshold){
                            break;
                        }
                        TThread other = ((Tweet)resultList.get(i).result).getTThread();
                        if(other==toAddTo){
                            continue;
                        }
                        if(other.getStartTweet()<toAddTo.getStartTweet()){
                            TThread tmp=other;
                            other=toAddTo;
                            toAddTo=tmp;
                        }
                        if(other.getStartTweet()<=toAddTo.getStartTweet()+TThread.recordingPeriod)
                            toAddTo.absorb(other);
                    }
                }
                
                //we won't actually know whether to add this thread to the result
                //set until after the thread's recording period is over.  THerefore, we
                //add it to a queue to be processed later.
                recentThreads.offer(currDoc);
                
                //if there's threads added to recentThreads whose TThread.recordingPeriod's
                //are guaranteed to be over (they've been in there for TThread.recordingPeriod
                //posts), then add them to the results if they're interesting enough
                if(recentThreads.size()>TThread.recordingPeriod){
                    Tweet judged = recentThreads.poll();
                    addThreadIfEvent(judged.getTThread(),fastestThreads);
                    if(printThreadMembership){
                        kindaRecentThreads.offer(judged);
                    }
                }
                if(kindaRecentThreads.size()>mergeWait){
                    Tweet printed = kindaRecentThreads.poll();
                    fw.write(printed.toString() + "\n");
                    if(currDoc.getUid()%10000==0){
                        fw.flush();
                    }
                }

                
                lsh.add(currDoc);
                
                if(currDoc.getAnnotations().size()>0){
                    annotatedDocs.add((Tweet)currDoc);
                }
                
            }
        
            
            //we're done with all the documents we're going to process.
            //Go through the threads that we haven't yet had a chance
            //to add to the final ResultSet.
            while(recentThreads.size()>0){
                Tweet judged = recentThreads.poll();
                addThreadIfEvent(judged.getTThread(),fastestThreads);
                if(printThreadMembership){
                    kindaRecentThreads.offer(judged);
                }
            }
            
            //and finish writing the thread memberships
            while(kindaRecentThreads.size()>0){
                Tweet printed = kindaRecentThreads.poll();
                fw.write(printed.toString() + "\n");
            }
            
            for(ResultPair<TThread> t: fastestThreads.popResults()){
                if(printSummary)
                    printThread(t.result,fw);
            }
            fw.flush();
            fw.close();
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
    
    public static void printThread(TThread res, BufferedWriter writer) throws IOException{
        writer.write("----------Post Count:"+res.getCount()+"----------\n");
        int i = 0;
        for(Tweet t: res.getTweets()){
            writer.write(t.getText() + "\n");
            i++;
            if(i==20){
                break;
            }
        }
    }
    
    public static void addThreadIfEvent(TThread t, ResultSet<TThread> fastestThreads){
        //System.out.println(t.getEntropy());
        t=t.getRoot();
        if(t.getStartTweet()>TThread.recordingPeriod && t.getEntropy()>3.5){
            fastestThreads.add(t,t.getCount());
        }
    }

}
