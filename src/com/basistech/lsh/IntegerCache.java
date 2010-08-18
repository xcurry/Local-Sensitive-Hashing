/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
// is this just an array with positive and negative indexes?
public class IntegerCache {
    private static Integer[] positives;
    private static Integer[] negatives;

    public static void init(int minCacheVal, int maxCacheVal){
        positives = new Integer[maxCacheVal+1];
        negatives = new Integer[-minCacheVal+1];
        for(int i = 0; i<positives.length; i++){
            positives[i]=Integer.valueOf(i);
        }
        for(int i = 0; i<negatives.length; i++){
            negatives[i]=Integer.valueOf(-i);
        }
    }

    public static boolean isInit(){
        return positives!=null && negatives!=null;
    }

    public static Integer get(int i){
        if(!isInit()){return Integer.valueOf(i);}
        Integer[] getFrom;
        if(i<0){
            i=-i;
            getFrom=negatives;
        }else{
            getFrom=positives;
        }
        if(i>=getFrom.length){
            return Integer.valueOf(i);
        }else{
            return getFrom[i];
        }
    }
}
