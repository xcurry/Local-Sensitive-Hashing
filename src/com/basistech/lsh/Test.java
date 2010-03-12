package com.basistech.lsh;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        File[] docs = docDir.listFiles(new SvnFilter());

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

    public static void testPermutation() {
        long seed = 9l; 
        int nFeats = 50;
        Permutation p = new Permutation(nFeats, seed);
        String first10 = new String();
        for (int i = 0; i < 10; ++i) {
            first10 += p.at(i) + " ";
        }
        assert(first10.equals("15 0 20 14 29 16 45 44 30 22 "));
        System.out.println("Permutation");
    }
    
    public static void testProjection() {
        long seed = 9l;
        int nFeats = 10;
        Sampler fs = new FlipSampler(seed);
        Projection pf = new Projection(nFeats, fs);
        assert(pf.toString().equals("1,-1,1,1,1,1,1,1,1,-1,"));
        Sampler as = new AchSampler(seed);
        Projection pa = new Projection(nFeats, as);
        assert(pa.toString().equals("1,0,0,1,0,0,0,-1,1,1,"));
        System.out.println("Projection");        
    }
        
    public static void testResultSet() {
        int capacity = 2;
        ResultSet rs = new ResultSet(capacity);
        
        String o1 = "key1";
        String o2 = "key2";
        String o3 = "key3";
        
        rs.add(o1, 5);
        rs.add(o2, 5);
        rs.add(o1, 3);
        rs.add(o2, 4);
        rs.add(o3, 1);
        rs.add(o3, 7);
        rs.add(o1, 9);
        String r = rs.top().toString();
        assert(r.equals("(key1,3)"));        
        rs.add(o2, 3);
        rs.add(o2, 1);
        rs.add(o1, 1);
        r = rs.top().toString();
        assert(r.equals("(key1,1)") || r.equals("(key3,1)"));
        rs.pop();
        r = rs.top().toString();
        assert(r.equals("(key1,1)") || r.equals("(key3,1)"));
        System.out.println("ResultSet");        
    }
    
    
    public static void testCosineSimilarity() {
        Map<String, Double> fv1 = new HashMap<String, Double>();
        Map<String, Double> fv2 = new HashMap<String, Double>();
        
        fv1.put("hi", 0.5);
        fv1.put("bye", 0.5);
        fv2.put("hi", 1.0);
        fv2.put("bye", 0.45);
        assert(Math.abs(CosineSimilarity.value(fv1, fv2) - 0.934d) < 0.001);    
        fv2.put("hi", 0.55);        
        assert(Math.abs(CosineSimilarity.value(fv1, fv2) - 0.995d) < 0.001);
        fv2.put("thing",50.0);
        assert(Math.abs(CosineSimilarity.value(fv1, fv2) - 0.014d) < 0.001);
        System.out.println("CosineDistance");        
    }
    
    private static class SvnFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.contains(".svn")) {
                return false;
            }
            return true;
        }
    }
    private static String usage = "test-dir";
    private static String dirName;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.print(usage + "\n");
            System.exit(1);
        }
        dirName = args[0];
        testTFIDF();
        testBitVector();
        testLogN();
        testPermutation();
        testProjection();
        testResultSet();
        testCosineSimilarity();
    }
}
