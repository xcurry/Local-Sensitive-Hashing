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
        TDT5DocStore docs = new TDT5DocStore();
        FilenameFilter english = new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                return name.contains("ENG");
            }
        };
        docs.enqueueDir(ComputeEnvironment.getDataDirectory()+"/tdt5/data/tkn_sgm",english);
        docs.loadDocTopics(ComputeEnvironment.getDataDirectory()+"/tdt5/LDC2006T19/tdt5_topic_annot/data/annotations/topic_relevance/TDT2004.topic_rel.v2.0");
        docs.setAnnotatedDocsOnly(true);
        Vocabulary v = new Vocabulary();
        FSDParser p = new NonwordSplitParser();
        Document doc;
        while((doc = docs.nextDoc())!=null){
            Featurizer.stringToInt(doc.getText(), p, v);
        }
        //hmm.setVocab(v);
        HMMFeaturizer feat = new HMMFeaturizer(hmm,v,p);
        docs.reset();
        feat.deriveAndAddFeatures(docs.nextDoc());
        System.out.println(hmm.printStatesCompact(v));
    }
}
