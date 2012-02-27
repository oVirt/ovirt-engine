package org.ovirt.engine.core.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * String helper utilities
 *
 *
 */
public class StringUtils {
    public static final String UTF8_CHARSET_ENCODING = "UTF8";
    private static final String DELIMITER = ",";
    private static final String SEP = "=";

    /**
     * Converts the sequence of bytes to string according to UTF-8 charset encoding.
     *
     * @param byteArrToEncode
     *            array of bytes that holds the data that will be used to create the string
     * @return the encoded string
     */
    public static String charsetEncodeBytesUTF8(byte[] byteArrToEncode) {

        return charsetEncodeBytes(byteArrToEncode, UTF8_CHARSET_ENCODING);
    }

    /**
     * Converts the sequence of bytes to string according to the given charset encoding.
     *
     * @param byteArrToEncode
     *            the array of bytes that holds the data that will be used to create the string
     * @param charsetEncoding
     *            the charset encodign to be used
     * @return the encoded string
     */
    public static String charsetEncodeBytes(byte[] byteArrToEncode, String charsetEncoding) {
        try {
            return new String(byteArrToEncode, charsetEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported charset", e);
        }

    }

    /**
     * Converts a string to an array of byte according to a given charset encoding
     *
     * @param strToDecode
     *            the string to decode
     * @param charsetEncoding
     *            the charset encoding to use
     * @return the decoded array of bytes
     */
    public static byte[] charsetDecodeString(String strToDecode, String charsetEncoding) {
        try {
            return strToDecode.getBytes(charsetEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported charset", e);
        }

    }

    /**
     * Converts the given string to an array of byte based on UTF8 encoding
     *
     * @param strToDecode
     *            the string to convert
     * @return the array of bytes returned
     */
    public static byte[] charsetDecodeStringUTF8(String strToDecode) {
        return charsetDecodeString(strToDecode, UTF8_CHARSET_ENCODING);

    }

    /**
     * Concatenates a sequence of string using StringBuilder should be used instead of s1+s2+s3+s4+s5..... +s-n
     *
     * @param strings
     *            varying number of string to be concatenated
     * @return
     */
    public static String concat(String... strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
        }

        return sb.toString();
    }
    /**
     * Converting a comma delimited key=value format string to a key,value map
     * values should not contain the equal sign (=)
     * @param str
     * @return a Map of the key/value pairs
     */
    public static Map<String, String> string2Map(String str) {

        Map<String, String> map = new HashMap<String, String>();
        if (str != null) {
            // remove map markers
            str = str.replaceAll("[{}]", "");
            if (str.length() > 0) {
                String[] keyValPairs = str.split(DELIMITER);
                for (String pair : keyValPairs) {
                    String[] keyval = pair.split(SEP);
                    if (keyval.length == 2)
                        map.put(keyval[0], keyval[1]);
                    else if (keyval.length == 1)
                        map.put(keyval[0], "");
                }
            }
        }
        return map;
    }

    /**
     * Converts a map to string formatted as key1=value1,key2=value2,...keyN=valueN
     *
     * @param map
     *            a map contains the values
     * @return a converted string contains map values
     */
    public static String map2String(Map<String, String> map){
        StringBuilder sb = new StringBuilder();

        Set<Entry<String, String>> entrySet = map.entrySet();
        if (map != null) {
            for (Entry<String, String> entry : entrySet) {
                if (sb.length() > 0) {
                    sb.append(DELIMITER);
                }
                sb.append(entry.getKey());
                sb.append(SEP);
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }
}
