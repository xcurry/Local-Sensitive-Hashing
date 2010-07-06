package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TwitterDocStore implements DocStore{
    private List<File> fileList = new ArrayList<File>();
    private int fileIdx=0;
    private int docCount;
    private boolean hasCount = false;
    private int nextToParse=0;
    //private int currDoc=-1;
    BufferedReader currFileTweets;
    private HashMap<String,List<String>> docTopics = new HashMap<String,List<String>>();
    private int nextId=0;
    
    public TwitterDocStore(){
        //configure tokenizer
        //tfidf.setGiveProportions(false);
    }
    
    public void enqueueDir(String dir, FilenameFilter filter){
        enqueueDir(new File(dir),filter);
    }
    
    public void enqueueDir(File dir, FilenameFilter filter){
        List<File> tmplist = Arrays.asList(dir.listFiles(filter));
        Collections.sort(tmplist);
        fileList.addAll(tmplist);
    }
    
    public void loadDocTopics(String file){try{
        File f = new File(file);
        FileReader fr = new FileReader(f);
        BufferedReader  buf = new BufferedReader(fr);
        String s;
        int numLoaded=0;
        while((s=buf.readLine())!=null){
            if(!s.contains("ONTOPIC"))
                continue;
            s=s.substring(s.indexOf("topicid=")+8);
            String topicid=s.substring(0,s.indexOf(' '));
            s=s.substring(s.indexOf("docno=")+6);
            String docno=s.substring(0,s.indexOf(' '));
            if(docTopics.containsKey(docno)){
                docTopics.get(docno).add(topicid);
            }else{
                ArrayList<String> ts=new ArrayList<String>();
                ts.add(topicid);
                docTopics.put(docno, ts);
            }
            numLoaded++;
        }
        System.out.println("Loaded "+numLoaded+" Topics");
    }catch(Exception e){throw new RuntimeException(e);}}
    
    public int getDocCount(){try{
        if(hasCount){
            return docCount;
        }
        docCount=1;
        for(File f: fileList){
            BufferedReader br = 
                new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            while(br.readLine()!=null){
                docCount++;
                if(docCount%100000==0){
                    System.out.print(".");
                    if(docCount%5000000==0){
                        System.out.println();
                    }
                }
            }
        }
        System.out.println();
        hasCount=true;
        return docCount;
    }catch(Exception e){throw new RuntimeException(e);}}
    
    public Tweet nextDoc(){
        String text = null;
        String date = null;
        String user = null;
        String docno = null;
        String currTweet=null;
        try{
            do{
                if(currFileTweets==null||(currTweet=currFileTweets.readLine())==null){
                    if(nextToParse==fileList.size()){
                        return null;
                    }
                    
                    FileReader fr = new FileReader(fileList.get(nextToParse));
                    currFileTweets=new BufferedReader(fr);
                    nextToParse++;
                    fileIdx=0;
                }
                
                //if there are non-ascii characters, throw out this tweet
                CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
                try{
                    decoder.decode(ByteBuffer.wrap(currTweet.getBytes()));
                }catch(Exception e){
                    currTweet=null;
                }
                if(currTweet==null){
                    continue;
                }
                try{
                    date=currTweet.substring(0,currTweet.indexOf("\t"));
                    text=currTweet.substring(currTweet.indexOf("\t")+1);
                    user=text.substring(0,text.indexOf("\t"));
                    text=text.substring(text.indexOf("\t")+1);
                    if(text.contains("\t")){
                        text=text.substring(0,text.lastIndexOf("\t"));
                    }
                    //remove url's
                    text=text.replaceAll("http://\\S*","");
                }catch(Exception e){
                    System.out.println("unable to parse tweet! Offending line:\n"+currTweet);
                    System.out.println("Line:"+fileIdx);
                    System.out.println("File:"+fileList.get(nextToParse-1).getAbsolutePath());
                    e.printStackTrace();
                    currTweet=null;
                }
            }while(currTweet==null);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        
        Tweet theReturn = new Tweet(text,user, date, nextId);
        nextId++;
        theReturn.setId(docno);
        List<String> topics = this.docTopics.get(docno);
        if(topics==null)
            topics = new ArrayList<String>();
        theReturn.setAnnotations(topics);
        return theReturn;
    }

    public void reset(){
        currFileTweets=null;
        nextToParse=0;
        fileIdx=0;
        nextId=0;
    }
}
