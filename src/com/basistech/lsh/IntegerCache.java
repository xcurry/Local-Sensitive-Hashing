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
