package com.basistech.lsh;

import java.util.Random;

public abstract class Sampler {
    protected Random rng; 

    public void setSeed(long seed) {
        rng.setSeed(seed);
    }
    public abstract int draw();
}
