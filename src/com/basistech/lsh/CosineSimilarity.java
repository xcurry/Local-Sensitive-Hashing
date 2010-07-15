package com.basistech.lsh;

import java.util.Map.Entry;

public class CosineSimilarity {
	
    public static double value(FeatureVector fv1, FeatureVector fv2) {
        double norm1 = fv1.getNorm();
        double norm2 = fv2.getNorm();
        double dot = 0.0d;

        for (Entry<Integer, Double> kv : fv1.entrySet()) {
            Integer key = kv.getKey();
            Double val2 = fv2.get(key);
            if (val2 != null) {
                dot += kv.getValue() * val2;                
            }            
        }        
        return Math.abs(dot) / (norm1 * norm2);        
        // if you start to lose precision, try this.
        //return Math.log(Math.abs(dot)) - Math.log(norm1) - Math.log(norm2);
    }   

    public static void main(String[] args){
        FeatureVector fv1 = new FeatureVector();
        FeatureVector fv2 = new FeatureVector();
        fv1.put(1, 1.0);
        fv2.put(1, 1.0);
        fv2.put(2, 1.0);
        System.out.println(CosineSimilarity.value(fv1,fv2));
    }
}
