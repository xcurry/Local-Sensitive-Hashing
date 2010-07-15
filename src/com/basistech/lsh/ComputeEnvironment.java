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
