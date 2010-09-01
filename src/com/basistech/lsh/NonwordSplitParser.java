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
