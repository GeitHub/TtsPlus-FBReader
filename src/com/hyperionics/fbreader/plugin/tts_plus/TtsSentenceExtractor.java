/*
 * Copyright (C) 2012 Greg Kochaniak <gregko@hyperionics.com>
 *
 */

package com.hyperionics.fbreader.plugin.tts_plus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class TtsSentenceExtractor {

    private TtsSentenceExtractor() {} // singleton, prevents instantiation of this class's objects

    public static class SentenceIndex {
        public String s;
        public int i;

        public SentenceIndex(String ss, int ii) {
            s = ss;
            i = ii;
        }
    }

    public static String[] extract(String paragraph, Locale loc) {
        paragraph = paragraph.replace(". . .", "...");
        paragraph = replaceEngAbbreviations(paragraph, loc);
        final Pattern p = Pattern.compile("[\\.\\!\\?]\\s+", Pattern.MULTILINE);
        String[] sentences = p.split(paragraph);
        int len = 0;
        for (int i = 0; i < sentences.length; i++) {
            len = paragraph.indexOf(sentences[i], len) + sentences[i].length();
            if (paragraph.length() > len) {
                sentences[i] += paragraph.substring(len, len+1);
            }
            len ++;
        }

        return sentences;
    }

    public static SentenceIndex[] build(List<String> wl, ArrayList<Integer> il, Locale loc) {
        ArrayList<String> ss = new ArrayList<String>();
        ArrayList<Integer> inds = new ArrayList<Integer>();
        String currSent = "";
        int i, indToAdd = 0;

        for (i = 0; i < wl.size(); i++) {
            boolean endSentence = false;
            String w = wl.get(i);
            int len = currSent.length();
            if (len == 0)
                indToAdd = il.get(i);
            w = replaceEngAbbreviations(w, loc);
            char lastCh = w.charAt(w.length() - 1);
            endSentence = lastCh == '.' && (i == wl.size()-1 || !wl.get(i+1).equals(".")) ||
                          lastCh == '!' || lastCh == '?';
            if (!currSent.equals("") && (w.length() > 1 || !endSentence) && currSent.charAt(currSent.length()-1) != '.')
                currSent += " ";
            currSent += w;
            if (endSentence || i == wl.size()-1) {
                ss.add(currSent);
                inds.add(indToAdd);
                currSent = "";
            }
        }

        SentenceIndex[] sentences = new SentenceIndex[ss.size()];
        for (i = 0; i < ss.size(); i++) {
            sentences[i] = new SentenceIndex(ss.get(i), inds.get(i));
        }
        return sentences;
    }

    /**
     * Replaces common English abbreviations that end with a dot, with equivalents without a dot
     * that the engine pronounces correctly, avoiding a dot aids in correct splitting into
     * sentences.
     * Eventually this method should take an external file or resource, that a user could edit
     * to correct pronunciation different words. Should take into account the locale as well.
     * 
     * @param inStr - input String
     * @return - String with abbreviations replaced
     */
    private static String replaceEngAbbreviations(String inStr, Locale loc) {
        // spelling is not important here, pronunciation by TTS engine is.
        if (loc == null)
            return inStr;

        String lang = loc.getLanguage();
        if (!lang.equals("eng"))
            return inStr;

        inStr = inStr.replace("Mr.", "Mr ");
        inStr = inStr.replace("Mrs.", "Mrs ");
        inStr = inStr.replace("Dr.", "Dr "); // we don't know if it's "Doctor" or "Drive"
        inStr = inStr.replace("Prof.", "Prof ");
        inStr = inStr.replace("i.e.", "I E ");
        inStr = inStr.replace("Rev.", "Rev ");
        inStr = inStr.replace("Gen.", "General ");
        inStr = inStr.replace("St.", "S T "); // we don't know if it's "Saint" or "Street"...
        inStr = inStr.replace("Rep.", "Representative ");
        inStr = inStr.replace("Ph.D.", "Ph.D ");
        inStr = inStr.replace("Sr.", "Senior ");
        inStr = inStr.replace("Jr.", "Junior ");
        inStr = inStr.replace("M.D.", "M D ");
        inStr = inStr.replace("B.A.", "B A ");
        inStr = inStr.replace("M.A.", "M A ");
        inStr = inStr.replace("D.D.S. ", "D D S ");
        inStr = inStr.replace("H.M.", "H M ");
        inStr = inStr.replace("H.M.S.", "H M S ");

        // Greg's private replacemtns... Move into preferences...
        inStr = inStr.replace("antiaging", "anti-aging");
        inStr = inStr.replace("Antiaging", "Anti-aging");
        inStr = inStr.replace("No.", "No;");
        return inStr;
    }
}
