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
