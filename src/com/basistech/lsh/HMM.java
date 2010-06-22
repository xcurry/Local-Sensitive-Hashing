package com.basistech.lsh;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class HMM {
    public class State {
        // TODO: switch to log math
        private Map<String, Double> observations;
        public int id;
        public State(int id) {
            observations = new HashMap<String, Double>();
            this.id = id;
        }
        public State(int id, Set<String> syms) {
            this(id);
            for (String sym : syms) {
                addSymbol(sym, Math.abs(rng.nextInt()));                
            }
            normalize();
        }

        public double getProb(String sym) {
            Double p = observations.get(sym);
            if (p == null) {
                return 0.0d;
            }
            return p;
        }

        public void normalize() {
            double sum = 0.0d;
            for (double v : observations.values()) {
                sum += v;
            }
            for (Entry<String, Double> e : observations.entrySet()) {
                e.setValue(e.getValue() / sum);
            }
        }

        public void addSymbol(String sym, double prob) {
            observations.put(sym, prob);            
        }         

        public String toString() {
            String s = "state_" + id + " = {";
            for (Entry<String, Double> e : observations.entrySet()) {
                s += e.getKey() + " " + String.format("%.4f, ", e.getValue());
            }            
            s += "}";
            return s;
        }
        
        public int hashCode() {
            return id;
        }
    }
    
    private double[][] transitions;
    private Set<State> states;
    private static Random rng;
    static {
        rng = new Random(); 
    }

    public HMM(int nStates, Set<String> observations) {
        initializeTransitions(nStates);
        initializeStates(nStates, observations);
    }

    private void initializeStates(int nStates, Set<String> observations) {
        states = new HashSet<State>();
        for (int i = 0; i < nStates; ++i) {
            State s = new State(i, observations);
            states.add(s);            
        }        
    }

    private void initializeTransitions(int nStates) {
        transitions = new double[nStates][nStates];
        for (int i = 0; i < nStates; ++i) {
            double sum = 0.0d;
            for (int j = 0; j < nStates; ++j) {
                int v = Math.abs(rng.nextInt());
                transitions[i][j] = v;
                sum += v;
            }
            for (int j = 0; j < nStates; ++j) {
                transitions[i][j] /= sum;   
            }
        }
    }

    public String toString() {
        String s = "States:\n";
        for (State st : states) {
            s += "  " + st.toString() + "\n";            
        }
        
        s += "Transitions: \n";
        int nStates = states.size();
        for (int i = 0; i < nStates; ++i) {
            s += "  ";
            for (int j = 0; j < nStates; ++j) {
                s += String.format("%.4f", transitions[i][j]) + " ";
            }
            s += "\n";
        }
        return s;
    }

    public static void test() {
        Set<String> obsSyms = new HashSet<String>();
        obsSyms.add("a");
        obsSyms.add("b");
        obsSyms.add("c");
        HMM h = new HMM(3, obsSyms);
        System.out.println(h.toString());
    }

    public static void main(String[] args) {
        test();
    }
}
