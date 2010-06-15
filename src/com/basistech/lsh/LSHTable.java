package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Deprecated
public class LSHTable {
    private class Storage implements Comparable<Storage> {
        public BitVector signature;
        public Object data;
        public int compareTo(Storage other) {
            return signature.compareTo(other.signature);
        }
    }

    private int dimension;
    private List<Projection> projections;
    private Permutation permutation;
    private ArrayList<Storage> table;
    private int maxDistance;
    private int beam;

    public LSHTable(int dimension, List<Projection> projections, long permutationSeed) {
        this.dimension = dimension;
        this.projections = projections;
        permutation = new Permutation(dimension, permutationSeed);
        table = new ArrayList<Storage>();	
        maxDistance = dimension + 1;
    }

    private BitVector computeSignature(FeatureVector featVec) {
        BitVector signature = new BitVector(dimension);
        int i = 0;
        for (Projection p : projections) {
            signature.set(permutation.at(i), p.bitValue(featVec));
            ++i;			
        }		
        return signature;		
    }

    public void pushBack(FeatureVector featVec, Object data) {
        Storage item = new Storage();
        item.signature = computeSignature(featVec);
        item.data = data;
        table.add(item);
    }

    public void sort() {
        Collections.sort(table);
    }

    public int getBeam() {
        return beam;
    }

    public void setBeam(int beam) {
        this.beam = beam;
    }

    public void search(HashMap<Object, Integer> results, FeatureVector featVec) {
        //TODO better search. options:  
        // 1: easier, quit searching if exceed max in ResultSet so far
        // 2: harder, pass ResultSet and work with it

        Storage item = new Storage();
        item.signature = computeSignature(featVec);
        int loc = Collections.binarySearch(table, item);
        int downLoc = loc;
        int downEnd = table.size();
        int downDistance = maxDistance;
        BitVector downSignature;
        int upLoc = loc - 1;
        int upEnd = -1;
        int upDistance = maxDistance;
        BitVector upSignature;

        if (upLoc != upEnd) {
            upSignature = table.get(upLoc).signature;
            upDistance = item.signature.hammingDist(upSignature);
        }
        if (downLoc != downEnd) {
            downSignature = table.get(downLoc).signature;
            downDistance = item.signature.hammingDist(downSignature);
        }

        while (results.size() < beam) {
            while (results.size() < beam && upDistance < downDistance && upLoc != upEnd) {
                Storage result = table.get(upLoc);
                results.put(result.data, upDistance);
                --upLoc;
                if (upLoc != upEnd) {
                    upSignature = table.get(upLoc).signature;
                    upDistance = item.signature.hammingDist(upSignature);
                } else {
                    upDistance = maxDistance;
                }				
            }
            while (results.size() < beam && downDistance < upDistance && downLoc != downEnd) {
                Storage result = table.get(downLoc);
                results.put(result.data, downDistance);
                ++downLoc;
                if (downLoc != downEnd) {
                    downSignature = table.get(downLoc).signature;
                    downDistance = item.signature.hammingDist(downSignature);
                } else {
                    downDistance = maxDistance;
                }				
            }
            if (upLoc == upEnd && downLoc == downEnd) {
                break;
            }
        }		
    }	
}
