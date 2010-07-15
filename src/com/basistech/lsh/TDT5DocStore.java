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

public class TDT5DocStore implements DocStore{
    private List<File> fileList = new ArrayList<File>();
    private int docCount;
    private boolean hasCount = false;
    private int nextToParse=0;
    private int currDoc=-1;
    NodeList currFileDocs;
    private HashMap<String,List<String>> docTopics = new HashMap<String,List<String>>();
    int nextUid=0;
    boolean annotatedDocsOnly;

    @Override
    public TDT5DocStore clone(){
        TDT5DocStore newStore = new TDT5DocStore();
        newStore.fileList = new ArrayList(fileList);
        newStore.docCount=docCount;
        newStore.hasCount = hasCount;
        newStore.nextToParse=nextToParse;
        newStore.currDoc=currDoc;
        newStore.currFileDocs=currFileDocs;
        newStore.docTopics = new HashMap<String,List<String>>();
        newStore.nextUid=nextUid;
        newStore.annotatedDocsOnly=annotatedDocsOnly;
        for(String s: docTopics.keySet()){
            newStore.docTopics.put(s, new ArrayList<String>(docTopics.get(s)));
        }
        return newStore;
    }
    
    public TDT5DocStore(){}
    
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

    public void setAnnotatedDocsOnly(boolean annotatedDocsOnly) {
        this.annotatedDocsOnly = annotatedDocsOnly;
    }

    @Override
    public void reset(){
        nextToParse=0;
        currDoc=-1;
        currFileDocs = null;
        nextUid=0;
    }

    public int getDocCount(){
        if(hasCount){
            return docCount;
        }
        TDT5DocStore counter = this.clone();
        counter.reset();
        docCount=0;
        while(counter.nextDoc()!=null){
            docCount++;
        }
        /*
        int i=0;
        int j=0;
        NodeList children = null;
        Node doc = null;
        try{
            for(File f: fileList){
                org.w3c.dom.Document d = parseFile(f);
                NodeList fileDocs = d.getChildNodes().item(0).getChildNodes();
                for(i = 0; i<fileDocs.getLength(); i++){
                    doc = fileDocs.item(i);
                    if("DOC".equals(doc.getNodeName())){
                        children = doc.getChildNodes();
                        boolean hasText=false;
                        boolean hasDocno=false;
                        String docno=null;
                        String text=null;
                        for(j = 0; j<children.getLength() && hasText==false; j++){
                            Node n = children.item(j);
                            //System.out.println(n.getTextContent());
                            if("TEXT".equals(n.getNodeName())){
                                docCount++;
                                if(docCount%10000==0){
                                    System.out.println("finished reading doc "+docCount);
                                    System.out.flush();
                                }
                                //System.out.println(n.getTextContent());
                                hasText=true;
                                text=n.getTextContent();
                                
                            }
                            if("DOCNO".equals(n.getNodeName())){
                                hasDocno=true;
                                docno=n.getTextContent();
                                while(docno.charAt(0)==' '){
                                    docno=docno.substring(1);
                                }
                                while(docno.charAt(docno.length()-1)==' '){
                                    docno=docno.substring(0,docno.length()-1);
                                }
                            }
                        }
                        if(hasText&&hasDocno){
                            List<String> topics = this.docTopics.get(docno);
                            if(topics!=null)
                                docsLoadedDuringCount.add(text);
                        }
                    }
                }
            }
        }catch(Exception e){
            //System.out.println("i:"+i+" j:"+j);
            //System.out.println(((CharacterDataImpl)children).getData());
            throw new RuntimeException(e);
        }
         */
        hasCount=true;
        return docCount;
    }

    @Override
    public Document nextDoc(){
        String text = null;
        String docno = null;
        try{
            do{
                currDoc++;
                if(currFileDocs==null||currDoc==currFileDocs.getLength()){
                    if(nextToParse==fileList.size()){
                        return null;
                    }
                    org.w3c.dom.Document d = parseFile(fileList.get(nextToParse));
                    currFileDocs=d.getChildNodes().item(0).getChildNodes();
                    currDoc=0;
                    nextToParse++;
                }
            }while(!"DOC".equals(currFileDocs.item(currDoc).getNodeName()));
            NodeList children = currFileDocs.item(currDoc).getChildNodes();
            for(int j = 0; text==null || docno==null; j++){
                Node n = children.item(j);
                if("TEXT".equals(n.getNodeName())){
                    text=n.getTextContent();
                }
                if("DOCNO".equals(n.getNodeName())){
                    docno=n.getTextContent();
                    while(docno.charAt(0)==' '){
                        docno=docno.substring(1);
                    }
                    while(docno.charAt(docno.length()-1)==' '){
                        docno=docno.substring(0,docno.length()-1);
                    }
                }
            }
        }catch(Exception e){throw new RuntimeException(e);}
        TDT5Document theReturn = new TDT5Document(text,nextUid);
        nextUid++;
        theReturn.setId(docno);
        List<String> topics = this.docTopics.get(docno);
        if(topics==null){
            if(annotatedDocsOnly){
                return nextDoc();
            }else{
                topics = new ArrayList<String>();
            }
        }
        theReturn.setAnnotations(topics);
        return theReturn;
    }
}
