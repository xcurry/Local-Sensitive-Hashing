/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cdoersch
 */
public class HMMFeatureExtractor {

    private int numStates;

    private Vector startProbs;
    private Matrix transProbs;
    private Vector endProbs;
    private Matrix emissionProbs;

    private int currSentence;

    public void train(List<List<Integer>> docs){
        boolean converged=false;
        List<Integer> megaSentence=new ArrayList<Integer>();
        //use -1 for the "new document" word.  Do we really need to treat it specially?
        //should punctuation get its own words?
        setSentence(megaSentence);
        while(!converged){
            Vector startProbs_save=startProbs.clone();
            Matrix transProbs_save=transProbs.clone();
            Vector endProbs_save=endProbs.clone();
            Matrix emissionProbs_save=emissionProbs.clone();
            forwardBackward();
            updateProbs();
            //check for convergence
        }
    }

    //save the sentence and create the trellis
    private void setSentence(List<Integer> doc){

    }

    //given the current state (mean parameter estimages) of the trellis,
    //update the transition/emission probs
    private void updateProbs(){

    }

    //compute trellis probabilities
    private void forwardBackward(){

    }

    public void getRepresentation(List<Integer> doc){
        setSentence(doc);
        forwardBackward();
        //compute features based on coding length
    }
}
