/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.libraries;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author MitsuyoRai
 */
public final class ConvertNumToText {

    // Static
    private static final List<String> digitsName = Arrays.asList(
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9"
    );

    private static final List<String> thousandsName = Arrays.asList(
            "",
            "nghin",
            "trieu",
            "ty",
            "nghin_ty",
            "trieu_ty",
            "ty_ty"
    );

    // Algorithm section
    /**
     * turn triplet digits into vietnamese words
     *
     * @param triplet a string of 3 digit integer
     * @param showZeroHundred whether to show Zero hundred
     * @return vietnamese string represent the input number
     */
    private static String readTriple(String triplet, boolean showZeroHundred) {
        List<Integer> digitList = stringToInt(triplet);

        int a = digitList.get(0);
        int b = digitList.get(1);
        int c = digitList.get(2);

        if (a == 0) {
            if (b == 0 && c == 0) {
                return "";
            }

            if (showZeroHundred) {
                return "0 tram " + readPair(b, c);
            }

            if (b == 0) {
                return digitsName.get(c);
            } else {
                return readPair(b, c);
            }

        }

        return digitsName.get(a) + " tram " + readPair(b, c);
    }

    private static String readPair(int b, int c) {
        String temp;

        switch (b) {
            case 0:
                return c == 0 ? "" : "le " + digitsName.get(c);
            case 1:
                switch (c) {
                    case 0:
                        return "10";
                    case 5:
                        temp = "lam";
                        break;
                    default:
                        temp = digitsName.get(c);
                        break;
                }
                return "10 " + temp;
            default:
                switch (c) {
                    case 0:
                        return digitsName.get(b) + " muoi";
                    case 1:
                        temp = "mt";
                        break;
                    case 4:
                        temp = "tu";
                        break;
                    case 5:
                        temp = "lam";
                        break;
                    default:
                        temp = digitsName.get(c);
                        break;
                }

                return digitsName.get(b) + " muoi " + temp;
        }
    }

    private static List<Integer> stringToInt(String triplet) {
        return triplet.chars().map(ConvertNumToText::charToInt)
                .boxed()
                .collect(Collectors.toList());
    }

    private static int charToInt(int c) {
        return c - '0';
    }

    /**
     * determine whether to show zero-hundred text
     *
     * Explain: count the amount of consecutive "000" at the end of the number
     * and compare it with number length
     *
     * @param groupOfThousand number represented in group of 3 digits of each
     * 1000^n
     * @return a boolean
     */
    private static boolean doShowZeroHundred(List<String> groupOfThousand) {
        int count = 0;
        int i = groupOfThousand.size() - 1;
        while (i >= 0 && groupOfThousand.get(i).equals("000")) {
            count++;
            i--;
        }

        return count < groupOfThousand.size() - 1;
    }

    public static String num2String(long num) {

        if (num == 0L) {
            return "0";
        }

        if (num < 0L) {
            return "am&" + num2String(-num);
        }

        String str = Long.toString(num);

        // zero padding in front of string to prepare for splitting
        switch (str.length() % 3) {
            case 1:
                str = "00" + str;
                break;
            case 2:
                str = "0" + str;
                break;
            default:
                break;
        }

        // Split into chunks of 3 digits each
        List<String> groupOfThousand = Arrays.asList(str.split("(?<=\\G.{3})"));

        boolean showZeroHundred = doShowZeroHundred(groupOfThousand);

        AtomicInteger index = new AtomicInteger();
        String result = groupOfThousand.stream()
                .map(triplet -> readTriple(triplet, showZeroHundred && index.get() > 0))
                .map(vnString -> vnString.trim().isEmpty() ? "" : vnString + ((groupOfThousand.size() - 1 - index.get()) == 0 ? "" : " " + thousandsName.get(groupOfThousand.size() - 1 - index.get())))
                .peek(s -> index.getAndIncrement())
                .collect(Collectors.joining(" "))
                .trim()
                .replaceAll("\\s+", "&");

        String returnValue = result.trim();
        returnValue = returnValue.replace("2&muoi", "20");
        returnValue = returnValue.replace("20&mt", "21");
        returnValue = returnValue.replace("20&lam", "25");

        returnValue = returnValue.replace("3&muoi", "30");
        returnValue = returnValue.replace("30&mt", "31");
        returnValue = returnValue.replace("30&lam", "35");

        returnValue = returnValue.replace("4&muoi", "40");
        returnValue = returnValue.replace("40&mt", "41");
        returnValue = returnValue.replace("40&lam", "45");

        returnValue = returnValue.replace("5&muoi", "50");
        returnValue = returnValue.replace("50&mt", "51");
        returnValue = returnValue.replace("50&lam", "55");

        returnValue = returnValue.replace("6&muoi", "60");
        returnValue = returnValue.replace("60&mt", "61");
        returnValue = returnValue.replace("60&lam", "65");

        returnValue = returnValue.replace("7&muoi", "70");
        returnValue = returnValue.replace("70&mt", "71");
        returnValue = returnValue.replace("70&lam", "75");

        returnValue = returnValue.replace("8&muoi", "80");
        returnValue = returnValue.replace("80&mt", "81");
        returnValue = returnValue.replace("80&lam", "85");

        returnValue = returnValue.replace("9&muoi", "90");
        returnValue = returnValue.replace("90&mt", "91");
        returnValue = returnValue.replace("90&lam", "95");

        return returnValue;
    }

    public static String num2String(long num, String refix) {
        return "".concat(refix).concat(num2String(num).replaceAll("&", "&".concat(refix)));
    }

}
