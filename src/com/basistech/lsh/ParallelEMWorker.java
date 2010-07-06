package com.basistech.lsh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ParallelEMWorker {
    private static HMM model;
    
    public static void main(String[] args) throws Exception {
        ObjectInputStream ois = null;
        ObjectInputStream docs = null;
        int id = Integer.valueOf(args[0]);
        File curModel = new File(args[1]);
        File docFile = new File(args[2]);
        File saveDir = new File(args[3]);

        ois = new ObjectInputStream(new FileInputStream(curModel));
        model = (HMM) ois.readObject();

        System.out.println("ParallelEMWorker:" + docFile.getAbsolutePath());
        docs = new ObjectInputStream(new FileInputStream(docFile));
        int[] doc = null;
        int processed = 0;
        while (true) {
            try{
                doc = (int[])docs.readObject();
            }catch(IOException e){break;}
            model.EStep(doc);
            processed++;
        }
        System.out.println("ParallelEMWorker: #processed=" + processed);
        File modelOut = new File(saveDir,"estep_"+id);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelOut));
        oos.writeObject(model);
        oos.flush();
        oos.close();
        File finalModelOut = new File(saveDir,"estep_"+id+"_final");
        modelOut.renameTo(finalModelOut);
    }
}
