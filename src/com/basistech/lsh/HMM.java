package com.basistech.lsh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.Random;

public class HMM implements Serializable, Cloneable{

    final static long serialVersionUID= -7338175117504339955L;

    private static Random rng;
    static {
        rng = new Random(2);
    }
    private int totalStates;
    private int totalObservations;
    private double[][] observations;
    private double[][] transitions;
    private double[][] alpha;
    private double[][] beta;
    private double[][] colCount;
    private double[][] transCount;
    private double[][] obsCount;
    private double[] prior;
    private double[] priorCount;
    private int trainingIterations = 0;
    

    private static double log2=Math.log(2);

    public HMM(int nStates, int nObservations) {
        this.totalStates = nStates;
        this.totalObservations = nObservations;
        initStorage();
    }
    
    private HMM(){}

    @Override
    public HMM clone(){
        HMM clone = new HMM();
        clone.totalStates=totalStates;
        clone.totalObservations=totalObservations;
        clone.observations=deepClone(observations);
        clone.transitions=deepClone(transitions);
        clone.alpha=deepClone(alpha);
        clone.beta=deepClone(beta);
        clone.colCount=deepClone(colCount);
        clone.transCount=deepClone(transCount);
        clone.obsCount=deepClone(obsCount);
        clone.trainingIterations=trainingIterations;
        clone.prior = prior.clone();
        clone.priorCount=priorCount.clone();
        return clone;
    }

    private double[][] deepClone(double[][] original){
        double[][] clone = new double[original.length][];
        for(int i = 0; i < original.length; i++)
                  clone[i] = (double[]) original[i].clone();
        return clone;
    }

    public int getTrainingIterations() {
        return trainingIterations;
    }

    private void initStorage() {
        initializeObservations();
        initializeTransitions();
        extendTrellis(5);
        initializeAccumulators();
    }

    private void initializeObservations() {
        observations = new double[totalStates][totalObservations];
        prior=new double[totalStates];
        for (int i = 0; i < totalStates; ++i) {
            double total = 0.0d;
            prior[i]=1.0d/totalStates;
            for (int j = 0; j < totalObservations; ++j) {
                int v = Math.abs(rng.nextInt() % 4) + 1;
                observations[i][j] = v;
                total += v;
            }
            for (int j = 0; j < totalObservations; ++j) {
                observations[i][j] /= total;
            }
        }
    }

    private void initializeTransitions() {
        transitions = new double[totalStates][totalStates];
        for (int i = 0; i < totalStates; ++i) {
            double total = 0.0d;
            for (int j = 0; j < totalStates; ++j) {
                int v = Math.abs(rng.nextInt() % 4) + 1;
                transitions[i][j] = v;
                total += v;
            }
            for (int j = 0; j < totalStates; ++j) {
                transitions[i][j] /= total;
            }
        }
    }
    
    // attempt to do clever initialization
    public void bumpStates(Queue<Integer> wordsToBump){
        for(int i = 0; i<totalStates; i++){
            bumpState(i, wordsToBump.poll());
        }
    }

    // give word half the probability of emission from state
    private void bumpState(int state, int word){
        for(int j = 0; j<totalObservations; j++){
            observations[state][j]/=.5;
        }
        observations[state][word]+=.5;
    }

    private void extendTrellis(int length) {
        if (alpha == null) {
            alpha = new double[length][totalStates];
            beta = new double[length][totalStates];
        } else if (alpha.length >= length) {
            return;
        } else {
            int prevLength = alpha.length;
            alpha = Arrays.copyOf(alpha, length);
            beta = Arrays.copyOf(beta, length);
            for (int i = prevLength; i < length; ++i) {
                alpha[i] = new double[totalStates];
                beta[i] = new double[totalStates];
            }
        }
    }

    private void initializeAccumulators() {     
        colCount = new double[totalStates][totalStates];
        transCount = new double[totalStates][totalStates];
        obsCount = new double[totalStates][totalObservations];
        priorCount = new double[totalStates];
    }

    private void forward(int[] obsSeq) {
        int obsLength = obsSeq.length;
        extendTrellis(obsLength);
        int time = 0;
        int sym = obsSeq[0];
        double alphaNorm=0;
        for (int i = 0; i < totalStates; ++i) {
            //if the symbol wasn't in the training corpus, then assume uniform probability
            alpha[time][i] = (sym<observations[0].length?observations[i][sym]:1)*prior[i];
            alphaNorm+=alpha[time][i];
        }
        for (int i = 0; i < totalStates; ++i) {
            alpha[time][i]/=alphaNorm;
        }
        ++time;
        for (; time < obsLength; ++time) {
            sym = obsSeq[time];
            alphaNorm=0;
            for (int j = 0; j < totalStates; ++j) {
                double alphaAcc = 0.0d;
                for (int i = 0; i < totalStates; ++i) {
                    alphaAcc += alpha[time - 1][i] * transitions[i][j];
                }
                try{
                    alpha[time][j] = alphaAcc * (sym<observations[0].length?observations[j][sym]:1);
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("alpha:"+alpha.length+","+alpha[0].length+" time:"+time+" j:"+j+" states:"+observations.length+","+observations[0].length+" sym:"+sym);
                    throw e;
                }
                alphaNorm+=alpha[time][j];
            }
            // normalize to avoid underflow
            for (int j = 0; j < totalStates; ++j) {
                alpha[time][j]/=alphaNorm;
            }
        }
    }

    private void backward(int[] obsSeq) {
        int obsLength = obsSeq.length;
        extendTrellis(obsLength);
        int time = obsLength - 1;
        for (int i = 0; i < totalStates; ++i) {
            beta[time][i] = 1.0/totalStates;
        }
        --time;
        for (; time > -1; --time) {
            double betaNorm=0;
            for (int i = 0; i < totalStates; ++i) {
                double betaAcc = 0.0d;
                for (int j = 0; j < totalStates; ++j) {
                    int sym=obsSeq[time+1];
                    //if the symbol wasn't in the training corpus, then assume uniform probability
                    betaAcc += beta[time + 1][j] * (sym<observations.length?observations[j][sym]:1) * transitions[i][j];
                }
                beta[time][i] = betaAcc;
                betaNorm+=beta[time][i];
            }
            // normalize to avoid underflow
            for (int i = 0; i < totalStates; ++i) {
                beta[time][i]/=betaNorm;
            }
        }
    }

    // for debugging, normalization of columns to avoid underflow invalidates
    private void checkFB(int length) {
        double a = 0.0d;
        length--;
        for (int i = 0; i < totalStates; ++i) {
            a += alpha[length][i] * beta[length][i];
        }
        double b = 0.0d;
        for (int i = 0; i < totalStates; ++i) {
            b += beta[0][i] * alpha[0][i];
        }
        //System.err.println("a = " + a + ", b = " + b);
    }

    public void EStep(int[] obsSeq) {
        if(obsSeq.length==0){
            //never underestimate TDT5's capacity to be ugly.  There are actually
            //documents in the unannotated set that contain no words.
            return;
        }
        forward(obsSeq);
        backward(obsSeq);
        for (int time = 0; time < obsSeq.length-1; ++time) {
            int sym = obsSeq[time+1];
            double colTotal = 0.0d;
            for (int i = 0; i < totalStates; ++i) {
                double curAlpha = alpha[time][i];
                for (int j = 0; j < totalStates; ++j) {
                    double v = curAlpha * beta[time + 1][j] * transitions[i][j] * observations[j][sym];
                    colCount[i][j] = v;
                    colTotal += v;
                }
            }
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    transCount[i][j] += colCount[i][j] / colTotal;
                }
            }
        }
        for(int time=0; time<obsSeq.length; time++){
            int sym=obsSeq[time];
            double total = 0.0d;
            for(int i = 0; i<totalStates; i++){
                total += alpha[time][i] * beta[time][i];
            }
            for(int i = 0; i<totalStates; i++){
                obsCount[i][sym] += alpha[time][i] * beta[time][i] / total;
                if(time==0){
                    priorCount[i]+=alpha[time][i] * beta[time][i] / total;
                }
            }
        }
    }

    public void MStep() {
        double priorTotal = 0.0d;
        for (int i = 0; i < totalStates; ++i) {
            double obsTotal = 0.0d;
            prior[i]=priorCount[i]+1;
            priorTotal+=prior[i];
            for (int sym = 0; sym < totalObservations; ++sym){
                //obsCount[i][sym]+=.01;
                obsTotal += obsCount[i][sym];
            }
            for (int sym = 0; sym < totalObservations; ++sym){
                observations[i][sym] = obsCount[i][sym] / obsTotal;
            }

            double transTotal = 0.0d;
            for (int j = 0; j < totalStates; ++j) {
                transCount[i][j]+=1;
                transTotal += transCount[i][j];
            }
            for (int j = 0; j < totalStates; ++j) {
                transitions[i][j] = transCount[i][j] / transTotal;
            }
        }
        for(int i = 0; i<totalStates; i++){
            prior[i]/=priorTotal;
        }
        resetAccumulators();
        trainingIterations++;
    }

    public void resetAccumulators() {
        for (int i = 0; i < totalStates; ++i) {
            for (int j = 0; j < totalStates; ++j) {
                transCount[i][j] = 0.0d;
            }
            for (int sym = 0; sym < totalObservations; ++sym) {
                obsCount[i][sym] = 0.0d;
            }
            priorCount[i]=0;
        }
    }

    public void mergeAccumulators(HMM other){
        double[][] otherObsCount = other.obsCount;
        double[][] otherTransCount = other.transCount;
        int nStates = getTotalStates();
        int nObs = getTotalObservations();
        for (int i = 0; i < nStates; ++i) {
            for (int j = 0; j < nStates; ++j) {
                transCount[i][j] += otherTransCount[i][j];
            }
            for (int k = 0; k < nObs; ++k) {
                obsCount[i][k] += otherObsCount[i][k];
            }
            priorCount[i]+=other.priorCount[i];
        }
    }

    public String printStatePopularity(){
        double[] pop1 = getStatePopularity();
        String str = "";
        for(int i = 0; i<totalStates; i++){
            str+=(i+": mean:"+pop1[i]+" prior:"+prior[i] +"\n");
        }
        return str;
    }

    private double[] getStatePopularity(){
        double[] pop1 = new double[totalStates];
        for(int i = 0; i<totalStates; i++){
            pop1[i]=prior[i];//1.0d/totalStates;
        }
        for(int t = 0; t<100; t++){
            double[] pop2 = new double[totalStates];
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    pop2[j]+=pop1[i]*transitions[i][j];
                }
            }
            pop1=pop2;
        }
        return pop1;
    }

    @Override
    public String toString() {
        String s = "Alpha:\n";
        for (int i = 0; i < alpha.length; ++i) {
            for (int j = 0; j < totalStates; ++j) {
                s += String.format("%.4f ", alpha[i][j]);
            }
            s += "\n";
        }
        s += "Beta:\n"; 
        for (int i = 0; i < beta.length; ++i) {
            for (int j = 0; j < totalStates; ++j) {
                s += String.format("%.4f ", beta[i][j]);
            }
            s += "\n";
        }
        s += "States:\n";
        for (int i = 0; i < totalStates; ++i) {
            s += i + ": ";
            for (int sym = 0; sym < totalObservations; ++sym) {
                s += String.format( "%d %.4f, ", sym, observations[i][sym]);
            }
            s += "\n";
        }
        s += "Transitions:\n";
        for (int i = 0; i < totalStates; ++i) {
            for (int j = 0; j < totalStates; ++j) {
                s += String.format("%.4f ", transitions[i][j]);
            }
            s += "\n";
        }
        s += "prior:\n";
        for(int i = 0; i<totalStates; i++){
            s += i+": "+String.format("%.4f ",prior[i])+"\n";
        }
        return s;
    }

    public String printStatesCompact(Vocabulary v){
        StringBuilder s = new StringBuilder();

        class keyProbPair implements Comparable<keyProbPair>{
            public int key;
            public Double prob;

            public int compareTo(keyProbPair o) {
                return -prob.compareTo(o.prob);
            }
        }
        for(int i = 0; i<totalStates; i++){
            System.out.println(i);
            s.append("\n"+i+":\n");
            ArrayList<keyProbPair> keys = new ArrayList<keyProbPair>();
            for(int sym = 0; sym<totalObservations; sym++){
                keyProbPair kpp = new keyProbPair();
                kpp.key=sym;
                kpp.prob=observations[i][sym];
                keys.add(kpp);
            }
            Collections.sort(keys);
            for(int j=0; j<40; j++){
                keyProbPair key = keys.get(j);
                s.append(String.format("%10f:%s\n",key.prob,v.reverseLookup(key.key)));
            }
        }
        return s.toString();
    }
    
    public static void test() {
        int[] obsSeq2 = {1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0};
        int[] obsSeq1 = {1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1};
        HMM h = new HMM(10, 3); // 3 states, observables {0, 1, 2}
        System.out.println(h.toString());
        for (int i = 0; i < 200; ++i) {
            for(int j=0; j<500; j++){
                h.EStep(obsSeq1);
                h.checkFB(obsSeq1.length);
                h.EStep(obsSeq2);
                h.checkFB(obsSeq2.length);
            }
            h.MStep();    
            System.out.println("=== m-step ===");
            System.out.println(h.toString());
        }
        int[] encodeSeq={0,2,1};
        FeatureVector fv = h.getFeatures(encodeSeq);
        System.out.println(h);
        System.out.println(fv);
    }

    public static void main(String[] args) {
        test();
    }

    //private Vocabulary v;
    //public void setVocab(Vocabulary v){this.v=v;}


    public FeatureVector getFeatures(int[] doc){
        if(!IntegerCache.isInit()){
            IntegerCache.init(-totalStates*totalStates, totalObservations);
        }
        forward(doc);
        backward(doc);
        double[][] transExpVals=new double[totalStates][totalStates];
        for(int t = 0; t<doc.length-1; t++){
            double[][] transProbs=new double[totalStates][totalStates];
            double norm=0;
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    int sym = doc[t+1];
                    transProbs[i][j]=alpha[t][i]*transitions[i][j]*
                    (sym<observations[0].length?observations[j][sym]:1)*
                    beta[t+1][j];
                    norm+=transProbs[i][j];
                }
            }
            //double stCodeLen=0;
            //System.out.print(v.reverseLookup(doc[t])+" ");
            //double expCodeLength=0;
            //double norm2=0;
            //for(int i = 0; i<totalStates; i++){
            //    norm2+=alpha[t][i]*beta[t][i];
            //}
            //for(int s=0; s<totalStates; s++){
            //    double prob=alpha[t][s]*beta[t][s]/norm2;
            //    expCodeLength+=prob*-Math.log(states[s][doc[t]]*9999/(double)10000+
            //                                  1/Math.pow(2, 32)*1/(double)10000)/log2;
            //}
            //System.out.println(expCodeLength);
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    transExpVals[i][j]+=transProbs[i][j]/norm;
            //        stCodeLen+=-transProbs[i][j]/norm*Math.log(transitions[i][j])/log2;
                }
            }
            //System.out.println(stCodeLen);
        }
        //System.exit(0);

        FeatureVector featVec = new FeatureVector();

        // feature set 1: for each transition in model, expected code length (entropy) for this doc
        int transid=0;
        for(int i = 0; i<totalStates; i++){
            for(int j = 0; j<totalStates; j++){
                transid--; //perhaps negative vals in IntegerCache are for set 1?
                double val = -transExpVals[i][j]*Math.log(transitions[i][j])/log2;
                if(!Double.isNaN(val)){
                    featVec.put(IntegerCache.get(transid),val);
                }//if val isNaN, then we underflowed (the transition probably never happened).
                 //just leave it out.
            }
        }

        // feature set 2: for each word in doc, total code length (entropy)
        for(int t = 0; t<doc.length; t++){
            double norm=0;
            for(int i = 0; i<totalStates; i++){
                norm+=alpha[t][i]*beta[t][i];
            }
            double expCodeLength=0;
            for(int s=0; s<totalStates; s++){
                double prob=alpha[t][s]*beta[t][s]/norm;
                double emissProb = (doc[t]<observations[0].length?observations[s][doc[t]]:1);
                expCodeLength+=prob*-Math.log(emissProb*9999/(double)10000+
                                              1/Math.pow(2, 32)*1/(double)10000)/log2;
            }
            Double bits = featVec.get(doc[t]);
            if(bits==null){
                bits=0.0;
            }
            bits+=expCodeLength;
            featVec.put(IntegerCache.get(doc[t]),bits);
        }
        return featVec;
    }

    public int getTotalStates() {
        return totalStates;
    }

    public int getTotalObservations() {
        return totalObservations;
    }
}
