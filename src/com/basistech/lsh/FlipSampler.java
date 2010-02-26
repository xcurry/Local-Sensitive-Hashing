package com.basistech.lsh;

public class FlipSampler extends Sampler {
    // sample {-1, 1} ~ {1/2, 1/2}
    public int draw() {
        int r = rng.nextInt(2);
        if (r == 0) {
            return -1;
        }
        return 1;
    }
}
