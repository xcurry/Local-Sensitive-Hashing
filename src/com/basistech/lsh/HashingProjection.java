package com.basistech.lsh;

public class HashingProjection {
    private double[] rep;
    private int modSize;

    public HashingProjection(Sampler sampler) {
        modSize=700+sampler.getRandom().nextInt(200);
        rep = new double[modSize];
        for(int i=1; i<modSize; i++){
            rep[i]=sampler.draw();
        }
    }

    public boolean bitValue(FeatureVector featVec) {
        double dotProduct = 0.0d;
        for (int featId : featVec.keySet()) {
            double featValue = featVec.get(featId);
            featId=featId%modSize;
            Double coef=rep[featId];
            dotProduct += featValue * coef;			
        }		
        return dotProduct > 0.0d;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i<rep.length; i++) {
            str.append(i + ":" + rep[i] + ";");
        }
        return str.toString();
    }	
}
