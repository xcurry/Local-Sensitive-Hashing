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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.basistech.lsh;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 *
 * @author cdoersch
 */
public class ComputeEnvironment {

    public static String getCompilationDate() {
        try{
            String dateVal = ResourceBundle.getBundle("com/basistech/lsh/version").getString("version.date");
            return dateVal;
        }catch(Exception e){
            return "compilation date unavailable";
        }
    }

    public static File getExecutableLocation(){try{
        File moduleFile = new File(ComputeEnvironment.class.getProtectionDomain()
                                .getCodeSource().getLocation().toURI());
        return moduleFile;
    }catch(Exception e){throw new RuntimeException(e);}}

    public static String getJavaHome(){
        return System.getProperty("java.home");
    }

    public static void main(String[] args){
        System.out.println(getCompilationDate());
    }

    private static boolean isCluster = false;
    public static void setWorkingOnCluster(){
        isCluster = true;
        removeBucketSizeCap();
        setClusterDataDirectory();
    }

    public static boolean isCluster(){
        return isCluster;
    }

    //the limit for the local environment.  On the cluster, the cap gets removed
    //via Main MethodSwitch
    private static Integer bucketSizeCap = 20;
    public static void removeBucketSizeCap(){
        bucketSizeCap = null;
    }
    public static Integer getBucketSizeCap(){
        return bucketSizeCap;
    }

    //set this to match the local environment.  On the cluster, this gets overwritten
    //via MainMethodSwitch
    private static String dataDirectory = "/home/cdoersch/data";
    public static void setClusterDataDirectory(){
        dataDirectory="/u1/fsd/data";
    }

    public static String getDataDirectory(){
        return dataDirectory;
    }


    private static File vardir;
    private static String vardirname;
    static{
        Date dateNow = new Date ();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        vardirname = "/var";//+dateformat.format(dateNow);
    }
    public static File getVarDirectory(){
        if(vardir==null){
            vardir = new File(getDataDirectory()+vardirname);
        }
        if(!vardir.exists()){
            vardir.mkdir();
        }
        return vardir;
    }
}
