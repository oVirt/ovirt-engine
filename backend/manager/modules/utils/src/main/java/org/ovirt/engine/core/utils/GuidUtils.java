package org.ovirt.engine.core.utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Guid;

public class GuidUtils {

    public static byte[] ToByteArray(UUID uuid) {
        String guidStr = uuid.toString();

        // Going to split the GUID to hexadecimal sequences.
        // Each sequence like that contains hexadecimal numbers that should be
        // converted to bytes.
        // As GUID may vary in size , the bytes are written to a byte array
        // output stream
        // which is kept in
        String[] guidParts = guidStr.split("-");
        if (guidParts == null || guidParts.length == 0) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (String guidPart : guidParts) {
            writeGUIDPartToStream(guidPart, baos);

        }

        return baos.toByteArray();
    }

    private static void writeGUIDPartToStream(String guidPart, ByteArrayOutputStream baos) {
        // GuidPart is composed from an even number of characters.
        // Each two characters are a hexadecimal number

        char[] dst = new char[guidPart.length()];
        guidPart.getChars(0, guidPart.length(), dst, 0);
        for (int counter = 0; counter < (guidPart.length()) / 2; counter++) {
            // Build a string from two characters
            StringBuilder numberStrSB = new StringBuilder();
            numberStrSB.append(dst[counter * 2]);
            numberStrSB.append(dst[(counter * 2) + 1]);
            // Convert the string to byte and add write it to the stream
            int number = Integer.parseInt(numberStrSB.toString(), 16);
            baos.write(number);
        }
    }

    private static final String SEPARATOR = ",";

    /**
     * Gets a string containing multiple <code>Guid</code> values separated by a comma and returns an ArrayList of
     * <code>Guid</code>. If the String is null/empty returns an empty array.
     *
     * @param str
     *            - String which contains list of <code>Guid</code>.
     * @return - Array of <code>Guid</code> type.
     */
    public static ArrayList<Guid> getGuidListFromString(String str) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<Guid>();
        }
        return getGuidListFromStringArray(Arrays.asList(str.split(SEPARATOR)));
    }

    /**
     * Gets a List of strings containing multiple <code>Guid</code> values and returns an ArrayList of <code>Guid</code>
     * . If the list is null/empty returns an empty ArrayList.
     *
     * @param strings
     *            - Array of Strings which contains <code>Guid</code> values.
     * @return - Array of <code>Guid</code> type.
     */
    public static ArrayList<Guid> getGuidListFromStringArray(List<String> strings) {
        ArrayList<Guid> guidList = new ArrayList<Guid>();
        if (strings != null && !strings.isEmpty()) {
            for (String guidString : strings) {
                guidList.add(Guid.createGuidFromStringDefaultEmpty(guidString));
            }
        }
        return guidList;
    }
}
