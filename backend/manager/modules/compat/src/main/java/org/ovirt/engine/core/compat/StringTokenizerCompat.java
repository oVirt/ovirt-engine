package org.ovirt.engine.core.compat;

import java.util.StringTokenizer;

/**
 * Wrapper class for StringTokenizer
 * @deprecated Use {@link StringTokenizer} directly instead.
 */
@Deprecated
public class StringTokenizerCompat {

    protected StringTokenizer st;

    public StringTokenizerCompat(String str) {
        st = new StringTokenizer(str);
    }

    public StringTokenizerCompat(String str, String delim) {
        st = new StringTokenizer(str, delim);
    }

    public StringTokenizerCompat(String str, String delim, boolean returnDelims) {
        st = new StringTokenizer(str, delim, returnDelims);
    }

    public boolean hasMoreTokens() {
        return st.hasMoreTokens();
    }

    public String nextToken() {
        return st.nextToken();
    }
}
