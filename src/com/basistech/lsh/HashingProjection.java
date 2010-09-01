/*
  Copyright (c) 2010, Basis Technology Corp.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other 
  materials provided with the distribution.

  Neither the name of the Basis Technology Corp. nor the names of its contributors may be used to endorse or promote products derived from this software without specific 
  prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.basistech.lsh;

import java.util.Random;

public class HashingProjection {
    private double[] poolOfSamples;
    private int modSize;

    public HashingProjection(Sampler sampler) {
        modSize=1024+sampler.getRandom().nextInt(1023);
        poolOfSamples = new double[modSize];
        for(int i=0; i<modSize; i++){
            poolOfSamples[i]=sampler.draw();
        }
    }

    public boolean bitValue(FeatureVector featVec) {
        double dotProduct = 0.0d;
        for (int featId : featVec.keySet()) {
            double featValue = featVec.get(featId);
            featId=featId%modSize; // hash feature into poolOfSamples
            if(featId<0){
                featId+=modSize;
            }
            Double coef=poolOfSamples[featId];
            dotProduct += featValue * coef;			
        }		
        return dotProduct > 0.0d;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i<poolOfSamples.length; i++) {
            str.append(i + ":" + poolOfSamples[i] + ";");
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
