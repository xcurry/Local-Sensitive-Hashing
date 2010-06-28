package com.basistech.lsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ParallelEM {
    private static HMM model;
    
    public static void EStep(File f) {  
        // load from TDT5DocStore
        // run model.EStep 
    }
    
    public static void mergeCounts(HMM other) {
        if (model == null) {
            model = other;
            return;
        }        
        double[][] obsCount = model.getObsCount();
        double[][] transCount = model.getTransCount();
        double[][] otherObsCount = other.getObsCount();
        double[][] otherTransCount = other.getTransCount();
        int nStates = model.getTotalStates();
        int nObs = model.getTotalObservations();
        for (int i = 0; i < nStates; ++i) {
            for (int j = 0; j < nStates; ++j) {
                transCount[i][j] += otherTransCount[i][j];                
            }
            for (int k = 0; k < nObs; ++k) {
                obsCount[i][k] += otherObsCount[i][k];                
            }            
        }
    }    
    
    public static void main(String[] args) throws Exception {
        ObjectInputStream ois = null;
        BufferedReader in = null;
        String func = args[0];
        String modelOut = args[1];
        if (func.equals("EStep")) {
            String curModel = args[2];
            ois = new ObjectInputStream(new FileInputStream(curModel));
            model = (HMM) ois.readObject();
            String fileList = args[3];
            in = new BufferedReader(new FileReader(fileList));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.err.println("* " + line);
                File f = new File(line);
                EStep(f);                
            }            
        } else if (func.equals("MStep")) {    
            String modelList = args[2];
            in = new BufferedReader(new FileReader(modelList));
            String line = null;
            while ((line = in.readLine()) != null) {
                ois = new ObjectInputStream(new FileInputStream(line));
                HMM h = (HMM) ois.readObject();
                mergeCounts(h);
            }
            model.MStep();
        } 
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelOut));
        oos.writeObject(model);
        oos.close();        
    }
}
