package com.basistech.lsh;

import java.util.ArrayList;
import java.util.List;

public class ClusterVectors {
	private List<LSHTable> tables;

	private int nTables;
	private int nFeatures;
	private int dimension;
	private int permutationSeed;
	private int projectionSeed;
	private List<Projection> projections;
	
	public void setNTables(int tables) {
		nTables = tables;
	}

	public void initialize() {
		initializeProjections();
		initializeTables();		
	}
	
	public void initializeProjections() {
		Sampler sampler = new FlipSampler();
		sampler.setSeed(projectionSeed);
		this.projections = new ArrayList<Projection>();
		for (int i = 0; i < dimension; ++i) {
			projections.add(new Projection(nFeatures, sampler));
		}		
	}
	
	public void initializeTables() {
		for (int i = 0; i < nTables; ++i) {
			tables.add(new LSHTable(nFeatures, dimension, permutationSeed, projections));
		}		
	}
	
	
	
	
	
}
