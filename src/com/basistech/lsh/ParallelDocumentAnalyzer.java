/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 *
 * @author cdoersch
 */
public class ParallelDocumentAnalyzer {
    private PetrovicLSH lsh;
    private DocStore docs;
    private int processorThreads;
    private int nullCount = 0;

    //stupid ArrayBlockingQueue doesn't accept nulls...
    private static Document nullDoc = new Document(){
        @Override public List<String> getAnnotations() {return null;}
        @Override public FeatureVector getFeatures() {return null;}
        @Override public int[] getHash() {return null;}
        @Override public String getText() {return null;}
        @Override public void setFeatures(FeatureVector fv) {}
        @Override public void setHash(int[] hash) {}
    };

    private ArrayBlockingQueue<DocPallet> in = new ArrayBlockingQueue<DocPallet>(50);
    private ArrayBlockingQueue<DocPallet> out = new ArrayBlockingQueue<DocPallet>(50);

    public ParallelDocumentAnalyzer(PetrovicLSH lsh, Featurizer feat, DocStore docs, int processorThreads) {
        this.lsh = lsh;
        this.docs = docs;
        this.processorThreads=processorThreads;

        feat.getVocabulary().makeThreadSafe();

        new Thread(new docReader()).start();

        for(int i = 0; i<processorThreads; i++){
            new Thread(new docProcessor(feat.clone())).start();
        }
    }

    private static class DocPallet{
        private Document doc;
        private Semaphore done;
        private DocPallet(Document doc){
            this.doc = doc;
            done=new Semaphore(0);
        }
    }

    private class docReader implements Runnable{
        @Override
        public void run() {try{
            Document doc;
            while((doc=docs.nextDoc())!=null){
                DocPallet pal = new DocPallet(doc);
                in.put(pal);
                //note: it can't be the worker threads putting the finished docs
                //into out because the worker threads might reorder them.
                out.put(pal);
            }
            for(int i = 0; i<processorThreads; i++){
                DocPallet pal = new DocPallet(nullDoc);
                in.put(pal);
                out.put(pal);
            }
        }catch(Exception e){throw new RuntimeException(e);}}
    }

    private class docProcessor implements Runnable{
        private Featurizer localFeat;
        private docProcessor(Featurizer localFeat){
            this.localFeat=localFeat;
        }
        @Override
        public void run() {try{
            DocPallet docp;
            while((docp=in.take()).doc!=nullDoc){
                localFeat.deriveAndAddFeatures(docp.doc);
                lsh.deriveAndAddHash(docp.doc);
                docp.doc.getFeatures().getNorm();
                docp.done.release();
            }
        }catch(Exception e){throw new RuntimeException(e);}}
    }

    public Document nextDoc(){try{
        if(nullCount==processorThreads){
            return null;
        }
        DocPallet docp;
        while((docp = out.take()).doc==nullDoc){
            nullCount++;
            if(nullCount==processorThreads){
                return null;
            }
        }
        docp.done.acquire();
        return docp.doc;
    }catch(InterruptedException e){throw new RuntimeException(e);}}
}
