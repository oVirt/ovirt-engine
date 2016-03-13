package org.ovirt.engine.core.common.utils;

import java.util.List;

import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

public class RpmVersionUtils {

    private static final String INVALID_RPM_NAME_FORMAT = "RPM name should be in format of Prefix-Version-Release";

    /**
     * Compares two Rpm version parts. The format of Rpm is - Prefix-Version-Release. Version part is either Version or
     * Release.
     *
     * @param part1
     *            part from first RPM
     * @param part2
     *            part from second RPM
     * @return
     *          0 - if two parts are equals
     *          1 - if the 1st argument is bigger than the 2nd
     *         -1 - if the 1st argument is smaller than the 2nd
     */
    public static int compareRpmParts(String part1, String part2) {
        // The method takes the two strings, and for each one of them -
        // Calculates segments of numeric or letters (for each segment - will try
        // to get its maximum possible length)
        // Then it will take two segments from both strings that are located in the same
        // position and compare them -
        // if both segments are equal - continue to next segment
        // if both segments are numeric - it will use Long comparison
        // if both segments are letters - it will use String comparison
        // in other cases - the string that has the numeric component wins
        StringBuilder[] comps1 = fillCompsArray(part1);
        StringBuilder[] comps2 = fillCompsArray(part2);
        int counter = 0;
        while (comps1[counter] != null && comps2[counter] != null
                && comps1[counter].toString().equals(comps2[counter].toString())) {
            counter++;
        }
        if (comps1[counter] == null && comps2[counter] == null) {
            // This means that the number of segments is equal, and that all segments in relative places are equal
            return 0;
        }
        // part1 has more parts
        if (comps1[counter] != null && comps2[counter] == null) {
            return 1;
        }
        // part2 has more parts
        if (comps2[counter] != null && comps1[counter] == null) {
            return -1;
        }
        Long longVal1 = parseLong(comps1[counter].toString());
        Long longVal2 = parseLong(comps2[counter].toString());

        // Both parts are not numeric, do lexicographical comparison
        if (longVal1 == null && longVal2 == null) {
            return comps1[counter].toString().compareTo(comps2[counter].toString());
        }
        // Both parts are numeric, do numeric comparison
        if (longVal1 != null && longVal2 != null) {
            return longVal1.compareTo(longVal2);
        }
        // 2nd part is numeric - it should "win"
        if (longVal1 == null) {
            return -1;
        }

        // 1st part is numeric - it should "win"
        return 1;
    }

    protected static Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * This method splits Rpm version (or release component) into segments. Each segment should contain only letters or
     * numbers. Characters which are not alpha numeric should be treated as delimiters
     */
    protected static StringBuilder[] fillCompsArray(String part) {
       if (part == null) {
            return null;
       }
       StringBuilder[] comps = new StringBuilder[part.length()];
       char[] chars = part.toCharArray();
       int arrayIndex = 0;
       int index = 0;
       int state = 0; //0 - start , 1 - alphabetic, 2 - numeric - 3 other
       StringBuilder current = new StringBuilder();
        // The maximum number of segments is the number of characters in the string
       while (index < part.length()) {
            // The current character is letter
            if (isLetter(chars[index])) {
                if (state == 2) {
                    // in case the current state is "numeric"
                    // this marks the end of segment, so
                    // put the segment in the array and
                    // create new buffer for the new segment
                    comps[arrayIndex++] = current;
                    current = new StringBuilder();
                }
                // change state to "alphabetic"
                state = 1;
                current.append(chars[index]);
            } else if (Character.isDigit(chars[index])) {
            // The current character is numeric
                if (state == 1) {
                    // in case the current state is "alphabetic"
                    // this marks the end of segment, so
                    // put the segment in the array and
                    // create new buffer for the new segment
                    comps[arrayIndex++] = current;
                    current = new StringBuilder();
                }
                // change state to "numeric"
                state = 2;
                current.append(chars[index]);
            } else {
                // The character is not a number nor a letter
                // This means a segment has ended
                // The segment should be added to the array
                // and a new buffer should be created
                if (state == 2 || state == 1 || state == 3) {
                    comps[arrayIndex++] = current;
                    current = new StringBuilder();
                }
                state = 3;
            }
            index++;
        }
        if (current.length() > 0) {
            comps[arrayIndex] = current;
        }
        return comps;

    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * Splits RPM into parts of Prefix, Version and Release
     *
     * @return array of strings filled with prefix,version,release strings (in this order)
     */
    public static String[] splitRpmToParts(String rpmName) {
        int lastDashIndex = rpmName.lastIndexOf('-');
        if (lastDashIndex == -1) {
            throw new IllegalArgumentException(INVALID_RPM_NAME_FORMAT);
        }

        char[] chars = rpmName.toCharArray();

        int beforeLastDashIndex = lastDashIndex - 1;
        while (beforeLastDashIndex >= 0) {
            if (chars[beforeLastDashIndex] == '-') {
                break;
            }
            beforeLastDashIndex--;
        }
        if (beforeLastDashIndex == -1) {
            throw new IllegalArgumentException(INVALID_RPM_NAME_FORMAT);
        }

        String[] parts = new String[3];
        parts[0] = rpmName.substring(0, beforeLastDashIndex);
        parts[1] = rpmName.substring(beforeLastDashIndex + 1, lastDashIndex);
        parts[2] = rpmName.substring(lastDashIndex + 1);
        return parts;
    }

    /**
     * Checks if an update is available for host OS
     *
     * @param isos
     *            an images which may upgrade the given host
     * @param hostOs
     *            the examined host OS
     * @return {@code true} if an update is available, else {@code false}
     */
    public static boolean isUpdateAvailable(List<RpmVersion> isos, String hostOs) {

        String[] hostOsParts = hostOs.split("-");
        for (int i = 0; i < hostOsParts.length; i++) {
            hostOsParts[i] = hostOsParts[i].trim();
        }

        // hostOs holds the following components:
        // hostOs[0] holds prefix
        // hostOs[1] holds version
        // hostOs[2] holds release
        final int VERSION_FIELDS_NUMBER = 4;

        // Fix hostOs[1] to be format of major.minor.build.revision
        // Add ".0" for missing parts
        String[] hostOsVersionParts = hostOsParts[1].split("\\.");
        for (int i = 0; i < VERSION_FIELDS_NUMBER - hostOsVersionParts.length; i++) {
            hostOsParts[1] = hostOsParts[1].trim() + ".0";
        }

        Version hostVersion = new Version(hostOsParts[1].trim());
        String releaseHost = hostOsParts[2].trim();

        for (RpmVersion iso : isos) {
            // Major check
            if (hostVersion.getMajor() == iso.getMajor()) {
                // Minor and Buildiso.getRpmName()
                if (iso.getMinor() > hostVersion.getMinor() || iso.getBuild() > hostVersion.getBuild()) {
                    return true;
                }

                String rpmFromIso = iso.getRpmName();

                // Removes the ".iso" file extension , and get the release part from it
                int isoIndex = rpmFromIso.indexOf(".iso");
                if (isoIndex != -1) {
                    rpmFromIso = iso.getRpmName().substring(0, isoIndex);
                }

                if (RpmVersionUtils.compareRpmParts(RpmVersionUtils.splitRpmToParts(rpmFromIso)[2], releaseHost) > 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
