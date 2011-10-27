package org.ovirt.engine.core.utils;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.ovirt.engine.core.compat.*;

public class GuidUtils {
    private static org.ovirt.engine.core.compat.Regex isGuidExp =
            new org.ovirt.engine.core.compat.Regex(
                    "^(\\{){0,1}[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}(\\}){0,1}$",
                    RegexOptions.Compiled);

    public static boolean isGuid(String candidate, RefObject<org.ovirt.engine.core.compat.Guid> output) {
        boolean isValid = false;
        output.argvalue = org.ovirt.engine.core.compat.Guid.Empty;
        if (candidate != null) {
            if (isGuidExp.IsMatch(candidate)) {
                output.argvalue = new org.ovirt.engine.core.compat.Guid(candidate);
                isValid = true;
            }
        }
        return isValid;
    }

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
}
