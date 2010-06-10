package com.basistech.lsh;

import java.util.Random;

public class GaussianSampler implements Sampler {
    private Random rng = new Random(); 

    public GaussianSampler(long seed) {        
        rng = new Random(seed);
    }
    
    // sample {-1, 1} ~ {1/2, 1/2}
    public double draw() {
        return rng.nextGaussian();
    }
    
    public Random getRandom(){
        return rng;
    }
}
