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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureVector implements Map<Integer, Double>{
    private HashMap<Integer, Double> rep;
    private double norm;
    private boolean hasNorm=false;

    public FeatureVector() {
        rep = new HashMap<Integer, Double>();
    }

    public void read(String rawRep) {
        String[] toks = rawRep.split(" ");

        Integer key = null;
        Double value = null;
        int i = 0;
        for (String tok : toks) {
            if (tok == null) {
                break;
            }
            if (i % 2 == 1) {
                value = Double.parseDouble(tok);
                rep.put(key, value);
            } else {
                key = Integer.parseInt(tok);
            }
            ++i;
        }
    }

    public String write() {
        String rawRep = new String();
        for (Entry<Integer, Double> entry : entrySet()) {
            rawRep += entry.getKey() + " " + entry.getValue() + " ";			
        }		
        return rawRep;
    }
    
    //auto-generated to mimic typedef
    public void clear() {
        rep.clear();
    }

    public Object clone() {
        return rep.clone();
    }

    public boolean containsKey(Object key) {
        return rep.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return rep.containsValue(value);
    }

    public Set<Entry<Integer, Double>> entrySet() {
        return rep.entrySet();
    }

    public boolean equals(Object o) {
        return rep.equals(o);
    }

    public Double get(Object key) {
        return rep.get(key);
    }

    public int hashCode() {
        return rep.hashCode();
    }

    public boolean isEmpty() {
        return rep.isEmpty();
    }

    public Set<Integer> keySet() {
        return rep.keySet();
    }

    public Double put(Integer key, Double value) {
        hasNorm=false;
        return rep.put(key, value);
    }

    public void putAll(Map<? extends Integer, ? extends Double> m) {
        hasNorm=false;
        rep.putAll(m);
    }

    public Double remove(Object key) {
        hasNorm=false;
        return rep.remove(key);
    }

    public int size() {
        return rep.size();
    }

    public String toString() {
        return rep.toString();
    }

    public Collection<Double> values() {
        return rep.values();
    }
    
    public double merge(FeatureVector other){
        hasNorm=false;
        // word counts for this & other        
        double total=0.0;
        for(Integer id:other.keySet()){
            Double count=rep.get(id);
            if(count==null){
                count=0.0;
            }
            double toadd=other.get(id);
            total+=toadd;
            rep.put(id,count+toadd);
        }
        return total;
    }

    public double getNorm() {
        if(hasNorm){
            return norm;
        }
        norm = 0.0d;
        for (Double v : rep.values()) {
            norm += v * v;
        }
        norm=Math.sqrt(norm);
        hasNorm=true;
        return norm;
    }

   // public void print(){
   //     for(Integer i: rep.keySet()){
   //         System.out.println(TFIDF2.vocab.reverseLookup(i)+" "+i+" "+rep.get(i));
   //     }
   //     System.exit(0);
   // }
}