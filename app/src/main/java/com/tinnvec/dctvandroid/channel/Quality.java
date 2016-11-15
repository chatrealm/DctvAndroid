package com.tinnvec.dctvandroid.channel;

/**
 * stream quality that can be selected by user.
 *
 * Created by kev on 11/14/16.
 */
public enum Quality {
    source,
    high,
    low;

    public static String[] allAsStrings() {
        return allAsStrings(values());
    }

    /** convert to an array of string suitable for other android stuff
     *
     * @return
     */
    public static String[] allAsStrings(Quality[] enums) {
        String[] vals = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            vals[i] = enums[i].toString();
        }
        return vals;
    }
}
