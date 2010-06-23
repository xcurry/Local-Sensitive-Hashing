package com.basistech.lsh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
                // TODO: good dist?
                addSymbol(sym, Math.abs(rng.nextInt() % 4) + 1);                
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
            String s = id + " = {";
            for (Entry<String, Double> e : observations.entrySet()) {
                s += e.getKey() + " " + String.format("%.4f, ", e.getValue());
            }            
            s += "}";
            return s;
        }
    }

    public class Trellis {
        public Trellis(int length, int height) {
            this.length = length;
            this.height = height;
            rep = new double[length][height];
        }
        public void extend(int length) {
            if (this.length >= length) {
                return;
            }
            int prevLength = rep.length;
            rep = Arrays.copyOf(rep, length);
            for (int i = prevLength; i < length; ++i) {
                rep[i] = new double[height];
            }
            this.length = length;
        } 
        public double get(int row, int col) {
            if (row < length && col < height) {
                return rep[row][col];
            }
            return 0.0d;
        }
        public boolean put(int row, int col, double value) {
            if (row < length && col < height) {
                rep[row][col] = value;
                return true;
            }
            return false;
        }
        public String toString() {
            String s = new String();
            if (rep != null) {
                for (int state = height - 1; state > -1; --state) {
                    for (int time = 0; time < length; ++time) {
                        s += String.format("%.4f", rep[time][state]) + " ";
                    }
                    s += "\n";
                }
            }            
            return s;
        }
        public int length;
        public int height;
        public double[][] rep;
    }
    
    private double[][] transitions;
    private List<State> states;
    private int nStates;
    private Trellis alpha;
    private Trellis beta;
    private static Random rng;
    static {
        rng = new Random(5); 
    }

    public HMM(int nStates, Set<String> observations) {
        initializeTransitions(nStates);
        initializeStates(nStates, observations);
        int nObs = observations.size();
        this.nStates = nStates; 
        alpha = new Trellis(5, nObs);
        beta = new Trellis(5, nObs);
    }

    private void initializeStates(int nStates, Set<String> observations) {
        states = new ArrayList<State>();
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
                // TODO: good dist?
                int v = Math.abs(rng.nextInt() % 4) + 1; 
                transitions[i][j] = v;
                sum += v;
            }
            for (int j = 0; j < nStates; ++j) {
                transitions[i][j] /= sum;   
            }
        }
    }

    private void printAB() {
        System.out.println("a:\n" + alpha);
        System.out.println("b:\n" + beta);
    }
    
    public double forward(List<String> obs) {
        int obsLength = obs.size();
        alpha.extend(obsLength);
        int time = -1;
        for (String sym : obs) {
            ++time;
            // initalization
            if (time == 0) {
                for (State s : states) {
                    double obsP = s.getProb(sym);
                    alpha.put(time, s.id, obsP);
                }
                continue;
            }
            // induction
            for (State currentState : states) {
                int current = currentState.id;
                double incomingAlpha = 0.0d;
                for (int previous = 0; previous < nStates; ++previous) {
                    double transP = transitions[previous][current];
                    incomingAlpha += transP * alpha.get(time - 1, previous);
                }
                double obsP = currentState.getProb(sym);
                alpha.put(time, current, obsP * incomingAlpha);
            }
        }
        double totalAlpha = 0.0d;
        for (State s : states) {
            int i = s.id;
            totalAlpha += alpha.get(time, i);
        }
        return totalAlpha;
    }

    public String toString() {
        String s = "States:\n" + states.toString() + "\n";
        s += "Transitions:\n";
        int nStates = states.size();
        for (int i = 0; i < nStates; ++i) {
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

        List<String> obs = new LinkedList<String>();
        obs.add("b");
        obs.add("b");
        obs.add("c");
        //h.printAB();
        //System.out.println("=== resize ===");
        double totalAlpha = h.forward(obs);
        h.printAB();
        System.out.println("* a_tot = " + totalAlpha);
    
    }

    public static void main(String[] args) {
        test();
    }
}
