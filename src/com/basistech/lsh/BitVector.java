package com.basistech.lsh;

import java.util.BitSet;
import java.util.Comparator;

public class BitVector {
    private static Comparator<BitVector> cmp;
    static {
        cmp = new LexCompare();
    }

    private BitSet bits;

    public BitVector(int length) {
        bits = new BitSet(length);
    }

    public int hammingDist(BitVector other) {
        BitSet xored = (BitSet) bits.clone();
        xored.xor(other.bits);
        int bitsSet = xored.cardinality();
        xored = null;
        return bitsSet;
    }

    public int length() {
        return bits.length();
    }

    public int compareTo(BitVector other) {
        return cmp.compare(this, other);
    }

    public static Comparator<BitVector> getCmp() {
        return cmp;
    }

    public static void setCmp(Comparator<BitVector> cmp) {
        BitVector.cmp = cmp;
    }	

    public boolean get(int bitIndex) {
        return bits.get(bitIndex);
    }

    public void set(int bitIndex, boolean value) {
        bits.set(bitIndex, value);
    }

    public int nextSetBit(int fromIndex) {
        return bits.nextSetBit(fromIndex);
    }

    public String toString() {
        return bits.toString();
    }
}
