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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class AROW implements OnlineLearner {
    private double r;
    private HashMap<Integer,Double> mean;
    private HashMap<Integer,Double> variance;

    public AROW() {
        r = 1.4;
        mean = new HashMap<Integer,Double>();
        variance = new HashMap<Integer,Double>();
        variance.put(IntegerCache.get(0),1.0d);
        //reallocate();
    }

    //private void reallocate() {
        //variance = Arrays.copyOf(variance, maxFeatures * 2);
        //int n = maxFeatures * 2;
        //for (int i = maxFeatures; i < n; ++i) {
        //    variance.put(IntegerCache.get(i),1.0d);
        //}
        //maxFeatures = n;
        //mean = Arrays.copyOf(mean, maxFeatures);
    //}

    @Override
    public void train(FeatureVector feats, int label) {
        double margin = predictMargin(feats);
        int sign = 1;
        if (label == 0) {
            sign = -1;
        }
        if (margin * sign < 1) {
            update(feats, margin, sign);
        }
    }

    private double getMean(int i){
        Double d = mean.get(i);
        if(d==null){
            return 0;
        }
        return d;
    }

    private double getVariance(int i){
        Double d = variance.get(i);
        if(d==null){
            return 1;
        }
        return d;
    }

    @Override
    public double predictMargin(FeatureVector feats) {
        double margin = 0.0d;
        for (int i : feats.keySet()) {
            margin += getMean(i)*feats.get(i);
        }
        return margin;
    }

    @Override
    public int predict(FeatureVector feats){
        return predictMargin(feats)>getPositiveThreshold()?1:0;
    }
    
    public double totalVariance(FeatureVector feats) {
        double total = 0.0d;
        for (int i : feats.keySet()) {
            total += getVariance(i)*feats.get(i);
        }
        return total;
    }
    
    @Override
    public double getPositiveThreshold() {
        return 0.0d;
    }

    private void update(FeatureVector feats, double margin, int sign) {
        double beta = 0.0d;
        for (int i : feats.keySet()) {
            //while (i >= maxFeatures) {
            //    reallocate();
            //}
            //beta += variance[i]; // * 1, binary fv
            double val = feats.get(i);
            beta += getVariance(i) * val * val; // real fv?
        }
        beta = 1 / (r + beta);
        double alpha = Math.max(0, 1 - sign * margin) * beta;
        for (int i : feats.keySet()) {
            double tmpVar = getVariance(i);
            mean.put(i, getMean(i) + alpha * tmpVar * sign);
            //variance[i] -= beta * variance[i] * variance[i]; // * 1 * 1, binary
            // variance update for real-valued fv?
            double val = feats.get(i);
            variance.put(i,tmpVar - beta * tmpVar * tmpVar * val * val);
        }
    }

    @Override
    public void finish() {}

    //public void read(BufferedReader input) throws IOException {
    //    String line;
    //    int lineNum = 0;
    //    while ((line = input.readLine()) != null) {
    //        while (lineNum >= maxFeatures) {
    //            maxFeatures *= 2;
    //            mean = Arrays.copyOf(mean, maxFeatures);
    //            variance = Arrays.copyOf(variance, maxFeatures);
    //        }
    //        String[] pair = line.split(" ");
    //        mean[lineNum] = Double.parseDouble(pair[0]);
    //        variance[lineNum] = Double.parseDouble(pair[1]);
    //        ++lineNum;
    //    }
    //}

    //public void write(BufferedWriter output) throws IOException {
    //    for (int i = 0; i < maxFeatures; ++i) {
    //        output.write(mean[i] + " " + variance[i]);
    //        output.newLine();
    //    }
    //}

    @Override
    public String toString() {
        return print(null);
    }

    public String print(Vocabulary vocab){
        StringBuilder str = new StringBuilder();
        //str.append("mean: \n");
        for (int i: mean.keySet()) {
            str.append((vocab==null?i:vocab.reverseLookup(i)) + ": mean:" + getMean(i) + " var:" +getVariance(i) + " \n");
        }
        //str.append("\n");
        //str.append("var: \n");
        //for (Integer i: variance.keySet()) {
        //    str.append((vocab==null?i:vocab.reverseLookup(i)) + ":" + getVariance(i) + " \n");
        //}
        return str.toString();
    }

    @Override
    public String getName() {
        return "aw";
    }
}
