package com.basistech.lsh;

import java.util.Comparator;

public class LexCompare implements Comparator<BitVector> {
    public int compare(BitVector bv1, BitVector bv2) {
        assert(bv1.length() == bv2.length());
        int size = bv1.length();

        int idx1 = -1;
        int idx2 = -1;
        do {
            idx1 = bv1.nextSetBit(idx1 + 1);
            idx2 = bv2.nextSetBit(idx2 + 1);
            if (idx1 == -1 && idx2 == -1) {
                break;
            }
            if (idx1 == -1 || idx1 > idx2) {
                return 1;
            } else if (idx2 == -1 || idx2 > idx1) {
                return -1;                
            }
        } while (idx1 < size && idx2 < size);

        return 0;
    }
}
