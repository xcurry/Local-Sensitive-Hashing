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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * We use this object counting functionality in a dozen places--we should
 * eventually change them all to use this class for the sake of conciseness
 * @author cdoersch
 */
public class DoubleCounter<T> implements Map<T, Double>{
    private HashMap<T, Double> rep = new HashMap<T, Double>();

    public double increment(T o){
        Double count = rep.get(o);
        double countint = 0;
        if(count!=null){
            countint=count.doubleValue();
        }
        rep.put(o,countint+1);
        return countint+1;
    }

    public double increment(T o, double val){
        Double count = rep.get(o);
        double countint = 0;
        if(count!=null){
            countint=count.doubleValue();
        }
        rep.put(o,countint+val);
        return countint+val;
    }

    public double getDouble(T o){
        Double count = rep.get(o);
        if(count==null){
            return 0;
        }
        return count;
    }

    /**
     * Ordered by biggest->smallest counts.
     * Tie-breaking is arbitrary.
     * @param nKeys
     * @return
     */
    public List<T> getNBestKeys(int nKeys){
        //T[] pile = (T[]) new Object[nKeys];
        //int[] counts = new int[nKeys];

        //for(T o: keySet()){
        //    int count = getInt(o);
        //    if(count>counts[counts.length-1]){
        //        int insidx=counts.length-1;
        //        while(insidx>0&&counts[insidx-1]<count){
        //            insidx--;
        //            counts[insidx+1]=counts[insidx];
        //            pile[insidx+1]=pile[insidx];
        //        }
        //        counts[insidx]=count;
        //        pile[insidx]=o;
        //    }
        //}

        ResultSet<T> res = new ResultSet<T>(nKeys);
        for(T o: keySet()){
            res.add(o, getDouble(o));
        }
        List<ResultPair<T>> list = res.popResults();
        List<T> pile = new ArrayList<T>();
        for(ResultPair<T> rp: list){
            pile.add(rp.result);
        }
        return pile;
    }

    @Override
    public void clear() {
        rep.clear();
    }

    @Override
    public boolean containsKey(Object o) {
        return rep.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        if(!(o instanceof Integer)){return false;}
        return rep.containsValue((Integer)o);
    }

    @Override
    public Set entrySet() {
        return rep.entrySet();
    }

    @Override
    public Double get(Object o) {
        return rep.get(o);
    }

    @Override
    public boolean isEmpty() {
        return rep.isEmpty();
    }

    @Override
    public Set<T> keySet() {
        return rep.keySet();
    }

    @Override
    public Double put(T k, Double v) {
        return rep.put(k,v);
    }

    @Override
    public void putAll(Map map) {
        rep.putAll(map);
    }

    @Override
    public Double remove(Object o) {
        return rep.remove(o);
    }

    @Override
    public int size() {
        return rep.size();
    }

    @Override
    public Collection values() {
        return rep.values();
    }


}
