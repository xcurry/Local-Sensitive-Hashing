package com.basistech.lsh;

import java.util.HashMap;
import java.util.Set;

public class Vocabulary {
    private HashMap<String, Integer> table;
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

    public int get(String w) {
        Integer id = table.get(w);
        if (id == null) {
            return UNDEF;
        }
        return id;
    }

    public int put(String w) {
        Integer i = table.get(w);
        if (i == null) {
            table.put(w, id);
            ++id;
            return id - 1;
        }
        return i;		
    }

    public int size() {
        return id;		
    }
    
    public String toString() {
        return table.toString();        
    }
}
