/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.lang.reflect.Method;

/**
 *
 * @author cdoersch
 */
public class MainClassSwitch {
    //Invokes the main method of a class given as a command-line argument.
    //The first argument is the class name; the remaining arguments are
    //passed to the invoked class.
    public static void main(String[] args) throws Exception{
        ComputeEnvironment.setWorkingOnCluster();
        String classArg = args[0];
        String[] newArgs = new String[args.length-1];
        for(int i = 0; i<newArgs.length; i++){
            newArgs[i]=args[i+1];
        }
        Class toInvoke = Class.forName("com.basistech.lsh."+classArg);
        Method mainMethod = toInvoke.getMethod("main", (new String[]{}).getClass());
        mainMethod.invoke(null, (Object)newArgs);
    }
}
