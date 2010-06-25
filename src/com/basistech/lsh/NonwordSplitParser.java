/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cdoersch
 */
public class NonwordSplitParser implements FSDParser{

    @Override
    public String[] parse(String doc) {
        //chop off nonword characters from the beginning and end of the
        //string so they don't cause empty-string tokens
        Pattern p = Pattern.compile("\\w.*\\w",Pattern.DOTALL);
        Matcher m = p.matcher(doc);
        if(!m.find()){
            return new String[]{};
        }
        doc=doc.substring(m.start(),m.end());
        doc=doc.toLowerCase();
        return doc.split("\\W+");
    }

    private void test(){
        String str = "ab cd";
        String[] strs=parse(str);
        System.out.println(strs[1]);
        str = ". ab .&^@ Cd ";
        strs=parse(str);
        System.out.println(strs[1]);
        str= "\nab\n cd ef gh ij kl mn op qr\n st uv wx yz\n ab cd";
        strs=parse(str);
        System.out.println(strs[14]);
    }

    public static void main(String[] args){
        new NonwordSplitParser().test();
        System.out.println("test finished");
    }

}
