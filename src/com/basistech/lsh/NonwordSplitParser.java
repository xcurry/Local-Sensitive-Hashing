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
        Pattern p = Pattern.compile("\\w.*\\w");
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
        assert("cd".equals(parse(str)[2]));
        str = ". ab .&^@ Cd ";
        assert("cd".equals(parse(str)[2]));
    }

    public static void main(String[] args){
        new NonwordSplitParser().test();
        System.out.println("test finished");
    }

}
