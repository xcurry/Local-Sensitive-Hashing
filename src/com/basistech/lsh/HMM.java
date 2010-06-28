package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HMM {
    private static Random rng;
    static {
        rng = new Random(2);
    }
    private int totalStates;
    private int totalObservations;
    private double[][] states;
    private double[][] transitions;
    private double[][] alpha;
    private double[][] beta;
    private double[][] colCount;
    private double[][] transCount;
    private double[][] obsCount;
    private Vocabulary vocab = new Vocabulary();
    private FSDParser parser = null;
    
    private List<int[]> docs;
    

    private static double log2=Math.log(2);

    private HMM(int nStates, int nObservations) {
        this.totalStates = nStates;
        this.totalObservations = nObservations;
        initStorage();
    }

    public HMM(int nStates, List<String> docStrs, FSDParser parser){
        this.totalStates = nStates;
        this.parser=parser;
        saveDocuments(docStrs);
        this.totalObservations=vocab.size();
        initStorage();
        train(20);
    }
    //  // create HMM with 128 states, obs alphabet all unique words
    //  // parsed by parser
    // HMM h = new HMM(128, docStrings, parser);
    //  // do 5 rounds of EM on the corpus
    // h.train(5); 
    public void train(int nIters) {
        for (int it = 0; it < nIters; ++it) {
            int idx=0;
            for (int[] doc:docs) {
                if(idx%500==0){
                    System.out.println("Estep " + it + " doc number "+idx);
                    System.out.flush();
                }
                EStep(doc);
                idx++;
            }
            System.out.println("Mstep "+it);
            MStep();
        }
    }    
    
    private List<int[]> saveDocuments(List<String> docStrs){
        docs = new ArrayList<int[]>();
        //parser should never return these strings
        for(String docStr: docStrs){
            docs.add(getTokenIds(docStr));
        }
        return docs;
    }

    private void initStorage() {
        initializeStates();
        initializeTransitions();
        extendTrellis(5);
        initializeAccumulators();
    }

    private void initializeStates() {
        states = new double[totalStates][totalObservations];
        for (int i = 0; i < totalStates; ++i) {
            double total = 0.0d;
            for (int j = 0; j < totalObservations; ++j) {
                int v = Math.abs(rng.nextInt() % 4) + 1;
                states[i][j] = v;
                total += v;
            }
            for (int j = 0; j < totalObservations; ++j) {
                states[i][j] /= total;
            }
        }
    }

    private void initializeTransitions() {
        transitions = new double[totalStates][totalStates];
        for (int i = 0; i < totalStates; ++i) {
            double total = 0.0d;
            for (int j = 0; j < totalStates; ++j) {
                // TODO: good dist?
                int v = Math.abs(rng.nextInt() % 4) + 1;
                transitions[i][j] = v;
                total += v;
            }
            for (int j = 0; j < totalStates; ++j) {
                transitions[i][j] /= total;
            }
        }
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
    }

    private void forward(int[] obsSeq) {
        int obsLength = obsSeq.length;
        extendTrellis(obsLength);
        int time = 0;
        int sym = obsSeq[0];
        double alphaNorm=0;
        for (int i = 0; i < totalStates; ++i) {
            alpha[time][i] = states[i][sym];
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
                alpha[time][j] = alphaAcc * states[j][sym];
                alphaNorm+=alpha[time][j];
            }
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
                    betaAcc += beta[time + 1][j] * states[j][obsSeq[time + 1]] * transitions[i][j];
                }
                beta[time][i] = betaAcc;
                betaNorm+=beta[time][i];
            }
            for (int i = 0; i < totalStates; ++i) {
                beta[time][i]/=betaNorm;
            }
        }
    }

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
        System.err.println("a = " + a + ", b = " + b);
    }

    public void EStep(int[] obsSeq) {
        forward(obsSeq);
        backward(obsSeq);
        for (int time = 0; time < obsSeq.length-1; ++time) {
            int sym = obsSeq[time+1];
            double colTotal = 0.0d;
            for (int i = 0; i < totalStates; ++i) {
                double curAlpha = alpha[time][i];
                for (int j = 0; j < totalStates; ++j) {
                    double v = curAlpha * beta[time + 1][j] * transitions[i][j] * states[j][sym];
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
            }
        }
    }

    public void MStep() {
        for (int i = 0; i < totalStates; ++i) {
            double obsTotal = 0.0d;
            for (int sym = 0; sym < totalObservations; ++sym){
                obsTotal += obsCount[i][sym];
            }
            for (int sym = 0; sym < totalObservations; ++sym){
                states[i][sym] = obsCount[i][sym] / obsTotal;
            }

            double transTotal = 0.0d;
            for (int j = 0; j < totalStates; ++j) {
                transTotal += transCount[i][j];
            }
            for (int j = 0; j < totalStates; ++j) {
                transitions[i][j] = transCount[i][j] / transTotal;
            }
        }
        resetAccumulators();
    }

    private void resetAccumulators() {
        for (int i = 0; i < totalStates; ++i) {
            for (int j = 0; j < totalStates; ++j) {
                transCount[i][j] = 0.0d;
            }
            for (int sym = 0; sym < totalObservations; ++sym) {
                obsCount[i][sym] = 0.0d;
            }
        }
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
                s += String.format( "%d %.4f, ", sym, states[i][sym]);
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
        return s;
    }
    
    public static void test() {
        int[] obsSeq2 = {1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 0};
        int[] obsSeq1 = {0, 0, 2, 2, 2, 1, 1, 1, 0, 0, 0, 2, 2, 2, 1, 1};
        HMM h = new HMM(3, 3); // 3 states, observables {0, 1, 2}
        System.out.println(h.toString());
        for (int i = 0; i < 200; ++i) {
            h.EStep(obsSeq1);
            h.checkFB(obsSeq1.length);
            h.EStep(obsSeq2);
            h.checkFB(obsSeq2.length);
            h.MStep();    
            System.out.println("=== m-step ===");
            System.out.println(h.toString());
        }
        int[] encodeSeq={0,1,1};
        FeatureVector fv = h.getFeatures(encodeSeq);
        System.out.println(h);
        System.out.println(fv);
    }

    public static void main(String[] args) {
        test();
    }

    public FeatureVector getFeatures(String doc){
        int[] intvals = getTokenIds(doc);
        return getFeatures(intvals);
    }

    private int[] getTokenIds(String doc){
        String[] tokens=parser.parse(doc);
        int[] ret = new int[tokens.length];
        for(int i = 0; i<tokens.length; i++){
            ret[i]=vocab.put(tokens[i]);
        }
        return ret;
    }

    private FeatureVector getFeatures(int[] doc){
        forward(doc);
        backward(doc);
        double[][] transExpVals=new double[totalStates][totalStates];
        for(int t = 0; t<doc.length-1; t++){
            double[][] transProbs=new double[totalStates][totalStates];
            double norm=0;
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    transProbs[i][j]=alpha[i][t]*transitions[i][j]*states[j][doc[t+1]]*beta[j][t+1];
                    norm+=transProbs[i][j];
                }
            }
            for(int i = 0; i<totalStates; i++){
                for(int j = 0; j<totalStates; j++){
                    transExpVals[i][j]+=transProbs[i][j]/norm;
                }
            }
        }

        FeatureVector featVec = new FeatureVector();

        int transid=0;
        for(int i = 0; i<totalStates; i++){
            for(int j = 0; j<totalStates; j++){
                transid--;
                featVec.put(transid,-transExpVals[i][j]*Math.log(transitions[i][j])/log2);
            }
        }

        for(int t = 0; t<doc.length; t++){
            double norm=0;
            for(int i = 0; i<totalStates; i++){
                norm+=alpha[i][t]*beta[i][t];
            }
            double expCodeLength=0;
            for(int s=0; s<totalStates; s++){
                double prob=alpha[s][t]*beta[s][t]/norm;
                expCodeLength+=prob*-Math.log(states[s][doc[t]]*9999/(double)10000+
                                              1/Math.pow(2, 32)*1/(double)10000)/log2;
            }
            Double bits = featVec.get(doc[t]);
            if(bits==null){
                bits=0.0;
            }
            bits+=expCodeLength;
            featVec.put(doc[t],bits);
        }
        return featVec;
    }

    public double[][] getTransCount() {
        return transCount;
    }

    public double[][] getObsCount() {
        return obsCount;
    }

    public int getTotalStates() {
        return totalStates;
    }

    public int getTotalObservations() {
        return totalObservations;
    }
}
