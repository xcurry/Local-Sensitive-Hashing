package com.basistech.lsh;

import java.util.Map;
import java.util.Map.Entry;

public class CosineSimilarity {
	
    public static double value(Map<? extends Object, Double> fv1, Map<? extends Object, Double> fv2) {
        double norm1 = norm(fv1);
        double norm2 = norm(fv2);
        double dot = 0.0d;

        for (Entry<? extends Object, Double> kv : fv1.entrySet()) {
            Object key = kv.getKey();
            Double val2 = fv2.get(key);
            if (val2 != null) {
                dot += kv.getValue() * val2;                
            }            
        }        
        return Math.abs(dot) / (norm1 * norm2);        
        // if you start to lose precision, try this.
        //return Math.log(Math.abs(dot)) - Math.log(norm1) - Math.log(norm2);
    }   

    public static double norm(Map<? extends Object, Double> fv) {
        double val = 0.0d;
        for (Double v : fv.values()) {
            val += v * v;
        }
        return Math.sqrt(val);        
    }

}
