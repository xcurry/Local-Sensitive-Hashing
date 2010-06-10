package com.basistech.lsh;

import java.util.Random;

public interface Sampler {
    public abstract double draw();
    public Random getRandom();
}
