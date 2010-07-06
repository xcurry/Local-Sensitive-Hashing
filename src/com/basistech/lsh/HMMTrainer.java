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
    private FSDParser parser = new NonwordSplitParser();
    private File currHMMFile = null;

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
            parcelDocuments();
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
    }

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
            oos.writeObject(docRep);
            idx++;
        }
        oos.flush();
        oos.close();
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
