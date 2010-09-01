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
