/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.libraries;

/**
 *
 * @author MitsuyoRai
 */
public final class ConvertTextToText {

    public static String TextToText(String string) {
        char[] ch;
        ch = string.replaceAll("/(_|-|:|;|<|>|,|~|@|#|$|%|^)/", string).toCharArray();
        String words = "";
        for (int i = 0; i < ch.length; i++) {
            words = words.concat(ch[i] + "&");
        }
        return words.substring(0, words.length() - 1);
    }

    public static String TextToText(String string, String refix) {
        char[] ch;
        ch = string.replaceAll("/(_|-|:|;|<|>|,|~|@|#|$|%|^)/", string).toCharArray();
        String words = "";
        for (int i = 0; i < ch.length; i++) {
            words = words.concat(refix).concat(ch[i] + "&");
        }
        return words.substring(0, words.length() - 1);
    }

}
