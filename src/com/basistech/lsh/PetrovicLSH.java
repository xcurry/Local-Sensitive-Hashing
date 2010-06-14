package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

public class PetrovicLSH {
    private int nTables;
    private List<PetrovicLSHTable> tables = new ArrayList<PetrovicLSHTable>();
    private Queue<Document> recentDocs = new LinkedList<Document>();
    private int numRecentDocs;
    private Thread[] threads = new Thread[4];
    private CyclicBarrier startWorkers;
    private CyclicBarrier doneWorkers;
    private Document docToAdd;
    private Document docToSearch;
    private HashMap<Document,Integer>[] searchResults; 
    
    @SuppressWarnings("unchecked")
    public PetrovicLSH(int dimension, int maxPerBucket, int nTables, int numRecentDocs){
    	GlobalStorage gs = new GlobalStorage();
    	for(int i=1; i<nTables; i++){
    		tables.add(new PetrovicLSHTable(dimension, maxPerBucket, gs, i));
    		System.out.println("Added table "+i);
    	}
    	this.nTables=nTables;
    	this.numRecentDocs=numRecentDocs;
    	startWorkers = new CyclicBarrier(threads.length+1);
    	doneWorkers = new CyclicBarrier(threads.length+1);
    	searchResults = (HashMap<Document,Integer>[]) new HashMap[threads.length];
    	for(int i=0; i<threads.length; i++){
    	    threads[i]=new Thread(new PetrovicLSHThread(i));
    	    threads[i].start();
    	}
    }
    
    private class PetrovicLSHThread implements Runnable{
        private int idx;
        public PetrovicLSHThread(int idx){
            this.idx=idx;
        }
        private int startIndex(){
            return tables.size()*idx/threads.length;
        }
        private int endIndex(){
            return tables.size()*(idx+1)/threads.length-1;
        }
        @Override
        public void run() {try{
            while(true){
                startWorkers.await();
                if(docToAdd!=null){
                    addLocal(docToAdd);
                }
                if(docToSearch!=null){
                    searchLocal(docToSearch);
                }
                doneWorkers.await();
            }
        }catch(Exception e){throw new RuntimeException(e);}}
        
        public void searchLocal(Document docToSearch){
            HashMap<Document,Integer> results = new HashMap<Document,Integer>();
            int start = startIndex();
            int end = endIndex();
            for(int i = start; i<=end; i++){
                tables.get(i).search(results,docToSearch.getFeatures());
            }
            searchResults[idx]=results;
        }
        
        public void addLocal(Document doc){
            int start = startIndex();
            int end = endIndex();
            for(int i = start; i<=end; i++){
                tables.get(i).add(doc);
            }
        }
    }
    

    public ResultSet<Document> search(Document document, int nResults) {try{
    	docToSearch=document;
    	startWorkers.await();
    	doneWorkers.await();
    	docToSearch=null;
        ResultSet<Document> filter = new ResultSet<Document>(3*nTables);
        for(HashMap<Document, Integer> results: searchResults){
            for(Document d: results.keySet()){
            	filter.add(d, (results.get(d)));
            }
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
        docToAdd=document;
        startWorkers.await();
        doneWorkers.await();
        docToAdd=null;
    	if(recentDocs.size()==numRecentDocs){
    	    recentDocs.poll();
    	}
    	recentDocs.offer(document);
    }catch(Exception e){throw new RuntimeException(e);}}


}
