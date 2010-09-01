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
