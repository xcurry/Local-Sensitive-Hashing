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
                //note: it can't be the worker threads putting the finished docs
                //into out because the worker threads might reorder them.
                out.put(pal);
                //System.out.println("outsize,docReader:"+out.size());
                in.put(pal);
                //System.out.println("insize,docReader:"+in.size());
            }
            for(int i = 0; i<processorThreads; i++){
                DocPallet pal = new DocPallet(nullDoc);
                out.put(pal);
                in.put(pal);
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
                //System.out.println("insize, docProcessor:"+in.size());
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
        //System.out.println("outsize, nextDoc:"+out.size());
        docp.done.acquire();
        return docp.doc;
    }catch(InterruptedException e){throw new RuntimeException(e);}}
}
