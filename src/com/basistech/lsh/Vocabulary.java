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

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Vocabulary {
    private HashMap<String, Integer> table;
    private HashMap<String, Integer> syncTable;
    private Lock lock;
    private int id = 0;
    public static int UNDEF = -1;
    private ThreadLocal<HashMap<Integer,String>> reverseTableStore = new ThreadLocal<HashMap<Integer,String>>();

    public Vocabulary() {
        table = new HashMap<String, Integer>();        
    }

    public Vocabulary(Set<String> words) {
        this();
        for (String w : words) {
            put(w);
        }		
    }

    public void makeThreadSafe(){
        if(syncTable==null){
            syncTable=new HashMap<String, Integer>();
            lock = new ReentrantLock();
        }
    }

    public int get(String w) {
        Integer id = table.get(w);
        if (id == null) {
            if(syncTable!=null){
                try{
                    lock.lock();
                    id = syncTable.get(w);
                }finally{lock.unlock();}
            }
            if(id==null){
                return UNDEF;
            }
        }
        return id;
    }

    public int put(String w) {
        Integer i = table.get(w);
        if (i == null) {
            if(syncTable==null){
                table.put(w, id);
                ++id;
                return id - 1;
            }else{
                try{
                    lock.lock();
                    i=syncTable.get(w);
                    if(i==null){
                        syncTable.put(w, id);
                        ++id;
                        return id - 1;
                    }
                }finally{lock.unlock();}
            }
        }
        return i;		
    }

    public int size() {
        return id;		
    }

    public String toString() {
        return table.toString();        
    }

    /**
     * Warning: for debugging purposes only.  Internally, this method creates
     * a reverse lookup table--the table won't reflect subsequent puts.
     * @param i
     * @return
     */
    public String reverseLookup(int i){
        HashMap<Integer,String> reverseTable = reverseTableStore.get();
        if(reverseTable==null){
            reverseTable=new HashMap<Integer,String>();
            for(String s: table.keySet()){
                reverseTable.put(table.get(s),s);
            }
            if(syncTable!=null){
                try{
                    lock.lock();
                    for(String s: syncTable.keySet()){
                        reverseTable.put(table.get(s),s);
                    }
                }finally{lock.unlock();}
            }
            reverseTableStore.set(reverseTable);
        }
        return reverseTable.get(i);
    }
}
