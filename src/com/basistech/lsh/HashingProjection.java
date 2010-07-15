package com.basistech.lsh;

import java.util.Random;

public class HashingProjection {
    private double[] rep;
    private int modSize;

    public HashingProjection(Sampler sampler) {
        modSize=1024+sampler.getRandom().nextInt(1023);
        rep = new double[modSize];
        for(int i=0; i<modSize; i++){
            rep[i]=sampler.draw();
        }
    }

    public boolean bitValue(FeatureVector featVec) {
        double dotProduct = 0.0d;
        for (int featId : featVec.keySet()) {
            double featValue = featVec.get(featId);
            featId=featId%modSize;
            if(featId<0){
                featId+=modSize;
            }
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

    public static void main(String[] args){
        Random gen = new Random();
        FeatureVector fv1 = new FeatureVector();
        FeatureVector fv2 = new FeatureVector();
        for(int i = 0; i<1000; i++){
            fv1.put(i, 1+1*Math.abs(gen.nextGaussian()));
            fv2.put(i, 1+1*Math.abs(gen.nextGaussian()));
        }
        //fv1.put(0, 1.0);
        //fv2.put(0, 1.0);
        System.out.println(CosineSimilarity.value(fv1,fv2));
        Sampler sampler = new FlipSampler(1);
        int count=0;
        for (int i = 0; i < 100000; ++i) {
            HashingProjection h = new HashingProjection(sampler);
            if(h.bitValue(fv1)==h.bitValue(fv2)){
                count++;
            }
        }

        System.out.println(Math.cos((1-count/100000.)*Math.PI));
    }
}
