/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cdoersch
 */
public class RottentomatoesDocStore implements DocStore{
    
    private int docCount=0;
    private boolean hasCount=false;
    private String myDir;
    private File posFile;
    private BufferedReader posFileReader;
    private boolean posFileHasMore = true;
    private File negFile;
    private BufferedReader negFileReader;
    private boolean negFileHasMore = true;
    private boolean nextIsPos=true;
    private int nextUid;
    
    public RottentomatoesDocStore(File dir){
        myDir = dir.getAbsolutePath();
        File g = new File(dir,"rt-polaritydata");
        posFile=new File(g,"rt-polarity.pos");
        negFile=new File(g,"rt-polarity.neg");
        reset();
    }

    private RottentomatoesDocStore newClone(){
        RottentomatoesDocStore clone = new RottentomatoesDocStore(new File(myDir));
        return clone;
    }

    @Override
    public int getDocCount() {
        if(hasCount){
            return docCount;
        }
        RottentomatoesDocStore counter = this.newClone();
        counter.reset();
        docCount=0;
        while(counter.nextDoc()!=null){
            docCount++;
        }
        hasCount=true;
        return docCount;
    }

    @Override
    public RottentomatoesDocument nextDoc() {try{
        String text = null;
        boolean isPos = false;
        
        //if the last doc was positive, give a negative; otherwise give a positive...
        //unless we've run out from one or the other, in which case give whatever we
        //can.  You'd think there'd be an easier way...
        if(posFileHasMore&&negFileHasMore){
            if(nextIsPos){
                text=posFileReader.readLine();
                isPos=true;
                nextIsPos=false;
                posFileHasMore=(text!=null);
            }else{
                text=negFileReader.readLine();
                isPos=false;
                nextIsPos=true;
                negFileHasMore=(text!=null);
            }
        }
        if(!posFileHasMore||!negFileHasMore){
            if(posFileHasMore){
                text=posFileReader.readLine();
                isPos=true;
                posFileHasMore=(text!=null);
            }else if(negFileHasMore){
                text=negFileReader.readLine();
                isPos=false;
                negFileHasMore=(text!=null);
            }else{
                text=null;
            }
        }
        if(text==null){
            return null;
        }


        RottentomatoesDocument theReturn = new RottentomatoesDocument(text,nextUid);
        nextUid++;
        List<String> topics = new ArrayList<String>();
        if(isPos){
            topics.add(SentimentLMTrainer.SENTIMENT_POSITIVE);
        }else{
            topics.add(SentimentLMTrainer.SENTIMENT_NEGATIVE);
        }
        theReturn.setAnnotations(topics);
        return theReturn;
    }catch(IOException e){throw new RuntimeException(e);}}

    @Override
    public void reset() {try{
        posFileReader = new BufferedReader(new FileReader(posFile));
        negFileReader = new BufferedReader(new FileReader(negFile));
        nextIsPos=true;
        nextUid=0;
    }catch(IOException e){throw new RuntimeException(e);}}

}
