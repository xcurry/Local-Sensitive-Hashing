package com.basistech.lsh;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class LSH {
    private int nTables;
    private int nFeatures;
    private int dimension;
    private int permutationSeed;
    private int projectionSeed;
    private List<Projection> projections;
    private List<LSHTable> tables;

    public List<Projection> getProjections() {
        return projections;
    }

    public void setProjections(List<Projection> projections) {
        this.projections = projections;
    }

    public void setNTables(int tables) {
        nTables = tables;
    }

    public void initialize() {
        initializeProjections();
        initializeTables();		
    }

    public void initializeProjections() {
        Sampler sampler = new FlipSampler(projectionSeed);
        this.projections = new ArrayList<Projection>();
        for (int i = 0; i < dimension; ++i) {
            projections.add(new Projection(nFeatures, sampler));
        }		
    }

    public void initializeTables() {
        for (int i = 0; i < nTables; ++i) {
            tables.add(new LSHTable(dimension, projections, permutationSeed));
        }		
    }

    public MappedPriorityQueue search(FeatureVector featVec, int nResults) {
        MappedPriorityQueue results = new MappedPriorityQueue(nResults);
        return search(featVec, nResults, results);
    }

    public MappedPriorityQueue search(FeatureVector featVec, int nResults, MappedPriorityQueue results) {
        // initialize loc & direction to search

        return results;
    }


}
