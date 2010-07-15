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

    public String reverseLookup(int i){
        for(String s: table.keySet()){
            if(table.get(s).intValue()==i){
                return s;
            }
        }
        if(syncTable!=null){
            try{
                lock.lock();
                for(String s: syncTable.keySet()){
                    if(syncTable.get(s).intValue()==i){
                        return s;
                    }
                }
            }finally{lock.unlock();}
        }
        return null;
    }
}
