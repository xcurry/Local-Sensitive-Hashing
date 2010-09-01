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
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author cdoersch
 */
public class HMMTrainer {
    private int numThreads;
    private File saveDir;
    private DocStore docs;
    private boolean hasParceledDocuments;
    private String[] docParcels;
    private Vocabulary vocab = new Vocabulary();
    private FSDParser parser = new CommonWordRemovalParser();
    private File currHMMFile = null;
    private final IntCounter<Integer> counts = new IntCounter<Integer>();
    private Comparator countCompare = new Comparator<Integer>(){
        public int compare(Integer o1, Integer o2){
            return counts.getInt(o2)-counts.getInt(o1);
        }
    };
    private PriorityQueue<Integer> mostFrequentWords = new PriorityQueue<Integer>(11,countCompare);

    private HMM currHMM;

    public HMMTrainer(int numThreads){
        this.numThreads=numThreads;
        saveDir=ComputeEnvironment.getVarDirectory();
        currHMMFile=new File(saveDir,"curr.hmm");
        docParcels = new String[numThreads];
    }

    public void useCorpus(DocStore docs){
        this.docs=docs;
        hasParceledDocuments=false;
    }

    public HMMFeaturizer getTrainedHMM(int minIters){
        if(currHMM==null&&saveDir!=null){
            loadHMM();
        }
        if(!hasParceledDocuments){
            parcelDocuments();//even if we're not doing training, we still need
                              //to initialize the vocabulary.
        }
        if(currHMM==null){
            initializeHMM();
            saveHMM();
        }
        while(currHMM.getTrainingIterations()<minIters){
            doTrainingStep();
        }
        return new HMMFeaturizer(currHMM,vocab,parser);
    }

    private void loadHMM(){try{
        if(!currHMMFile.exists())
            return;
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(currHMMFile));
        currHMM = (HMM) ois.readObject();
    }catch(Exception e){throw new RuntimeException(e);}}

    private void saveHMM(){try{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(currHMMFile));
        oos.writeObject(currHMM);
        oos.flush();
        oos.close();
    }catch(Exception e){throw new RuntimeException(e);}}

    private void initializeHMM(){
        currHMM = new HMM(100,vocab.size());
        currHMM.bumpStates(mostFrequentWords);
    }

    // for parallelization of E step of EM
    private void parcelDocuments(){try{
        Document doc;
        int nDocs=docs.getDocCount();
        int idx = 0;
        ObjectOutputStream oos = null;
        System.out.println("HMMTrainer: ndocs="+nDocs);
        while((doc=docs.nextDoc())!=null){
            int parcel = idx*numThreads/nDocs;
            if(docParcels[parcel]==null){
                if(oos!=null){
                    oos.flush();
                    oos.close();
                }
                File newFile = new File(saveDir,"docparcel"+parcel);
                docParcels[parcel]=newFile.getAbsolutePath();
                oos=new ObjectOutputStream(new FileOutputStream(newFile));
            }
            int[] docRep = Featurizer.stringToInt(doc.getText(), parser, vocab);
            for(int j=0; j<docRep.length; j++){
                this.counts.increment(IntegerCache.get(docRep[j]));
            }
            oos.writeObject(docRep);
            idx++;
        }
        oos.flush();
        oos.close();
        for(Integer i: counts.keySet()){
            mostFrequentWords.offer(i);
        }
        System.out.println("HMMTrainer: idx="+idx);
        hasParceledDocuments=true;
    }catch(Exception e){throw new RuntimeException(e);}}

    public void doTrainingStep(){try{
        for(int parcel=0; parcel<numThreads; parcel++){
            File dest_file = new File(saveDir,"estep_"+parcel+"_final");
            if(dest_file.exists())
                dest_file.delete();
        }
        for(int parcel=0; parcel<numThreads-1; parcel++){
            File scriptFile=File.createTempFile("worker_thread", ".sh");
            PrintStream script = new PrintStream(scriptFile);
            // write a script file for sge 
            script.println("#!/bin/bash");
            script.println("#$ -l mem=4G");
            script.println(ComputeEnvironment.getJavaHome()+"/bin/java -jar "+
                    ComputeEnvironment.getExecutableLocation().getAbsolutePath()+
                    " ParallelEMWorker " +
                    parcel +
                    " \""+currHMMFile.getAbsolutePath()+"\" " +
                    "\""+docParcels[parcel]+"\" "+
                    "\""+saveDir.getAbsolutePath()+"\"");
            script.close();
            System.out.println("/opt/sge/bin/lx24-amd64/qsub "+scriptFile.getAbsolutePath());
            // sumbit this job to the queue run e.g., from cn0
            Runtime.getRuntime().exec("/opt/sge/bin/lx24-amd64/qsub "+scriptFile.getAbsolutePath());
        }
        //TODO: it should be possible to pass the needed information directly,
        //without screwing around with files.  This will mean no disk when we
        //have just one thread.
        ParallelEMWorker.main(new String[]{
            String.valueOf(docParcels.length-1),
            currHMMFile.getAbsolutePath(),
            docParcels[docParcels.length-1],
            saveDir.getAbsolutePath()
        });
        currHMM.resetAccumulators();
        // poll until the E step finishes; using presence of file
        for(int parcel=0; parcel<numThreads; parcel++){
            File dest_file = new File(saveDir,"estep_"+parcel+"_final");
            while(!dest_file.exists()){
                Thread.sleep(100);
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dest_file));
            HMM other = (HMM)ois.readObject();
            currHMM.mergeAccumulators(other);
        }
        currHMM.MStep();
        
        if(saveDir!=null){
            saveHMM();
        }
    }catch(Exception e){throw new RuntimeException(e);}}
}
