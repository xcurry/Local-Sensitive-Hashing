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
