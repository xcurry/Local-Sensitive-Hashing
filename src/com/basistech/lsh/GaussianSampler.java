package com.basistech.lsh;

import java.util.Random;

public class GaussianSampler implements Sampler {
    private Random rng = new Random();

    public GaussianSampler(){
    }

    public GaussianSampler(long seed) {        
        rng = new Random(seed);
    }
    
    public double draw() {
        return rng.nextGaussian();
    }
    
    public Random getRandom(){
        return rng;
    }
}
