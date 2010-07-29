/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;

/**
 *
 * @author cdoersch
 */
public class AnalyzeHMM {
    public static void main(String[] args) throws Exception{
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("/home/cdoersch/Desktop/curr.hmm"));
        HMM hmm = (HMM)ois.readObject();
        System.out.println("HMM training iterations:"+hmm.getTrainingIterations());
        System.out.println(hmm.printStatePopularity());
        TDT5DocStore docs = new TDT5DocStore();
        FilenameFilter english = new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                return name.contains("ENG");
            }
        };
        docs.enqueueDir(ComputeEnvironment.getDataDirectory()+"/tdt5/data/tkn_sgm",english);
        docs.loadDocTopics(ComputeEnvironment.getDataDirectory()+"/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        docs.setUnAnnotatedDocsOnly(true);
        Vocabulary v = new Vocabulary();
        FSDParser p = new CommonWordRemovalParser();
        Document doc;
        int i = 0;
        while((doc = docs.nextDoc())!=null){
            i++;
            if(i%1000==0){
                System.out.println(i);
            }
            if(i%10000==0){
                System.gc();
            }
            Featurizer.stringToInt(doc.getText(), p, v);
        }
        System.gc();
        //hmm.setVocab(v);
        //HMMFeaturizer feat = new HMMFeaturizer(hmm,v,p);
        //docs.reset();
        //feat.deriveAndAddFeatures(docs.nextDoc());
        String str=hmm.printStatesCompact(v);
        System.out.println(str);
        System.out.flush();
        new Thread(new Runnable(){
            @Override
            public void run(){try{
                Thread.sleep(20000);
            }catch(Exception e){}}
        }).start();
    }
}
