package org.ovirt.engine.core.compat;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to extract and represent a version of RPM. It support several patterns:
 * <li> {@literal <name>-<version>-<release>.<architecture>.rpm} - version part is not allowed to contain "-".
 * Supported by {@link RpmVersion#RpmVersion(String)}
 * <li> {@literal <prefix+name>[ -].*<[0-9].*\\.}{1,3}[0-9] {@literal><any text>} e.g:
 * <i>rhev-agent-2.3.7-1.el6</i> or <i>RHEV-Agent 2.3.395</i>. Supported by {@link RpmVersion#RpmVersion(String, String, boolean)}
 */
public class RpmVersion extends Version {

    private static final long serialVersionUID = 5938069430310131270L;
    private static final String RPM_REGEX = "([^ ]+)\\-([0-9][^ \\-]*\\-[0-9\\.]+)\\.(.*)";
    private static final Pattern rpmCompiled = Pattern.compile(RPM_REGEX);
    private String rpmName;
    private String rpmRelease;

    public RpmVersion() {
        super();
    }

    /**
     * {@literal Supports RPM format by spec: <name>-<version>-<release>.<architecture>.rpm}
     * @param rpmName
     *            the RPM name
     */
    public RpmVersion(String rpmName) {
        this.rpmName = rpmName;
        setValue(extractRpmVersion(rpmName));
        extractRpmRelease(rpmName);
    }

    /**
     * Support RPM version extraction where a given prefix is omitted from the RPM name.
     * @param rpmName
     *            the full RPM name
     * @param namePrefix
     *            a prefix which is part of the RPM name and should ignored (case insensitive) while extracting the
     *            version
     * @param ignoreCaseSensitive
     *            an indicator whether to ignore the case of the given prefix, if known ahead
     */
    public RpmVersion(String rpmName, String namePrefix, boolean ignoreCaseSensitive) {
        this.rpmName = rpmName;
        if (rpmName == null || rpmName.isEmpty()) {
            setValue(rpmName);
        } else {
            if (namePrefix == null) {
                namePrefix = "";
            }
            int start = -1;
            if (ignoreCaseSensitive) {
                start = rpmName.toLowerCase().indexOf(namePrefix.toLowerCase());
            } else {
                start = rpmName.indexOf(namePrefix);
            }

            String raw;
            if (start > -1) {
                raw = rpmName.substring(start + namePrefix.length());
            } else {
                raw = rpmName;
            }
            extractRpmRelease(raw);
            setValue(extractRpmVersion(raw.toCharArray()));
        }
    }

    private void extractRpmRelease(String rpmName) {
        if (rpmName == null || rpmName.isEmpty()) {
            return;
        }
        int lastDashIndex = rpmName.lastIndexOf('-');
        if (lastDashIndex == -1) {
            return;
        }
        rpmRelease = rpmName.substring(lastDashIndex + 1);
    }

    public String getRpmName() {
        return this.rpmName;
    }

    public void setRpmName(String rpmName) {
        this.rpmName = rpmName;
    }

    /**
     * Extracts the version out of a given array of characters by the following algorithm:
     * <li>Find first digit location after tool name (case ignored).
     * <li>Parse as many version parts elements as provided, up to 4.
     * <li>Set default value "0" for the missing parts.
     * @param version
     *            the char material which contains the version
     * @return a string contains a version in format of w.x.y.z where w,x,y and z are int.
     */
    private String extractRpmVersion(char[] version) {
        int start = indexOfFirstDigit(version);
        int end = version.length;
        int dots = 3;

        for (int i = start; i < end; i++) {
            if (!Character.isDigit(version[i]) && version[i] != '.') {
                end = i;
                break;
            } else if (version[i] == '.') {
                if (dots == 0) {
                    end = i;
                    break;
                }
                dots--;
            }
        }

        StringBuilder sb = new StringBuilder(new String(version, start, end - start));
        for (int i = 0; i < dots; i++) {
            sb.append(".0");
        }
        return sb.toString();
    }

    private String extractRpmVersion(String rpmName) {
        if (rpmName == null || rpmName.isEmpty()) {
            return rpmName;
        }
        Matcher matchToolPattern = rpmCompiled.matcher(rpmName);

        String rawString = null;
        if (matchToolPattern.find() && matchToolPattern.groupCount() > 1) {
            rawString = matchToolPattern.group(2);
            return extractRpmVersion(rawString.toCharArray());
        }
        return null;
    }

    private static int indexOfFirstDigit(char[] version) {
        int i = 0;
        for (; i < version.length; i++) {
            if (Character.isDigit(version[i])) {
                break;
            }
        }
        return i;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                rpmName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RpmVersion)) {
            return false;
        }
        RpmVersion other = (RpmVersion) obj;
        return super.equals(obj)
                && Objects.equals(rpmName, other.rpmName);
    }

    public String getRpmRelease() {
        return rpmRelease;
    }

    public void setRpmRelease(String rpmRelease) {
        this.rpmRelease = rpmRelease;
    }

}
