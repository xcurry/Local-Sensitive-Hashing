package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class PetrovicLSH {
    private int nTables;
    private List<PetrovicLSHTable> tables = new ArrayList<PetrovicLSHTable>();
    private Queue<Document> recentDocs = new LinkedList<Document>();
    private int numRecentDocs;

    @SuppressWarnings("unchecked")
    public PetrovicLSH(int dimension, int maxPerBucket, int nTables, int numRecentDocs){
    	for(int i=0; i<nTables; i++){
    		tables.add(new PetrovicLSHTable(dimension, maxPerBucket, i));
    		//System.out.println("Added table "+i);
    	}
    	this.nTables=nTables;
    	this.numRecentDocs=numRecentDocs;
    }


    public ResultSet<Document> search(Document document, int nResults) {try{
        if(document.getHash()==null){
            deriveAndAddHash(document);
        }
    	HashMap<Document,Integer> results = new HashMap<Document,Integer>();
        for(PetrovicLSHTable t: tables){
            t.search(results,document);
        }
        boolean recentDocsFlag = false;
        if(results.size()<5+nResults){
            recentDocsFlag=true;
        }
        ResultSet<Document> filter = new ResultSet<Document>(3*nTables);
        for(Document d: results.keySet()){
            filter.add(d, results.get(d));
        }
        ResultSet<Document> theReturn = new ResultSet<Document>(nResults);
        for(ResultPair<Document> rp: filter.popResults()){
        	Document curr = rp.result;
        	theReturn.add(curr, CosineSimilarity.value(curr.getFeatures(), document.getFeatures()));
        }
        if(recentDocsFlag){
            for(Document recentDoc:recentDocs){
                theReturn.add(recentDoc, CosineSimilarity.value(recentDoc.getFeatures(), document.getFeatures()));
            }
        }
        return theReturn;
    }catch(Exception e){throw new RuntimeException(e);}}

    public void add(Document document){try{
        if(document.getHash()==null){
            deriveAndAddHash(document);
        }
        for(PetrovicLSHTable t: tables){
            t.add(document);
        }
        if(numRecentDocs>0){
            if(recentDocs.size()==numRecentDocs){
                recentDocs.poll();
            }
            recentDocs.offer(document);
        }
    }catch(Exception e){throw new RuntimeException(e);}}

    public int getNDocs(){
        HashSet<Document> docs = new HashSet<Document>();
        for(PetrovicLSHTable t: tables){
            t.addToDocs(docs);
        }
        return docs.size();
    }

    public void deriveAndAddHash(Document d){
        d.setHash(new int[nTables]);
        for(PetrovicLSHTable t: tables){
            t.deriveAndAddHash(d);
        }
    }

    //note--this won't do the right thing unless you modify createProjections
    //to not use the hashIndex as the seed
    public static void main(String[] args){
        main2();
        Random gen = new Random();
        FeatureVector fv1 = new FeatureVector();
        FeatureVector fv2 = new FeatureVector();
        for(int i = 0; i<1000; i++){
            fv1.put(i, 1+.5*Math.abs(gen.nextGaussian()));
            fv2.put(i, 1+.5*Math.abs(gen.nextGaussian()));
        }
        //fv1.put(0, 1.0);
        //fv2.put(0, 1.0);
        System.out.println(1-Math.pow(1-Math.pow(1-Math.acos(CosineSimilarity.value(fv1,fv2))/Math.PI,13),20));
        Document d1 = new TDT5Document("",0);
        Document d2 = new TDT5Document("",1);
        d1.setFeatures(fv1);
        d2.setFeatures(fv2);
        int count=0;
        for (int i = 0; i < 1000; ++i) {
            if(i%50==49){
                System.out.print("."); 
            }
            PetrovicLSH lsh=new PetrovicLSH(13,1,20,0);
            lsh.deriveAndAddHash(d1);
            lsh.deriveAndAddHash(d2);
            lsh.add(d1);
            ResultSet<Document> docs = lsh.search(d2,1);
            if(docs.popResults().size()>0){
                count++;
            }
        }

        System.out.println(count/1000.);
    }

    public static void main2(){
        double expVal=0;
        int count = 0;
        Random gen = new Random();
        for (int j = 0; j < 1000; ++j) {
            PetrovicLSH lsh=new PetrovicLSH(13,1,20,0);
            if(j%50==49){
                System.out.print(".");
            }
            FeatureVector fv1 = new FeatureVector();
            FeatureVector fv2 = new FeatureVector();
            for(int i = 0; i<1000; i++){
                fv1.put(i, Math.abs(gen.nextGaussian()));
                fv2.put(i, Math.abs(gen.nextGaussian()));
            }
            //fv1.put(0, 1.0);
            //fv2.put(0, 1.0);
            expVal+=(1-Math.pow(1-Math.pow(1-Math.acos(CosineSimilarity.value(fv1,fv2))/Math.PI,13),20));
            Document d1 = new TDT5Document("",0);
            Document d2 = new TDT5Document("",1);
            d1.setFeatures(fv1);
            d2.setFeatures(fv2);

            lsh.deriveAndAddHash(d1);
            lsh.deriveAndAddHash(d2);
            lsh.add(d1);
            ResultSet<Document> docs = lsh.search(d2,1);
            if(docs.popResults().size()>0){
                count++;
            }
        }
        System.out.println(expVal);
        System.out.println(count);
        System.exit(0);
    }
}
