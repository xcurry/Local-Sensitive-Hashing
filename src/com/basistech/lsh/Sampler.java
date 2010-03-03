package com.basistech.lsh;

import java.util.Random;

public abstract class Sampler {
    protected Random rng = new Random(); 

    public void setSeed(long seed) {        
        rng.setSeed(seed);
    }
    public abstract int draw();
}
