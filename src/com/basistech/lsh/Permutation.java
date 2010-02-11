package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Permutation {
	private int size;
	private ArrayList<Integer> values;
	private Random rng;
	
	public Permutation(int size, long seed) {
		this.size = size;
		values = new ArrayList<Integer>(size);
		rng.setSeed(seed);

		for (int i = 0; i < size; ++i) {	
			values.set(i, i);
		}
		Collections.shuffle(values, rng);
//		int i = 0;
//		for (i = 0; i < size; ++i) {
//			values[i] = i;
//		i = size;
//		while (i > 1) {
//			int swapIndex = rng.nextInt(i);
//			int tmp = values[swapIndex];
//			--i;
//			values[swapIndex] = values[i];
//			values[i] = tmp;			
//		}		
	}

	public int at(int i) {
		assert(i < size);
		//return values[i];
		return values.get(i);
	}
}