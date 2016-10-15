package org.ovirt.engine.core.compat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * A port of java.util.Formatter to GWT, without GWT non-compatible types (especially Calendar and Precision
 * calculations)
 */
public final class FormatterJava {
    private enum BigDecimalLayoutForm {
        DECIMAL_FLOAT,
        SCIENTIFIC
    }

    private static class Conversion {
        // if (arg.TYPE != boolean) return boolean
        // if (arg != null) return true; else return false;
        static final char BOOLEAN = 'b';
        static final char BOOLEAN_UPPER = 'B';
        // Character, Byte, Short, Integer
        // (and associated primitives due to autoboxing)
        static final char CHARACTER = 'c';
        static final char CHARACTER_UPPER = 'C';

        // java.util.Date, java.util.Calendar, long
        static final char DATE_TIME = 't';
        static final char DATE_TIME_UPPER = 'T';
        static final char DECIMAL_FLOAT = 'f';
        // Byte, Short, Integer, Long, BigInteger
        // (and associated primitives due to autoboxing)
        static final char DECIMAL_INTEGER = 'd';
        static final char GENERAL = 'g';
        static final char GENERAL_UPPER = 'G';
        // arg.hashCode()
        static final char HASHCODE = 'h';

        static final char HASHCODE_UPPER = 'H';
        static final char HEXADECIMAL_FLOAT = 'a';

        static final char HEXADECIMAL_FLOAT_UPPER = 'A';
        static final char HEXADECIMAL_INTEGER = 'x';

        static final char HEXADECIMAL_INTEGER_UPPER = 'X';
        static final char LINE_SEPARATOR = 'n';
        static final char OCTAL_INTEGER = 'o';
        static final char PERCENT_SIGN = '%';
        // Float, Double, BigDecimal
        // (and associated primitives due to autoboxing)
        static final char SCIENTIFIC = 'e';
        static final char SCIENTIFIC_UPPER = 'E';

        // if (arg instanceof Formattable) arg.formatTo()
        // else arg.toString();
        static final char STRING = 's';
        static final char STRING_UPPER = 'S';

        // Returns true iff the Conversion is applicable to character.
        static boolean isCharacter(char c) {
            switch (c) {
            case CHARACTER:
            case CHARACTER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is a floating-point type.
        static boolean isFloat(char c) {
            switch (c) {
            case SCIENTIFIC:
            case SCIENTIFIC_UPPER:
            case GENERAL:
            case GENERAL_UPPER:
            case DECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is applicable to all objects.
        static boolean isGeneral(char c) {
            switch (c) {
            case BOOLEAN:
            case BOOLEAN_UPPER:
            case STRING:
            case STRING_UPPER:
            case HASHCODE:
            case HASHCODE_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is an integer type.
        static boolean isInteger(char c) {
            switch (c) {
            case DECIMAL_INTEGER:
            case OCTAL_INTEGER:
            case HEXADECIMAL_INTEGER:
            case HEXADECIMAL_INTEGER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion does not require an argument
        static boolean isText(char c) {
            switch (c) {
            case LINE_SEPARATOR:
            case PERCENT_SIGN:
                return true;
            default:
                return false;
            }
        }

        static boolean isValid(char c) {
            return isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)
                    || c == 't' || isCharacter(c);
        }
    }

    private static class DateTime {
        static final char AM_PM = 'p'; // (am or pm)
        static final char CENTURY = 'C'; // (00 - 99)
        // (Sat Nov 04 12:02:33 EST 1999)
        static final char DATE = 'D'; // (mm/dd/yy)
        // * static final char LOCALE_TIME = 'X'; // (%H:%M:%S) - parse format?
        static final char DATE_TIME = 'c';
        static final char DAY_OF_MONTH = 'e'; // (1 - 31) -- like d
        static final char DAY_OF_MONTH_0 = 'd'; // (01 - 31)
        static final char DAY_OF_YEAR = 'j'; // (001 - 366)
        static final char HOUR = 'l'; // (1 - 12) -- like I
        static final char HOUR_0 = 'I'; // (01 - 12)
        static final char HOUR_OF_DAY = 'k'; // (0 - 23) -- like H
        static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)
        static final char ISO_STANDARD_DATE = 'F'; // (%Y-%m-%d)
        static final char MILLISECOND = 'L'; // jdk, not in gnu (000 - 999)
        static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)

        static final char MINUTE = 'M'; // (00 - 59)
        static final char MONTH = 'm'; // (01 - 12)
        static final char NAME_OF_DAY = 'A'; // 'A'
        // Date
        static final char NAME_OF_DAY_ABBREV = 'a'; // 'a'
        static final char NAME_OF_MONTH = 'B'; // 'B'
        static final char NAME_OF_MONTH_ABBREV = 'b'; // 'b'
        // * static final char ISO_WEEK_OF_YEAR_2 = 'g'; // cross %y %V
        // * static final char ISO_WEEK_OF_YEAR_4 = 'G'; // cross %Y %V
        static final char NAME_OF_MONTH_ABBREV_X = 'h'; // -- same b
        static final char NANOSECOND = 'N'; // (000000000 - 999999999)
        static final char SECOND = 'S'; // (00 - 60 - leap second)
        static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)
        static final char TIME = 'T'; // (24 hour hh:mm:ss)
        // Composites
        static final char TIME_12_HOUR = 'r'; // (hh:mm:ss [AP]M)

        static final char TIME_24_HOUR = 'R'; // (hh:mm same as %H:%M)
        // * static final char DAY_OF_WEEK_1 = 'u'; // (1 - 7) Monday
        // * static final char WEEK_OF_YEAR_SUNDAY = 'U'; // (0 - 53) Sunday+
        // * static final char WEEK_OF_YEAR_MONDAY_01 = 'V'; // (01 - 53) Monday+
        // * static final char DAY_OF_WEEK_0 = 'w'; // (0 - 6) Sunday
        // * static final char WEEK_OF_YEAR_MONDAY = 'W'; // (00 - 53) Monday
        static final char YEAR_2 = 'y'; // (00 - 99)
        static final char YEAR_4 = 'Y'; // (0000 - 9999)
        static final char ZONE = 'Z'; // (symbol)
        static final char ZONE_NUMERIC = 'z'; // (-1200 - +1200) - ls minus?

        // * static final char LOCALE_DATE = 'x'; // (mm/dd/yy)

        static boolean isValid(char c) {
            switch (c) {
            case HOUR_OF_DAY_0:
            case HOUR_0:
            case HOUR_OF_DAY:
            case HOUR:
            case MINUTE:
            case NANOSECOND:
            case MILLISECOND:
            case MILLISECOND_SINCE_EPOCH:
            case AM_PM:
            case SECONDS_SINCE_EPOCH:
            case SECOND:
            case TIME:
            case ZONE_NUMERIC:
            case ZONE:

                // Date
            case NAME_OF_DAY_ABBREV:
            case NAME_OF_DAY:
            case NAME_OF_MONTH_ABBREV:
            case NAME_OF_MONTH:
            case CENTURY:
            case DAY_OF_MONTH_0:
            case DAY_OF_MONTH:
                // * case ISO_WEEK_OF_YEAR_2:
                // * case ISO_WEEK_OF_YEAR_4:
            case NAME_OF_MONTH_ABBREV_X:
            case DAY_OF_YEAR:
            case MONTH:
                // * case DAY_OF_WEEK_1:
                // * case WEEK_OF_YEAR_SUNDAY:
                // * case WEEK_OF_YEAR_MONDAY_01:
                // * case DAY_OF_WEEK_0:
                // * case WEEK_OF_YEAR_MONDAY:
            case YEAR_2:
            case YEAR_4:

                // Composites
            case TIME_12_HOUR:
            case TIME_24_HOUR:
                // * case LOCALE_TIME:
            case DATE_TIME:
            case DATE:
            case ISO_STANDARD_DATE:
                // * case LOCALE_DATE:
                return true;
            default:
                return false;
            }
        }
    }

    private class FixedString implements FormatString {
        private final String s;

        FixedString(String s) {
            this.s = s;
        }

        @Override
        public int index() {
            return -2;
        }

        @Override
        public void print(Object arg)
                throws IOException {
            a.append(s);
        }

        @Override
        public String toString() {
            return s;
        }
    }

    private static class Flags {
        static final Flags ALTERNATE = new Flags(1 << 2); // '#'

        static final Flags GROUP = new Flags(1 << 6); // ','

        static final Flags LEADING_SPACE = new Flags(1 << 4); // ' '
        // duplicate declarations from Formattable.java
        static final Flags LEFT_JUSTIFY = new Flags(1 << 0); // '-'
        static final Flags NONE = new Flags(0); // ''

        static final Flags PARENTHESES = new Flags(1 << 7); // '('
        // numerics
        static final Flags PLUS = new Flags(1 << 3); // '+'
        // indexing
        static final Flags PREVIOUS = new Flags(1 << 8); // '<'
        static final Flags UPPERCASE = new Flags(1 << 1); // '^'
        static final Flags ZERO_PAD = new Flags(1 << 5); // '0'

        public static Flags parse(String s) {
            if (s == null || s.isEmpty()) {
                return Flags.NONE;
            }
            char[] ca = s.toCharArray();
            Flags f = new Flags(0);
            for (int i = 0; i < ca.length; i++) {
                Flags v = parse(ca[i]);
                if (f.contains(v)) {
                    throw new IllegalArgumentException(v.toString());
                }
                f.add(v);
            }
            return f;
        }

        // Returns a string representation of the current <tt>Flags</tt>.
        public static String toString(Flags f) {
            return f.toString();
        }

        // parse those flags which may be provided by users
        private static Flags parse(char c) {
            switch (c) {
            case '-':
                return LEFT_JUSTIFY;
            case '#':
                return ALTERNATE;
            case '+':
                return PLUS;
            case ' ':
                return LEADING_SPACE;
            case '0':
                return ZERO_PAD;
            case ',':
                return GROUP;
            case '(':
                return PARENTHESES;
            case '<':
                return PREVIOUS;
            default:
                throw new IllegalArgumentException(String.valueOf(c));
            }
        }

        private int flags;

        private Flags(int f) {
            flags = f;
        }

        public boolean contains(Flags f) {
            return (flags & f.valueOf()) == f.valueOf();
        }

        public Flags dup() {
            return new Flags(flags);
        }

        public Flags remove(Flags f) {
            flags &= ~f.valueOf();
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (contains(LEFT_JUSTIFY)) {
                sb.append('-');
            }
            if (contains(UPPERCASE)) {
                sb.append('^');
            }
            if (contains(ALTERNATE)) {
                sb.append('#');
            }
            if (contains(PLUS)) {
                sb.append('+');
            }
            if (contains(LEADING_SPACE)) {
                sb.append(' ');
            }
            if (contains(ZERO_PAD)) {
                sb.append('0');
            }
            if (contains(GROUP)) {
                sb.append(',');
            }
            if (contains(PARENTHESES)) {
                sb.append('(');
            }
            if (contains(PREVIOUS)) {
                sb.append('<');
            }
            return sb.toString();
        }

        public int valueOf() {
            return flags;
        }

        Flags add(Flags f) {
            flags |= f.valueOf();
            return this;
        }
    }

    private class FormatSpecifier implements FormatString {
        private class BigDecimalLayout {
            private boolean dot = false;
            private StringBuilder exp;
            private StringBuilder mant;
            private int scale;

            public BigDecimalLayout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                layout(intVal, scale, form);
            }

            // The exponent will be formatted as a sign ('+' or '-') followed
            // by the exponent zero-padded to include at least two digits.
            public char[] exponent() {
                return toCharArray(exp);
            }

            public boolean hasDot() {
                return dot;
            }

            public char[] mantissa() {
                return toCharArray(mant);
            }

            public int scale() {
                return scale;
            }

            private void layout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                char[] coeff = intVal.toString().toCharArray();
                this.scale = scale;

                // Construct a buffer, with sufficient capacity for all cases.
                // If E-notation is needed, length will be: +1 if negative, +1
                // if '.' needed, +2 for "E+", + up to 10 for adjusted
                // exponent. Otherwise it could have +1 if negative, plus
                // leading "0.00000"
                mant = new StringBuilder(coeff.length + 14);

                if (scale == 0) {
                    int len = coeff.length;
                    if (len > 1) {
                        mant.append(coeff[0]);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            mant.append('.');
                            dot = true;
                            mant.append(coeff, 1, len - 1);
                            exp = new StringBuilder("+");
                            if (len < 10) {
                                exp.append("0").append(len - 1);
                            } else {
                                exp.append(len - 1);
                            }
                        } else {
                            mant.append(coeff, 1, len - 1);
                        }
                    } else {
                        mant.append(coeff);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            exp = new StringBuilder("+00");
                        }
                    }
                    return;
                }
                long adjusted = -(long) scale + (coeff.length - 1);
                if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {
                    // count of padding zeros
                    int pad = scale - coeff.length;
                    if (pad >= 0) {
                        // 0.xxx form
                        mant.append("0.");
                        dot = true;
                        for (; pad > 0; pad--) {
                            mant.append('0');
                        }
                        mant.append(coeff);
                    } else {
                        if (-pad < coeff.length) {
                            // xx.xx form
                            mant.append(coeff, 0, -pad);
                            mant.append('.');
                            dot = true;
                            mant.append(coeff, -pad, scale);
                        } else {
                            // xx form
                            mant.append(coeff, 0, coeff.length);
                            for (int i = 0; i < -scale; i++) {
                                mant.append('0');
                            }
                            this.scale = 0;
                        }
                    }
                } else {
                    // x.xxx form
                    mant.append(coeff[0]);
                    if (coeff.length > 1) {
                        mant.append('.');
                        dot = true;
                        mant.append(coeff, 1, coeff.length - 1);
                    }
                    exp = new StringBuilder();
                    if (adjusted != 0) {
                        long abs = Math.abs(adjusted);
                        // require sign
                        exp.append(adjusted < 0 ? '-' : '+');
                        if (abs < 10) {
                            exp.append('0');
                        }
                        exp.append(abs);
                    } else {
                        exp.append("+00");
                    }
                }
            }

            private char[] toCharArray(StringBuilder sb) {
                if (sb == null) {
                    return null;
                }
                char[] result = new char[sb.length()];
                sb.getChars(0, result.length, result, 0);
                return result;
            }
        }

        private char c;
        private boolean dt = false;
        private Flags f = Flags.NONE;
        private int index = -1;
        // cache the line separator
        private final String ls = "\n";

        private int precision;

        private int width;

        FormatSpecifier(FormatterJava formatter, String[] sa) {
            int idx = 0;

            index(sa[idx++]);
            flags(sa[idx++]);
            width(sa[idx++]);
            precision(sa[idx++]);

            String dtStr = sa[idx];
            if (dtStr != null && !dtStr.isEmpty()) {
                dt = true;
                if (dtStr.equals("T")) {
                    f.add(Flags.UPPERCASE);
                }
            }
            conversion(sa[++idx]);

            if (dt) {
                checkDateTime();
            } else if (Conversion.isGeneral(c)) {
                checkGeneral();
            } else if (Conversion.isCharacter(c)) {
                checkCharacter();
            } else if (Conversion.isInteger(c)) {
                checkInteger();
            } else if (Conversion.isFloat(c)) {
                checkFloat();
            } else if (Conversion.isText(c)) {
                checkText();
            } else {
                throw new IllegalArgumentException(String.valueOf(c));
            }
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void print(Object arg) throws IOException {
            if (dt) {
                printDateTime(arg);
                return;
            }
            switch (c) {
            case Conversion.DECIMAL_INTEGER:
            case Conversion.OCTAL_INTEGER:
            case Conversion.HEXADECIMAL_INTEGER:
                printInteger(arg);
                break;
            case Conversion.SCIENTIFIC:
            case Conversion.GENERAL:
            case Conversion.DECIMAL_FLOAT:
            case Conversion.HEXADECIMAL_FLOAT:
                printFloat(arg);
                break;
            case Conversion.CHARACTER:
            case Conversion.CHARACTER_UPPER:
                printCharacter(arg);
                break;
            case Conversion.BOOLEAN:
                printBoolean(arg);
                break;
            case Conversion.STRING:
                printString(arg);
                break;
            case Conversion.HASHCODE:
                printHashCode(arg);
                break;
            case Conversion.LINE_SEPARATOR:
                a.append(ls);
                break;
            case Conversion.PERCENT_SIGN:
                a.append('%');
                break;
            default:
                assert false;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder('%');
            // Flags.UPPERCASE is set internally for legal conversions.
            Flags dupf = f.dup().remove(Flags.UPPERCASE);
            sb.append(dupf.toString());
            if (index > 0) {
                sb.append(index).append('$');
            }
            if (width != -1) {
                sb.append(width);
            }
            if (precision != -1) {
                sb.append('.').append(precision);
            }
            if (dt) {
                sb.append(f.contains(Flags.UPPERCASE) ? 'T' : 't');
            }
            sb.append(f.contains(Flags.UPPERCASE)
                    ? Character.toUpperCase(c) : c);
            return sb.toString();
        }

        // Add a '.' to th mantissa if required
        private char[] addDot(char[] mant) {
            char[] tmp = mant;
            tmp = new char[mant.length + 1];
            System.arraycopy(mant, 0, tmp, 0, mant.length);
            tmp[tmp.length - 1] = '.';
            return tmp;
        }

        private int adjustWidth(int width, Flags f, boolean neg) {
            int newW = width;
            if (newW != -1 && neg && f.contains(Flags.PARENTHESES)) {
                newW--;
            }
            return newW;
        }

        private void checkBadFlags(Flags... badFlags) {
            for (int i = 0; i < badFlags.length; i++) {
                if (f.contains(badFlags[i])) {
                    failMismatch(badFlags[i], c);
                }
            }
        }

        private void checkCharacter() {
            if (precision != -1) {
                throw new IllegalArgumentException("invalid pecision " + precision);
            }
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new IllegalArgumentException(toString());
            }
        }

        private void checkDateTime() {
            if (precision != -1) {
                throw new IllegalArgumentException("invalid pecision " + precision);
            }
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new IllegalArgumentException(toString());
            }
        }

        private void checkFloat() {
            checkNumeric();
            if (c == Conversion.HEXADECIMAL_FLOAT) {
                checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
            } else if (c == Conversion.SCIENTIFIC) {
                checkBadFlags(Flags.GROUP);
            } else if (c == Conversion.GENERAL) {
                checkBadFlags(Flags.ALTERNATE);
            }
        }

        private void checkGeneral() {
            if ((c == Conversion.BOOLEAN || c == Conversion.HASHCODE)
                    && f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, c);
            }
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new IllegalArgumentException(toString());
            }
            checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD,
                    Flags.GROUP, Flags.PARENTHESES);
        }

        private void checkInteger() {
            checkNumeric();
            if (precision != -1) {
                throw new IllegalArgumentException("invalid pecision " + precision);
            }

            if (c == Conversion.DECIMAL_INTEGER) {
                checkBadFlags(Flags.ALTERNATE);
            } else if (c == Conversion.OCTAL_INTEGER) {
                checkBadFlags(Flags.GROUP);
            } else {
                checkBadFlags(Flags.GROUP);
            }
        }

        private void checkNumeric() {
            if (width != -1 && width < 0) {
                throw new IllegalArgumentException("illegal width " + width);
            }

            if (precision != -1 && precision < 0) {
                throw new IllegalArgumentException("invalid precision " + precision);
            }

            // '-' and '0' require a width
            if (width == -1
                    && (f.contains(Flags.LEFT_JUSTIFY) || f.contains(Flags.ZERO_PAD))) {
                throw new IllegalArgumentException(toString());
            }

            // bad combination
            if ((f.contains(Flags.PLUS) && f.contains(Flags.LEADING_SPACE))
                    || (f.contains(Flags.LEFT_JUSTIFY) && f.contains(Flags.ZERO_PAD))) {
                throw new IllegalArgumentException(f.toString());
            }
        }

        private void checkText() {
            if (precision != -1) {
                throw new IllegalArgumentException("invalid precision " + precision);
            }
            switch (c) {
            case Conversion.PERCENT_SIGN:
                if (f.valueOf() != Flags.LEFT_JUSTIFY.valueOf()
                        && f.valueOf() != Flags.NONE.valueOf()) {
                    throw new IllegalArgumentException(f.toString());
                }
                // '-' requires a width
                if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                    throw new IllegalArgumentException(toString());
                }
                break;
            case Conversion.LINE_SEPARATOR:
                if (width != -1) {
                    throw new IllegalArgumentException(" illegal width: " + width);
                }
                if (f.valueOf() != Flags.NONE.valueOf()) {
                    throw new IllegalArgumentException(f.toString());
                }
                break;
            default:
                assert false;
            }
        }

        private char conversion(String s) {
            c = s.charAt(0);
            if (!dt) {
                if (!Conversion.isValid(c)) {
                    throw new IllegalArgumentException(String.valueOf(c));
                }
                if (Character.isUpperCase(c)) {
                    f.add(Flags.UPPERCASE);
                }
                c = Character.toLowerCase(c);
                if (Conversion.isText(c)) {
                    index = -2;
                }
            }
            return c;
        }

        private void failConversion(char c, Object arg) {
            throw new IllegalArgumentException("Conversion failed for '" + c + "' with class "
                    + arg.getClass().getName());
        }

        private void failMismatch(Flags f, char c) {
            String fs = f.toString();
            throw new IllegalArgumentException("Match failed" + fs + " for char: " + c);
        }

        private Flags flags(String s) {
            f = Flags.parse(s);
            if (f.contains(Flags.PREVIOUS)) {
                index = -1;
            }
            return f;
        }

        private char getZero() {
            return zero;
        }

        private int index(String s) {
            if (s != null && !s.isEmpty()) {
                try {
                    index = Integer.parseInt(s.substring(0, s.length() - 1));
                } catch (NumberFormatException x) {
                    assert false;
                }
            } else {
                index = 0;
            }
            return index;
        }

        private String justify(String s) {
            if (width == -1) {
                return s;
            }
            StringBuilder sb = new StringBuilder();
            boolean pad = f.contains(Flags.LEFT_JUSTIFY);
            int sp = width - s.length();
            if (!pad) {
                for (int i = 0; i < sp; i++) {
                    sb.append(' ');
                }
            }
            sb.append(s);
            if (pad) {
                for (int i = 0; i < sp; i++) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }

        // neg := val < 0
        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
            if (!neg) {
                if (f.contains(Flags.PLUS)) {
                    sb.append('+');
                } else if (f.contains(Flags.LEADING_SPACE)) {
                    sb.append(' ');
                }
            } else {
                if (f.contains(Flags.PARENTHESES)) {
                    sb.append('(');
                } else {
                    sb.append('-');
                }
            }
            return sb;
        }

        private StringBuilder
                localizedMagnitude(StringBuilder sb, char[] value, Flags f,
                        int width) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            int begin = sb.length();

            char zero = getZero();

            // determine localized grouping separator and size
            char grpSep = '\0';
            int grpSize = -1;
            char decSep = '\0';

            int len = value.length;
            int dot = len;
            for (int j = 0; j < len; j++) {
                if (value[j] == '.') {
                    dot = j;
                    break;
                }
            }

            if (dot < len) {
                decSep = '.';
            }

            if (f.contains(Flags.GROUP)) {
                grpSep = ',';
                grpSize = 3;
            }

            // localize the digits inserting group separators as necessary
            for (int j = 0; j < len; j++) {
                if (j == dot) {
                    sb.append(decSep);
                    // no more group separators after the decimal separator
                    grpSep = '\0';
                    continue;
                }

                char c = value[j];
                sb.append((char) ((c - '0') + zero));
                if (grpSep != '\0' && j != dot - 1 && ((dot - j) % grpSize == 1)) {
                    sb.append(grpSep);
                }
            }

            // apply zero padding
            len = sb.length();
            if (width != -1 && f.contains(Flags.ZERO_PAD)) {
                for (int k = 0; k < width - len; k++) {
                    sb.insert(begin, zero);
                }
            }

            return sb;
        }

        private StringBuilder
                localizedMagnitude(StringBuilder sb, long value, Flags f,
                        int width) {
            char[] va = Long.toString(value, 10).toCharArray();
            return localizedMagnitude(sb, va, f, width);
        }

        private int precision(String s) {
            precision = -1;
            if (s != null && !s.isEmpty()) {
                try {
                    // remove the '.'
                    precision = Integer.parseInt(s.substring(1));
                    if (precision < 0) {
                        throw new IllegalArgumentException("precision: " + precision);
                    }
                } catch (NumberFormatException x) {
                    assert false;
                }
            }
            return precision;
        }

        private void print(BigDecimal value) throws IOException {
            if (c == Conversion.HEXADECIMAL_FLOAT) {
                failConversion(c, value);
            }
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigDecimal v = value.abs();
            // leading sign indicator
            leadingSign(sb, neg);

            // the value
            print(sb, v, f, c, precision, neg);

            // trailing sign indicator
            trailingSign(sb, neg);

            // justify based on width
            a.append(justify(sb.toString()));
        }

        private void print(BigInteger value) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigInteger v = value.abs();

            // leading sign indicator
            leadingSign(sb, neg);

            // the value
            if (c == Conversion.DECIMAL_INTEGER) {
                char[] va = v.toString().toCharArray();
                localizedMagnitude(sb, va, f, adjustWidth(width, f, neg));
            } else if (c == Conversion.OCTAL_INTEGER) {
                String s = v.toString(8);

                int len = s.length() + sb.length();
                if (neg && f.contains(Flags.PARENTHESES)) {
                    len++;
                }

                // apply ALTERNATE (radix indicator for octal) before ZERO_PAD
                if (f.contains(Flags.ALTERNATE)) {
                    len++;
                    sb.append('0');
                }
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < width - len; i++) {
                        sb.append('0');
                    }
                }
                sb.append(s);
            } else if (c == Conversion.HEXADECIMAL_INTEGER) {
                String s = v.toString(16);

                int len = s.length() + sb.length();
                if (neg && f.contains(Flags.PARENTHESES)) {
                    len++;
                }

                // apply ALTERNATE (radix indicator for hex) before ZERO_PAD
                if (f.contains(Flags.ALTERNATE)) {
                    len += 2;
                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < width - len; i++) {
                        sb.append('0');
                    }
                }
                if (f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase();
                }
                sb.append(s);
            }

            // trailing sign indicator
            trailingSign(sb, value.signum() == -1);

            // justify based on width
            a.append(justify(sb.toString()));
        }

        private void print(byte value) throws IOException {
            long v = value;
            if (value < 0
                    && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += 1L << 8;
                assert v >= 0 : v;
            }
            print(v);
        }

        private void print(Date t, char c) throws IOException {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c);

            // justify based on width
            String s = justify(sb.toString());
            if (f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase();
            }

            a.append(s);
        }

        private void print(double value) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = Double.compare(value, 0.0) == -1;

            if (!Double.isNaN(value)) {
                double v = Math.abs(value);

                // leading sign indicator
                leadingSign(sb, neg);

                // the value
                if (!Double.isInfinite(v)) {
                    print(sb, v, f, c, precision, neg);
                } else {
                    sb.append(f.contains(Flags.UPPERCASE)
                            ? "INFINITY" : "Infinity");
                }

                // trailing sign indicator
                trailingSign(sb, neg);
            } else {
                sb.append(f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
            }

            // justify based on width
            a.append(justify(sb.toString()));
        }

        private void print(float value) throws IOException {
            print((double) value);
        }

        private void print(int value) throws IOException {
            long v = value;
            if (value < 0
                    && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += 1L << 32;
                assert v >= 0 : v;
            }
            print(v);
        }

        private void print(long value) throws IOException {

            StringBuilder sb = new StringBuilder();

            if (c == Conversion.DECIMAL_INTEGER) {
                boolean neg = value < 0;
                char[] va;
                if (value < 0) {
                    va = Long.toString(value, 10).substring(1).toCharArray();
                } else {
                    va = Long.toString(value, 10).toCharArray();
                }

                // leading sign indicator
                leadingSign(sb, neg);

                // the value
                localizedMagnitude(sb, va, f, adjustWidth(width, f, neg));

                // trailing sign indicator
                trailingSign(sb, neg);
            } else if (c == Conversion.OCTAL_INTEGER) {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,
                        Flags.PLUS);
                String s = Long.toOctalString(value);
                int len = f.contains(Flags.ALTERNATE)
                        ? s.length() + 1
                        : s.length();

                // apply ALTERNATE (radix indicator for octal) before ZERO_PAD
                if (f.contains(Flags.ALTERNATE)) {
                    sb.append('0');
                }
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < width - len; i++) {
                        sb.append('0');
                    }
                }
                sb.append(s);
            } else if (c == Conversion.HEXADECIMAL_INTEGER) {
                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,
                        Flags.PLUS);
                String s = Long.toHexString(value);
                int len = f.contains(Flags.ALTERNATE)
                        ? s.length() + 2
                        : s.length();

                // apply ALTERNATE (radix indicator for hex) before ZERO_PAD
                if (f.contains(Flags.ALTERNATE)) {
                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < width - len; i++) {
                        sb.append('0');
                    }
                }
                if (f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase();
                }
                sb.append(s);
            }

            // justify based on width
            a.append(justify(sb.toString()));
        }

        private void print(short value) throws IOException {
            long v = value;
            if (value < 0
                    && (c == Conversion.OCTAL_INTEGER
                    || c == Conversion.HEXADECIMAL_INTEGER)) {
                v += 1L << 16;
                assert v >= 0 : v;
            }
            print(v);
        }

        private void print(String s) throws IOException {
            if (precision != -1 && precision < s.length()) {
                s = s.substring(0, precision);
            }
            if (f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase();
            }
            a.append(justify(s));
        }

        // value > 0
        private void print(StringBuilder sb, BigDecimal value,
                Flags f, char c, int precision, boolean neg)
                throws IOException {
            if (c == Conversion.SCIENTIFIC) {
                // Create a new BigDecimal with the desired precision.
                int prec = precision == -1 ? 6 : precision;
                int scale = value.scale();
                int origPrec = value.precision();
                int nzeros = 0;
                int compPrec;

                if (prec > origPrec - 1) {
                    compPrec = origPrec;
                    nzeros = prec - (origPrec - 1);
                } else {
                    compPrec = prec + 1;
                }

                MathContext mc = new MathContext(compPrec);
                BigDecimal v = new BigDecimal(value.unscaledValue(), scale, mc);

                BigDecimalLayout bdl = new BigDecimalLayout(v.unscaledValue(), v.scale(),
                        BigDecimalLayoutForm.SCIENTIFIC);

                char[] mant = bdl.mantissa();

                // Add a decimal point if necessary. The mantissa may not
                // contain a decimal point if the scale is zero (the internal
                // representation has no fractional part) or the original
                // precision is one. Append a decimal point if '#' is set or if
                // we require zero padding to get to the requested precision.
                if ((origPrec == 1 || !bdl.hasDot())
                        && (nzeros > 0 || f.contains(Flags.ALTERNATE))) {
                    mant = addDot(mant);
                }

                // Add trailing zeros in the case precision is greater than
                // the number of available digits after the decimal separator.
                mant = trailingZeros(mant, nzeros);

                char[] exp = bdl.exponent();
                int newW = width;
                if (width != -1) {
                    newW = adjustWidth(width - exp.length - 1, f, neg);
                }
                localizedMagnitude(sb, mant, f, newW);

                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');

                Flags flags = f.dup().remove(Flags.GROUP);
                char sign = exp[0];
                assert sign == '+' || sign == '-';
                sb.append(exp[0]);

                char[] tmp = new char[exp.length - 1];
                System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                sb.append(localizedMagnitude(null, tmp, flags, -1));
            } else if (c == Conversion.DECIMAL_FLOAT) {
                // Create a new BigDecimal with the desired precision.
                int prec = precision == -1 ? 6 : precision;
                int scale = value.scale();
                if (scale > prec) {
                    // more "scale" digits than the requested "precision
                    int compPrec = value.precision();
                    if (compPrec <= scale) {
                        // case of 0.xxxxxx
                        value = value.setScale(prec, RoundingMode.HALF_UP);
                    } else {
                        compPrec -= scale - prec;
                        value = new BigDecimal(value.unscaledValue(),
                                scale,
                                new MathContext(compPrec));
                    }
                }
                BigDecimalLayout bdl = new BigDecimalLayout(
                        value.unscaledValue(),
                        value.scale(),
                        BigDecimalLayoutForm.DECIMAL_FLOAT);
                char[] mant = bdl.mantissa();
                int nzeros = bdl.scale() < prec ? prec - bdl.scale() : 0;

                // Add a decimal point if necessary. The mantissa may not
                // contain a decimal point if the scale is zero (the internal
                // representation has no fractional part). Append a decimal
                // point if '#' is set or we require zero padding to get to the
                // requested precision.
                if (bdl.scale() == 0 && (f.contains(Flags.ALTERNATE) || nzeros > 0)) {
                    mant = addDot(bdl.mantissa());
                }

                // Add trailing zeros if the precision is greater than the
                // number of available digits after the decimal separator.
                mant = trailingZeros(mant, nzeros);

                localizedMagnitude(sb, mant, f, adjustWidth(width, f, neg));
            } else if (c == Conversion.GENERAL) {
                int prec = precision;
                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }

                BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
                BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec);
                if (value.equals(BigDecimal.ZERO)
                        || ((value.compareTo(tenToTheNegFour) != -1)
                        && (value.compareTo(tenToThePrec) == -1))) {

                    int e = -value.scale()
                            + (value.unscaledValue().toString().length() - 1);

                    // xxx.yyy
                    // g precision (# sig digits) = #x + #y
                    // f precision = #y
                    // exponent = #x - 1
                    // => f precision = g precision - exponent - 1
                    // 0.000zzz
                    // g precision (# sig digits) = #z
                    // f precision = #0 (after '.') + #z
                    // exponent = - #0 (after '.') - 1
                    // => f precision = g precision - exponent - 1
                    prec = prec - e - 1;

                    print(sb, value, f, Conversion.DECIMAL_FLOAT, prec,
                            neg);
                } else {
                    print(sb, value, f, Conversion.SCIENTIFIC, prec - 1, neg);
                }
            } else if (c == Conversion.HEXADECIMAL_FLOAT) {
                // This conversion isn't supported. The error should be
                // reported earlier.
                assert false;
            }
        }

        private Appendable print(StringBuilder sb, Date t, char c)
                throws IOException {
            assert width == -1;
            if (sb == null) {
                sb = new StringBuilder();
            }
            int i;
            Flags flags;
            char sep;
            switch (c) {
            case DateTime.HOUR_OF_DAY_0: // 'H' (00 - 23)
            case DateTime.HOUR_0: // 'I' (01 - 12)
            case DateTime.HOUR_OF_DAY: // 'k' (0 - 23) -- like H
            case DateTime.HOUR: // 'l' (1 - 12) -- like I
                i = t.getHours();
                if (c == DateTime.HOUR_0 || c == DateTime.HOUR) {
                    i = i == 0 || i == 12 ? 12 : i % 12;
                }
                flags = c == DateTime.HOUR_OF_DAY_0
                        || c == DateTime.HOUR_0
                        ? Flags.ZERO_PAD
                        : Flags.NONE;
                sb.append(localizedMagnitude(null, i, flags, 2));
                break;
            case DateTime.MINUTE: // 'M' (00 - 59)
                i = t.getMinutes();
                flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2));
                break;
            case DateTime.SECONDS_SINCE_EPOCH: // 's' (0 - 99...?)
                flags = Flags.NONE;
                sb.append(localizedMagnitude(null, t.getTime() / 1000, flags, width));
                break;
            case DateTime.SECOND: // 'S' (00 - 60 - leap second)
                i = t.getSeconds();
                flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2));
                break;
            case DateTime.ZONE_NUMERIC: // 'z' ({-|+}####) - ls minus?
                i = t.getTimezoneOffset();
                boolean neg = i < 0;
                sb.append(neg ? '-' : '+');
                if (neg) {
                    i = -i;
                }
                int min = i / 60000;
                // combine minute and hour into a single integer
                int offset = (min / 60) * 100 + (min % 60);
                flags = Flags.ZERO_PAD;

                sb.append(localizedMagnitude(null, offset, flags, 4));
                break;
            case DateTime.CENTURY: // 'C' (00 - 99)
            case DateTime.YEAR_2: // 'y' (00 - 99)
            case DateTime.YEAR_4: // 'Y' (0000 - 9999)
                i = t.getYear();
                int size = 2;
                switch (c) {
                case DateTime.CENTURY:
                    i /= 100;
                    break;
                case DateTime.YEAR_2:
                    i %= 100;
                    break;
                case DateTime.YEAR_4:
                    size = 4;
                    break;
                }
                flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, size));
                break;
            case DateTime.DAY_OF_MONTH_0: // 'd' (01 - 31)
            case DateTime.DAY_OF_MONTH: // 'e' (1 - 31) -- like d
                i = t.getDate();
                flags = c == DateTime.DAY_OF_MONTH_0
                        ? Flags.ZERO_PAD
                        : Flags.NONE;
                sb.append(localizedMagnitude(null, i, flags, 2));
                break;
            case DateTime.MONTH: // 'm' (01 - 12)
                i = t.getMonth();
                flags = Flags.ZERO_PAD;
                sb.append(localizedMagnitude(null, i, flags, 2));
                break;
            // Composites
            case DateTime.TIME: // 'T' (24 hour hh:mm:ss - %tH:%tM:%tS)
            case DateTime.TIME_24_HOUR: // 'R' (hh:mm same as %H:%M)
                sep = ':';
                print(sb, t, DateTime.HOUR_OF_DAY_0).append(sep);
                print(sb, t, DateTime.MINUTE);
                if (c == DateTime.TIME) {
                    sb.append(sep);
                    print(sb, t, DateTime.SECOND);
                }
                break;
            case DateTime.TIME_12_HOUR: // 'r' (hh:mm:ss [AP]M)
                sep = ':';
                print(sb, t, DateTime.HOUR_0).append(sep);
                print(sb, t, DateTime.MINUTE).append(sep);
                print(sb, t, DateTime.SECOND).append(' ');
                // this may be in wrong place for some locales
                StringBuilder tsb = new StringBuilder();
                print(tsb, t, DateTime.AM_PM);
                sb.append(tsb.toString().toUpperCase());
                break;
            case DateTime.DATE_TIME: // 'c' (Sat Nov 04 12:02:33 EST 1999)
                sep = ' ';
                print(sb, t, DateTime.NAME_OF_DAY_ABBREV).append(sep);
                print(sb, t, DateTime.NAME_OF_MONTH_ABBREV).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0).append(sep);
                print(sb, t, DateTime.TIME).append(sep);
                print(sb, t, DateTime.ZONE).append(sep);
                print(sb, t, DateTime.YEAR_4);
                break;
            case DateTime.DATE: // 'D' (mm/dd/yy)
                sep = '/';
                print(sb, t, DateTime.MONTH).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0).append(sep);
                print(sb, t, DateTime.YEAR_2);
                break;
            case DateTime.ISO_STANDARD_DATE: // 'F' (%Y-%m-%d)
                sep = '-';
                print(sb, t, DateTime.YEAR_4).append(sep);
                print(sb, t, DateTime.MONTH).append(sep);
                print(sb, t, DateTime.DAY_OF_MONTH_0);
                break;
            default:
                throw new IllegalArgumentException("Format flag: '" + c + "' is not supported");
            }
            return sb;
        }

        // !Double.isInfinite(value) && !Double.isNaN(value)
        private void print(StringBuilder sb, double value,
                Flags f, char c, int precision, boolean neg)
                throws IOException {
            sb.append(value);
        }

        private void printBoolean(Object arg) throws IOException {
            String s;
            if (arg != null) {
                s = arg instanceof Boolean
                        ? arg.toString()
                        : Boolean.toString(true);
            } else {
                s = Boolean.toString(false);
            }
            print(s);
        }

        private void printCharacter(Object arg) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            String s = null;
            if (arg instanceof Character) {
                s = arg.toString();
            } else if (arg instanceof Byte) {
                byte i = ((Byte) arg).byteValue();
                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalArgumentException("invalid code  point " + i);
                }
            } else if (arg instanceof Short) {
                short i = ((Short) arg).shortValue();
                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalArgumentException("invalid code  point " + i);
                }
            } else if (arg instanceof Integer) {
                int i = ((Integer) arg).intValue();
                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalArgumentException("invalid code  point " + i);
                }
            } else {
                failConversion(c, arg);
            }
            print(s);
        }

        private void printDateTime(Object arg) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }

            Date date = null;
            // Instead of Calendar.setLenient(true), perhaps we should
            // wrap the IllegalArgumentException that might be thrown?
            if (arg instanceof Long) {
                // Note that the following method uses an instance of the
                // default time zone (TimeZone.getDefaultRef().
                date = new Date((Long) arg);
            } else if (arg instanceof Date) {
                // Note that the following method uses an instance of the
                // default time zone (TimeZone.getDefaultRef().
                date = (Date) arg;
            } else {
                failConversion(c, arg);
            }
            print(date, c);
        }

        private void printFloat(Object arg) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Float) {
                print(((Float) arg).floatValue());
            } else if (arg instanceof Double) {
                print(((Double) arg).doubleValue());
            } else if (arg instanceof BigDecimal) {
                print((BigDecimal) arg);
            } else {
                failConversion(c, arg);
            }
        }

        private void printHashCode(Object arg) throws IOException {
            String s = arg == null
                    ? "null"
                    : Integer.toHexString(arg.hashCode());
            print(s);
        }

        // -- Methods to support throwing exceptions --

        private void printInteger(Object arg) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Byte) {
                print(((Byte) arg).byteValue());
            } else if (arg instanceof Short) {
                print(((Short) arg).shortValue());
            } else if (arg instanceof Integer) {
                print(((Integer) arg).intValue());
            } else if (arg instanceof Long) {
                print(((Long) arg).longValue());
            } else if (arg instanceof BigInteger) {
                print((BigInteger) arg);
            } else {
                failConversion(c, arg);
            }
        }

        private void printString(Object arg) throws IOException {
            if (arg == null) {
                print("null");
            } else {
                print(arg.toString());
            }
        }

        // neg := val < 0
        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
            if (neg && f.contains(Flags.PARENTHESES)) {
                sb.append(')');
            }
            return sb;
        }

        // Add trailing zeros in the case precision is greater than the number
        // of available digits after the decimal separator.
        private char[] trailingZeros(char[] mant, int nzeros) {
            char[] tmp = mant;
            if (nzeros > 0) {
                tmp = new char[mant.length + nzeros];
                System.arraycopy(mant, 0, tmp, 0, mant.length);
                for (int i = mant.length; i < tmp.length; i++) {
                    tmp[i] = '0';
                }
            }
            return tmp;
        }

        private int width(String s) {
            width = -1;
            if (s != null && !s.isEmpty()) {
                try {
                    width = Integer.parseInt(s);
                    if (width < 0) {
                        throw new IllegalArgumentException("Illegal width " + width);
                    }
                } catch (NumberFormatException x) {
                    assert false;
                }
            }
            return width;
        }
    }

    private interface FormatString {
        int index();

        void print(Object arg) throws IOException;

        //@Override
        String toString();
    }

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

    private static RegExp fsPattern = RegExp.compile(formatSpecifier);

    private Appendable a;

    private IOException lastException;

    private final char zero = '0';

    public FormatterJava() {
        init(new StringBuilder());
    }

    public FormatterJava(Appendable a) {
        if (a == null) {
            a = new StringBuilder();
        }
        init(a);
    }

    public FormatterJava format(String format, Object... args) {
        // index of last argument referenced
        int last = -1;
        // last ordinary index
        int lasto = -1;

        FormatString[] fsa = parse(format);
        for (int i = 0; i < fsa.length; i++) {
            FormatString fs = fsa[i];
            int index = fs.index();
            try {
                switch (index) {
                case -2: // fixed string, "%n", or "%%"
                    fs.print(null);
                    break;
                case -1: // relative index
                    if (last < 0 || (args != null && last > args.length - 1)) {
                        throw new IllegalArgumentException(fs.toString());
                    }
                    fs.print(args == null ? null : args[last]);
                    break;
                case 0: // ordinary index
                    lasto++;
                    last = lasto;
                    if (args != null && lasto > args.length - 1) {
                        throw new IllegalArgumentException(fs.toString());
                    }
                    fs.print(args == null ? null : args[lasto]);
                    break;
                default: // explicit index
                    last = index - 1;
                    if (args != null && last > args.length - 1) {
                        throw new IllegalArgumentException(fs.toString());
                    }
                    fs.print(args == null ? null : args[last]);
                    break;
                }
            } catch (IOException x) {
                lastException = x;
            }
        }
        return this;
    }

    public IOException ioException() {
        return lastException;
    }

    public Appendable out() {
        return a;
    };

    @Override
    public String toString() {
        return a.toString();
    }

    private void checkText(String s) {
        int idx;
        // If there are any '%' in the given string, we got a bad format
        // specifier.
        if ((idx = s.indexOf('%')) != -1) {
            char c = idx > s.length() - 2 ? '%' : s.charAt(idx + 1);
            throw new IllegalArgumentException(String.valueOf(c));
        }
    }

    // Initialize internal data.
    private void init(Appendable a) {
        this.a = a;
    }

    // Look for format specifiers in the format string.
    private FormatString[] parse(String s) {
        ArrayList<FormatString> al = new ArrayList<>();
        while (s.length() > 0) {
            MatchResult m = fsPattern.exec(s);
            if (m != null) {
                int i = m.getIndex();
                if (i > 0) {
                    // Anything between the start of the string and the beginning
                    // of the format specifier is either fixed text or contains
                    // an invalid format string.
                    String staticText = s.substring(0, i);

                    // Make sure we didn't miss any invalid format specifiers
                    checkText(staticText);

                    // Assume previous characters were fixed text
                    al.add(new FixedString(staticText));
                }
                // Expect 6 groups in regular expression
                String[] sa = new String[6];
                for (int j = 1; j < m.getGroupCount(); j++) {
                    sa[j - 1] = m.getGroup(j);
                    // System.out.print(sa[j] + " ");
                }
                // System.out.println();
                al.add(new FormatSpecifier(this, sa));

                // trim parsed string
                s = s.substring(i + m.getGroup(0).length(), s.length());
            } else {
                // No more valid format specifiers. Check for possible invalid
                // format specifiers.
                checkText(s);
                // The rest of the string is fixed text
                al.add(new FixedString(s));
                break;
            }
        }
        // FormatString[] fs = new FormatString[al.size()];
        // for (int j = 0; j < al.size(); j++)
        // System.out.println(((FormatString) al.get(j)).toString());
        return al.toArray(new FormatString[0]);
    }
}
