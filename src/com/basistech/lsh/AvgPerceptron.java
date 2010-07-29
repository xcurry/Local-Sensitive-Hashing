package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

public class AvgPerceptron implements OnlineLearner {
    // private int bias; //TODO
    //TODO: This class does not support features with negative ids

    private double rate;
    private int maxFeatures;
    private boolean trainingP;
    private double[] current;
    private double[] averaged;
    private int[] lastUpdate;
    private double[] active;
    private int curRound = 0;

    public AvgPerceptron() {
        rate = 1;
        maxFeatures = 1;
        trainingP = true;
        current = new double[maxFeatures];
        averaged = new double[maxFeatures];
        lastUpdate = new int[maxFeatures];
        reallocate();
    }

    private void reallocate() {
        maxFeatures *= 2;
        current = Arrays.copyOf(current, maxFeatures);
        averaged = Arrays.copyOf(averaged, maxFeatures);
        lastUpdate = Arrays.copyOf(lastUpdate, maxFeatures);
        if (trainingP) {
            active = current;
        } else {
            active = averaged;
        }
    }

    @Override
    public void train(FeatureVector feats, int label) {
        double margin = predictMargin(feats);
        if (margin >= 0 && label == 0) {
            update(feats, -1);
        } else if (margin < 0 && label == +1) {
            update(feats, +1);
        }
    }

    @Override
    public double predictMargin(FeatureVector feats) {
        if (trainingP) {
            ++curRound;
        }
        double margin = 0.0d;
        for (int i : feats.keySet()) {
            if (i < maxFeatures) {
                margin += active[i] * feats.get(i);
            }
        }
        return margin;
    }

    @Override
    public double getPositiveThreshold() {
        return 0.0d;
    }

    private void update(FeatureVector feats, int sign) {
        double amt = sign * rate;
        for (int i : feats.keySet()) {
            while (i >= maxFeatures) {
                reallocate();
            }
            updateAvg(i, amt);
            current[i] += amt;
        }
    }

    @Override
    public void finish() {
        for (int i = 0; i < maxFeatures; ++i) {
            updateAvg(i, 0);
        }
        active = averaged;
        trainingP = false;
    }

    private void updateAvg(int i, double amt) {
        int delta = curRound - lastUpdate[i];
        if (delta > 0) {
            averaged[i] += current[i] * delta + amt;
            lastUpdate[i] = curRound;
        }
    }

    public void read(BufferedReader input) throws IOException {
        String line;
        int lineNum = 0;
        while ((line = input.readLine()) != null) {
            while (lineNum >= maxFeatures) {
                maxFeatures *= 2;
                averaged = Arrays.copyOf(averaged, maxFeatures);
            }
            averaged[lineNum] = Integer.parseInt(line);
            ++lineNum;
        }
        active = averaged;
        trainingP = false;
    }

    public void write(BufferedWriter output) throws IOException {
        for (int i = 0; i < maxFeatures; ++i) {
            output.write(Double.toString(averaged[i]));
            output.newLine();
        }
    }

    @Override
    public String toString() {
        String str = new String();
        str += "cur: ";
        for (int i = 0; i < maxFeatures; ++i) {
            str += current[i] + " ";
        }
        str += "\n";
        str += "avg: ";
        for (int i = 0; i < maxFeatures; ++i) {
            str += averaged[i] + " ";
        }
        return str;
    }

    @Override
    public String getName() {
        return "ap";
    }

    @Override
    public int predict(FeatureVector feats){
        return predictMargin(feats)>getPositiveThreshold()?1:0;
    }

    @Override
    public String print(Vocabulary vocab) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < Math.min(vocab.size(),maxFeatures); ++i) {
            str.append(vocab == null ? i : vocab.reverseLookup(i))
                    .append(": cur:")
                    .append(current[i])
                    .append(" avg:")
                    .append(averaged[i])
                    .append(" \n");
        }
        return str.toString();
    }


}
