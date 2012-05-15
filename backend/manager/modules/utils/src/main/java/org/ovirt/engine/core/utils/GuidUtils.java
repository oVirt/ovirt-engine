package org.ovirt.engine.core.utils;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

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
        // TODO Auto-generated method stub

    }

    public static Guid getGuidValue(NGuid id) {
        return id == null ? null : id.getValue();
    }
}
