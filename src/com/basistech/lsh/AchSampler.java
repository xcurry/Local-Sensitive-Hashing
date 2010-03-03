package com.basistech.lsh;

import java.util.Random;

public class AchSampler implements Sampler {
    private Random rng = new Random(); 

    public AchSampler(long seed) {        
        rng = new Random(seed);
    }

    // sample {-1, 0, 1} ~ {1/6, 2/3, 1/6}
    public int draw() {
        int r = rng.nextInt(6);
        if (r == 0) {
            return -1;
        } else if (r == 1) {
            return 1;
        } else {
            return 0;
        }
    }
}
