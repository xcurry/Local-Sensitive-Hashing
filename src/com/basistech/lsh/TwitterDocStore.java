package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TwitterDocStore {
    private List<File> fileList = new ArrayList<File>();
    private int docCount;
    private boolean hasCount = false;
    private int nextToParse=0;
    //private int currDoc=-1;
    BufferedReader currFileTweets;
    private TFIDF2 tfidf = new TFIDF2();
    private HashMap<String,List<String>> docTopics = new HashMap<String,List<String>>();
    public TwitterDocStore(){}
    
    public void enqueueDir(String dir, FilenameFilter filter){
        enqueueDir(new File(dir),filter);
    }
    
    
    public void enqueueDir(File dir, FilenameFilter filter){
        List<File> tmplist = Arrays.asList(dir.listFiles(filter));
        Collections.sort(tmplist);
        fileList.addAll(tmplist);
    }
    
    /*
     * The TDT5 people almost followed the XML standard, but not quite
     * closely enough that Java's XML parsers can read it.  We need
     * to fix it for them.
     */
    private org.w3c.dom.Document parseFile(File f) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        FileReader fr=new FileReader(f);
        StringBuilder data = new StringBuilder("<DOCUMENT>");
        BufferedReader reader = new BufferedReader(fr);
        int read=0;
        char[] buf = new char[1024];
        while((read=reader.read(buf)) != -1){
            data.append(buf, 0, read);
        }
        reader.close();
        data.append("</DOCUMENT>");
        String str =data.toString(); 
        str = str.replaceAll("&  AMP;", "&amp;");
        str = str.replaceAll("&,", "&amp;,");
        str = str.replaceAll("& Lt;", "&lt; ");
        str = str.replaceAll("  >  ", "  &gt;  ");
        str = str.replaceAll("< ", " &lt;)");
        str = str.replaceAll(" <\\)", " &lt;)");
        str = str.replaceAll(" <\"", " &lt;)");
        str = str.replaceAll("& ", "&amp; ");
        //str = str.replaceAll("\"", "&quot;");

        ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes("UTF-8"));
        return db.parse(bais);
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
        for(File f: fileList){
            FileReader fr=new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
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
                }
            }while(currTweet==null);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        text=currTweet.substring(currTweet.indexOf("\t")+1);
        text=text.substring(text.indexOf("\t")+1);
        Tweet theReturn = new Tweet(text,tfidf.computeFeatures(text,false));
        theReturn.setId(docno);
        List<String> topics = this.docTopics.get(docno);
        if(topics==null)
            topics = new ArrayList<String>();
        theReturn.setTopics(topics);
        return theReturn;
    }
}
