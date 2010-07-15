package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PetrovicLSH {
    private int nTables;
    private List<PetrovicLSHTable> tables = new ArrayList<PetrovicLSHTable>();
    private Queue<Document> recentDocs = new LinkedList<Document>();
    private int numRecentDocs;

    @SuppressWarnings("unchecked")
    public PetrovicLSH(int dimension, int maxPerBucket, int nTables, int numRecentDocs){
    	for(int i=0; i<nTables; i++){
    		tables.add(new PetrovicLSHTable(dimension, maxPerBucket, i));
    		System.out.println("Added table "+i);
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
        ResultSet<Document> filter = new ResultSet<Document>(3*nTables);
        for(Document d: results.keySet()){
            filter.add(d, results.get(d));
        }
        ResultSet<Document> theReturn = new ResultSet<Document>(nResults);
        for(ResultPair<Document> rp: filter.popResults()){
        	Document curr = rp.result;
        	theReturn.add(curr, CosineSimilarity.value(curr.getFeatures(), document.getFeatures()));
        }
        if(theReturn.numResults()<nResults){
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

    public void deriveAndAddHash(Document d){
        d.setHash(new int[nTables]);
        for(PetrovicLSHTable t: tables){
            t.deriveAndAddHash(d);
        }
    }
}
