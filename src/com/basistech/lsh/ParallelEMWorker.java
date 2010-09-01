/*
  Copyright (c) 2010, Basis Technology Corp.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other 
  materials provided with the distribution.

  Neither the name of the Basis Technology Corp. nor the names of its contributors may be used to endorse or promote products derived from this software without specific 
  prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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
