package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * This class may be used to sort strings that have numeric sequences in them. <br>
 * <br>
 * A common problem is that sorting such strings lexicographically (as any other string) results in an order that may be
 * considered counter-intuitive, e.g. "Example10" will appear before "Example2" since '1' is lexicographically less than
 * '2'. <br>
 * <br>
 * The method compare() deals with these strings by splitting them into alternating subsequences of digits and
 * nondigits, sorting the digit sequences numerically and the nondigit sequences lexicographically. <br>
 * <br>
 * Nulls and empty strings are allowed; nulls are considered less than empty strings, and empty strings less than
 * nonempty ones. <br>
 * <br>
 * It is assumed that a string always begins with a nondigit sequence; if it actually begins with a digit sequence, the
 * behaviour is as if it started with an empty nondigit sequence.
 */
public class LexoNumericComparator implements Comparator<String>, Serializable {

    private boolean caseSensitive;

    public LexoNumericComparator(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public LexoNumericComparator() {
        this(false);
    }

    @Override
    public int compare(String str1, String str2) {
        return comp(str1, str2, caseSensitive);
    }

    public static int comp(String str1, String str2) {
        return comp(str1, str2, false);
    }

    public static int comp(String str1, String str2, boolean caseSensitive) {
        if (str1 == null) {
            return (str2 == null) ? 0 : -1;
        } else if (str2 == null) {
            return 1;
        }

        boolean digitTurn = false;
        int begSeq1 = 0;
        int begSeq2 = 0;

        while (begSeq1 != str1.length()) {

            // str1 and str2 have the same prefix but str1 has an extra sequence => str1 > str2
            if (begSeq2 == str2.length()) {
                return 1;
            }

            int endSeq1 = findEndOfSequence(str1, begSeq1, digitTurn);
            int endSeq2 = findEndOfSequence(str2, begSeq2, digitTurn);
            String seq1 = str1.substring(begSeq1, endSeq1);
            String seq2 = str2.substring(begSeq2, endSeq2);
            int compRes = compareSequence(seq1, seq2, digitTurn, caseSensitive);
            if (compRes != 0) {
                return compRes;
            }

            digitTurn = !digitTurn;
            begSeq1 = endSeq1;
            begSeq2 = endSeq2;
        }

        // str1 and str2 have the same prefix but str2 has an extra sequence => str1 < str2
        if (begSeq2 != str2.length()) {
            return -1;
        }

        // if the comparison is case-insensitive, differentiate between the strings unless they're truly identical
        return Integer.signum(str1.compareTo(str2));
    }

    private static int compareSequence(String seq1, String seq2, boolean digitSequence, boolean caseSensitive) {
        return digitSequence ? compDigitSequence(seq1, seq2, caseSensitive)
                : compNonDigitSequence(seq1, seq2, caseSensitive);
    }

    private static int compDigitSequence(String seq1, String seq2, boolean caseSensitive) {
        int compRes = new BigInteger(seq1).compareTo(new BigInteger(seq2));
        return compRes == 0 ? compNonDigitSequence(seq1, seq2, caseSensitive) : compRes;
    }

    private static int compNonDigitSequence(String seq1, String seq2, boolean caseSensitive) {
        return Integer.signum(caseSensitive ? seq1.compareTo(seq2) : seq1.compareToIgnoreCase(seq2));
    }

    private static int findEndOfSequence(String seq, int startIndex, boolean digitSequence) {
        return digitSequence ? findEndOfDigitSequence(seq, startIndex) : findEndOfNonDigitSequence(seq, startIndex);
    }

    private static int findEndOfDigitSequence(String seq, int startIndex) {
        for (int i = startIndex; i < seq.length(); ++i) {
            if (!Character.isDigit(seq.charAt(i))) {
                return i;
            }
        }
        return seq.length();
    }

    private static int findEndOfNonDigitSequence(String seq, int startIndex) {
        for (int i = startIndex; i < seq.length(); ++i) {
            if (Character.isDigit(seq.charAt(i))) {
                return i;
            }
        }
        return seq.length();
    }
}
