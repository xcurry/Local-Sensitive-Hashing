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
