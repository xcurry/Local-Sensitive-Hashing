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
package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// http://www.cs.jhu.edu/~mdredze/datasets/sentiment/
public class MDSDReader implements DocStore{
    //private int label;
    //private FeatureVector feats;
    //private Feature featType;
    private File dat;
    private BufferedReader datIn;
    private Vocabulary vocab = new Vocabulary();
    private int nextUID=0;
    private boolean hasCount=false;
    private int docCount=0;
    
    public MDSDReader(File dat){try{
        this.dat = dat;
        datIn = new BufferedReader(new FileReader(dat));
    }catch(IOException e){throw new RuntimeException(e);}}

    public MDSDReader(File dat,Vocabulary vocab){try{
        this.dat = dat;
        datIn = new BufferedReader(new FileReader(dat));
        this.vocab = vocab;
    }catch(IOException e){throw new RuntimeException(e);}}

    @Override
    public Document nextDoc() {try{
        String line;
        if ((line = datIn.readLine()) == null) {
            return null;
        }
        String label = "";
        FeatureVector feats = new FeatureVector();
        String[] records = line.split(" ");
        for (String record : records) {
            String[] pair = record.split(":");
            String ngram = pair[0];
            if (ngram.equals("#label#")) {
                if (pair[1].equals("positive")) {
                    label=SentimentLMTrainer.SENTIMENT_POSITIVE;
                } else {
                    label=SentimentLMTrainer.SENTIMENT_NEGATIVE;
                }
                break;               
            }
            //int count = Integer.parseInt(pair[1]);
            feats.put(vocab.put(ngram),Double.valueOf(pair[1]));
        }
        if("".equals(label)){
            System.out.println("fail");
        }
        MDSDDocument doc = new MDSDDocument(line,feats,nextUID,label);
        nextUID++;
        return doc;
    }catch(IOException e){throw new RuntimeException(e);}}

    @Override
    public void reset(){try{
        nextUID=0;
        datIn = new BufferedReader(new FileReader(dat));
    }catch(IOException e){throw new RuntimeException(e);}}

    @Override
    public int getDocCount() {
        if(hasCount){
            return docCount;
        }
        MDSDReader counter = new MDSDReader(dat);
        counter.reset();
        docCount=0;
        while(counter.nextDoc()!=null){
            docCount++;
        }
        hasCount=true;
        return docCount;
    }

    public Featurizer getFeaturizer(){
        return new NullFeaturizer(vocab);
    }
}
