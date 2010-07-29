package com.basistech.lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
    	int end=(bucket+1)*maxPerBucket;
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

    void addToDocs(HashSet<Document> docs) {
        for(int i = 0; i<table.length; i++){
            if(table[i]!=null)
                docs.add(table[i]);
        }
    }

    //note--this won't do the right thing unless you modify createProjections
    //to not use the hashIndex as the seed
    public static void main(String[] args){
        Random gen = new Random();
        FeatureVector fv1 = new FeatureVector();
        FeatureVector fv2 = new FeatureVector();
        for(int i = 0; i<1000; i++){
            fv1.put(i, 1+.1*Math.abs(gen.nextGaussian()));
            fv2.put(i, 1+.1*Math.abs(gen.nextGaussian()));//note--this won't do the right thing unless you modify createProjections
    //to not use the hashIndex as the seed
        }
        //fv1.put(0, 1.0);
        //fv2.put(0, 1.0);
        System.out.println(Math.pow(1-Math.acos(CosineSimilarity.value(fv1,fv2))/Math.PI,13));
        Sampler sampler = new FlipSampler(1);
        Document d1 = new TDT5Document("",0);
        Document d2 = new TDT5Document("",1);
        d1.setHash(new int[1]);
        d1.setFeatures(fv1);
        d2.setHash(new int[1]);
        d2.setFeatures(fv2);
        int count=0;
        for (int i = 0; i < 10000; ++i) {
            PetrovicLSHTable lsht=new PetrovicLSHTable(13,1,0);
            lsht.deriveAndAddHash(d1);
            lsht.deriveAndAddHash(d2);
            lsht.add(d1);
            HashMap<Document,Integer> docs = new HashMap<Document,Integer>();
            lsht.search(docs, d2);
            if(docs.size()>0){
                count++;
            }
        }

        System.out.println(count/10000.);
    }
}
