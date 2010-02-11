package com.basistech.lsh;

public class Projection {
	private int size;
	private int[] rep;
	
	public Projection(int size, Sampler sampler) {
		assert(size > 0);
		this.size = size;
		rep = new int[size];
		for (int i = 0; i < size; ++i) {
			rep[i] = sampler.draw();
		}
	}
	
	public boolean bitValue(FeatureVector featVec) {
		double dotProduct = 0.0d;
		for (int featId : featVec.keySet()) {
			double featValue = featVec.get(featId);
			dotProduct += featValue * rep[featId];			
		}		
		return dotProduct > 0.0d;
	}
	
	public String toString() {
		String str = new String();
		for (int i = 0; i < size; ++i) {
			str += rep[i] + ",";
		}
		return str;
	}	
}
