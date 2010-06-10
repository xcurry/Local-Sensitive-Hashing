package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HashingProjection {
    private Sampler sampler;
    private HashMap<Integer,Double> rep;
    private int modSize;

    public HashingProjection(Sampler sampler) {
        this.sampler = sampler;
        rep = new HashMap<Integer,Double>();
        modSize=700+sampler.getRandom().nextInt(200);
    }

    public boolean bitValue(FeatureVector featVec) {
        double dotProduct = 0.0d;
        for (int featId : featVec.keySet()) {
            double featValue = featVec.get(featId);
            featId=featId%modSize;
            Double coef=rep.get(featId);
            if(coef==null){
            	coef=sampler.draw();
            	rep.put(featId, coef);
            }
            dotProduct += featValue * coef;			
        }		
        return dotProduct > 0.0d;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        ArrayList<Integer> al = new ArrayList<Integer>(rep.keySet());
        Collections.sort(al);
        for (Integer i: al) {
            str.append(i + ":" + rep.get(i) + ";");
        }
        return str.toString();
    }	
}
