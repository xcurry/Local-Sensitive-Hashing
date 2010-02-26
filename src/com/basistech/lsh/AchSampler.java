package com.basistech.lsh;

public class AchSampler extends Sampler {
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
