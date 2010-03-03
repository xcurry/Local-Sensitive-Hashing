package com.basistech.lsh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Test {    

    private static void testBitVector() {
        BitVector v1 = new BitVector(2);
        BitVector v2 = new BitVector(2);
        v2.set(0, true);
        BitVector v3 = new BitVector(2);
        v3.set(1, true);
        BitVector v4 = new BitVector(2);
        v4.set(0, true);
        v4.set(1, true);

        LexCompare cmp = new LexCompare();
        List<BitVector> l = new ArrayList<BitVector>();        
        l.add(v3);
        l.add(v1);
        l.add(v2);
        l.add(v4);
        Collections.sort(l, cmp);

        assert(l.toString().equals("[{}, {1}, {0}, {0, 1}]"));
        assert(v2.hammingDist(v3) == 2 && v1.hammingDist(v2) == 1);
        System.out.println("BitVector");
    }

    private static void testLogN() {
        double tol = 1e-10;
        assert(LogN.value(0) == Double.MIN_VALUE);
        assert(Math.abs(LogN.value(1) - 0.0d) < tol);
        assert(Math.abs(LogN.value(2) - 1.0d) < tol);
        System.out.println("LogN");
    }

    private static void testTFIDF() throws IOException {
        File docDir = new File(dirName + "/TFIDF");
        File[] docs = docDir.listFiles();

        TFIDF tfidf = new TFIDF();
        tfidf.computeDocumentFrequency(docs);
        tfidf.computeTFIDF(docs);

        HashMap<String, FeatureVector> featVecs = tfidf.getTfidf();
        Vocabulary vocab = tfidf.getVocab();
        FeatureVector fv = featVecs.get("3");
        assert(Math.abs(fv.get(vocab.get("more")) - 0.5283d) < 1e-3d);
        assert(Math.abs(fv.get(vocab.get("hello")) - 0.0d) < 1e-3d);

//        for (Entry<String, FeatureVector> kv : featVecs.entrySet()) {
//            System.out.println("k: " + kv.getKey());
//            System.out.println("v: " + kv.getValue());
//        }
//        System.out.println("vocab: " + vocab.toString());   
        System.out.println("TFIDF");
    }

    public static void testPermuation() {
                
        
    }
    
    public static void testProjection() {
        
        
    }
        
    public static void testResultSet() {
        
        
    }
    
    private static String usage;
    private static String dirName;
    static {
        usage = "test-dir";        
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.print(usage + "\n");
            System.exit(1);
        }
        dirName = args[0];
        testTFIDF();
        testBitVector();
        testLogN();
    }
}
