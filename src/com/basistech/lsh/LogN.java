package com.basistech.lsh;

public class LogN {
    private static double[] table;
    private static int size = 100000;
    private static double LOG2;

    static {
        table = new double[size];
        LOG2 = Math.log(2.0);
        for (int i = 0; i < size; ++i) {
            table[i] = log2(i);
        }	
    }

    public static double value(int n) {
        if (n < size) {
            return table[n];
        }
        return log2(n); 
    }

    private static double log2(int n) {
        return Math.log(n) / LOG2; 
    }
}
