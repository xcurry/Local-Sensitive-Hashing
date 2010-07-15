package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PetrovicLSHTable {
    private int dimension;
    private List<HashingProjection> projections;
    private Document[] table;
    private int[] nextSlot;
    private int maxPerBucket;
    private int hashIndex;

    public PetrovicLSHTable(int dimension, int maxPerBucket, int hashIndex) {
        this.dimension = dimension;
        if(dimension>31){
        	throw new IllegalArgumentException("Dimensions greater than 31 not supported, dimension="+dimension);
        }
        this.projections = createProjections(hashIndex);
        this.maxPerBucket=maxPerBucket;
        table = new Document[maxPerBucket*(int)Math.pow(2, dimension)];
        nextSlot = new int[(int)Math.pow(2, dimension)];
        this.hashIndex=hashIndex;
    }

    private List<HashingProjection> createProjections(int projectionSeed){
    	Sampler sampler = new GaussianSampler(projectionSeed);
        ArrayList<HashingProjection> projs = new ArrayList<HashingProjection>();
        for (int i = 0; i < dimension; ++i) {
            projs.add(new HashingProjection(sampler));
        }
        return projs;
    }

    private int getSignature(Document d) {
        int[] hash = d.getHash();
        return hash[hashIndex];
    }

    public void deriveAndAddHash(Document d){
        int signature = 0;
        //d.getFeatures().print();
        for (int i = 0; i<projections.size(); i++) {
            HashingProjection p = projections.get(i);
            boolean bv = p.bitValue(d.getFeatures());
            int toadd=((bv?1:0)<<i);
            signature = signature + toadd;
        }
        int[] hash = d.getHash();
        hash[hashIndex] = signature;
    }

    public void add(Document doc){
    	int bucket=getSignature(doc);
    	int insertloc=bucket*maxPerBucket+nextSlot[bucket];
    	table[insertloc]=doc;
    	nextSlot[bucket]=(nextSlot[bucket]+1)%maxPerBucket;
    }

    public void search(HashMap<Document, Integer> results, Document toSearch) {
    	int bucket = getSignature(toSearch);
    	int start=bucket*maxPerBucket;
    	int end=bucket*(maxPerBucket+1)-1;
    	for(int i=start; i<end && i<table.length && table[i]!=null; i++){
    		Document doc=table[i];
    		Integer val=results.get(doc);
    		if(val==null){
    			results.put(doc, 1);
    		}else{
    			results.put(doc, val+1);
    		}
    	}
    }
}
