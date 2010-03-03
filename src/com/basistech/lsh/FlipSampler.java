package com.basistech.lsh;

import java.util.Random;

public class FlipSampler implements Sampler {
    private Random rng = new Random(); 

    public FlipSampler(long seed) {        
        rng = new Random(seed);
    }
    
    // sample {-1, 1} ~ {1/2, 1/2}
    public int draw() {
        int r = rng.nextInt(2);
        if (r == 0) {
            return -1;
        }
        return 1;
    }
}
