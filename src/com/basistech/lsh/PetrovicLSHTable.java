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

    public PetrovicLSHTable(int dimension, int maxPerBucket, GlobalStorage gs, int projectionSeed) {
        this.dimension = dimension;
        if(dimension>31){
        	throw new IllegalArgumentException("Dimensions greater than 31 not supported, dimension="+dimension);
        }
        this.projections = createProjections(projectionSeed);
        this.maxPerBucket=maxPerBucket;
        table = new Document[maxPerBucket*(int)Math.pow(2, dimension)];
        nextSlot = new int[(int)Math.pow(2, dimension)];
    }
    
    private List<HashingProjection> createProjections(int projectionSeed){
    	Sampler sampler = new GaussianSampler(projectionSeed);
        ArrayList<HashingProjection> projs = new ArrayList<HashingProjection>();
        for (int i = 0; i < dimension; ++i) {
            projs.add(new HashingProjection(sampler));
        }
        return projs;
    }

    private int computeSignature(FeatureVector featVec) {
        int signature = 0;
        int i = 0;
        for (HashingProjection p : projections) {
            signature = signature + ((p.bitValue(featVec)?1:0)<<i);
            ++i;
        }
        return signature;		
    }
    
    public void add(Document doc){
    	FeatureVector features=doc.getFeatures();
    	int bucket=computeSignature(features);
    	int insertloc=bucket*maxPerBucket+nextSlot[bucket];
    	table[insertloc]=doc;
    	nextSlot[bucket]=(nextSlot[bucket]+1)%maxPerBucket;
    }

    public void search(HashMap<Document, Integer> results, FeatureVector featVec) {
    	int bucket = computeSignature(featVec);
    	int start=bucket*maxPerBucket;
    	int end=bucket*(maxPerBucket+1)-1;
    	for(int i=start; i<end && table[i]!=null; i++){
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
