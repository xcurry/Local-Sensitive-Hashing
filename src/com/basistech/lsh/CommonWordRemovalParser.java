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

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 *
 * @author cdoersch
 */
public class CommonWordRemovalParser implements FSDParser{

    private NonwordSplitParser parent = new NonwordSplitParser();
    private String deleteString = ":DELETE";

    private HashSet<String> badwords = new HashSet<String>(Arrays.asList(new String[]{
        //articles
        "the",
        "a",
        "an",
        //prepositions
        "aboard",
        "about",
        "above",
        "absent",
        "across",
        "after",
        "against",
        "along",
        "alongside",
        "amid",
        "amidst",
        "among",
        "amongst",
        "around",
        "as",
        "aside",
        "astride",
        "at",
        "athwart",
        "atop",
        "barring",
        "before",
        "behind",
        "below",
        "beneath",
        "beside",
        "besides",
        "between",
        "betwixt",
        "beyond",
        "but",
        "by",
        "circa",
        "concerning",
        "despite",
        "down",
        "during",
        "except",
        "excluding",
        "failing",
        "following",
        "for",
        "from",
        "given",
        "in",
        "including",
        "inside",
        "into",
        "like",
        "mid",
        "minus",
        "near",
        "next",
        "notwithstanding",
        "of",
        "off",
        "on",
        "onto",
        "opposite",
        "out",
        "outside",
        "over",
        "pace",
        "past",
        "per",
        "plus",
        "pro",
        "qua",
        "regarding",
        "round",
        "save",
        "since",
        "than",
        "through",
        "throughout",
        "till",
        "times",
        "to",
        "toward",
        "towards",
        "under",
        "underneath",
        "unlike",
        "until",
        "up",
        "upon",
        "versus",
        "via",
        "vice",
        "with",
        "within",
        "without",
        "worth",
        //conjunctions
        "and",
        "or",
        "but",
        "nor",
        "after",
        "how",
        "till",
        "although",
        "if",
        "unless",
        "as",
        "inasmuch",
        "until",
        "when",
        "lest",
        "whenever",
        "where",
        "wherever",
        "since",
        "while",
        "because",
        "before",
        "than",
        "that",
        "though",
        //pronouns
        "another",
        "both",
        "eachother",
        "either",
        "he",
        "her",
        "hers",
        "herself",
        "him",
        "himself",
        "his",
        "i",
        "it",
        "its",
        "itself",
        "little",
        "me",
        "mine",
        "my",
        "myself",
        "neither",
        "noone",
        "one",
        "other",
        "others",
        "our",
        "ours",
        "ourselves",
        "she",
        "that",
        "their",
        "theirs",
        "them",
        "themselves",
        "these",
        "they",
        "this",
        "those",
        "us",
        "we",
        "what",
        "whatever",
        "which",
        "whichever",
        "who",
        "whoever",
        "whom",
        "whomever",
        "whose",
        "you",
        "your",
        "yours",
        "yourself",
        "yourselves",
        "all",
        "any",
        "anybody",
        "anyone",
        "anything",
        "each",
        "everybody",
        "everyone",
        "everything",
        "few",
        "many",
        "more",
        "most",
        "much",
        "nothing",
        "none",
        "nobody",
        "some",
        "somebody",
        "someone",
        "something",
        "several",
        //days of the week
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",
        "sunday",

        //numbers
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteem",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty",
        "thirty",
        "fourty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety",
        "hundred",
        "thousand",
        "million",
        "billion",
        "trillion",
        //contractions
        "ll",
        "ve",
        "t",
        "s",
        "d",
        "re",
        "m",

        //misc
        "_"
    }));

    @Override
    public String[] parse(String doc) {
        String[] toks = parent.parse(doc);
        int count = 0;
        Pattern p = Pattern.compile("\\d+");
        for(int i = 0; i<toks.length; i++){
            if(badwords.contains(toks[i])){
                //this token matches something on the bad word list
                toks[i]=deleteString;
            }else if(p.matcher(toks[i]).matches()){
                //this token is a number
                toks[i]=deleteString;
            }else{
                count++;
            }
        }
        int idx=0;
        String[] theReturn = new String[count];
        for(int i = 0; i<toks.length; i++){
            if(toks[i]!=deleteString){
                theReturn[idx]=toks[i];
                idx++;
            }
        }
        return theReturn;
    }

}
