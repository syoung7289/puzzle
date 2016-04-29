package com.scyoung.puzzlemethis.Util;

/**
 * Created by scyoung on 4/29/16.
 */
public class StringUtil {

    public static String convertToCondensedUpperCase(String stringToConvert) {
        return stringToConvert.toUpperCase().replaceAll("\\s+", "_");
    }

    public static String convertToTitleCase(String stringToConvert) {
        String tc = stringToConvert;
        if (tc.length() > 0) {
            String[] arr = stringToConvert.split(" ");
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < arr.length; i++) {
                if (arr[i].length() > 0) {
                    sb.append(Character.toUpperCase(arr[i].charAt(0)))
                            .append(arr[i].substring(1)).append(" ");
                }
            }
            tc = sb.toString().trim();
        }
        return tc;
    }

    public static String convertFromCondensedUpperCase(String stringToConvert) {
        String spacedString = stringToConvert.toLowerCase().replaceAll("_", " ");
        return convertToTitleCase(spacedString);
    }
}
