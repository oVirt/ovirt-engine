package org.ovirt.engine.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Version;

public class OS {

    private static final Pattern versionPattern = Pattern.compile("(^[\\d\\.]+)");
    private static final List<String> OS_IDENTIFIER = Arrays.asList("RHEL", "oVirt Node", "RHEV Hypervisor");
    private static final String OS_DELIMITER = " - ";
    private String name;
    private Version version;
    private String fullVersion;

    public OS() {
        name = "";
        version = new Version();
        fullVersion = "";
    }

    public OS(String name, Version version, String fullVersion) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        this.name = name.trim();
        this.version = new Version(version.getMajor(), version.getMinor(), version.getBuild(), version.getRevision());
        this.fullVersion = fullVersion;
    }

    public boolean isValid() {
        return !version.isNotValid() && !name.isEmpty();
    }

    public boolean isSameOsFamily(final OS os) {
        return isSameOs(os) ||
                OS_IDENTIFIER.contains(os.getName()) && OS_IDENTIFIER.contains(this.getName());
    }

    public boolean isSameOs(final OS os) {
        return getName().equals(os.getName());
    }

    public boolean isNewerThan(final OS os) {
        return getVersion().greater(os.getVersion());
    }

    public boolean isOlderThan(final OS os) {
        return getVersion().less(os.getVersion());
    }

    public boolean isSameMajorVersion(final OS os) {
        return getVersion().getMajor() == os.getVersion().getMajor();
    }

    public String getName() {
        return name;
    }

    public String getOsFamily() {
        return OS_IDENTIFIER.contains(name) ? "RHEL" : name;
    }

    public Version getVersion() {
        return version;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public static OS fromPackageVersionString(String packageVersionString) {
        if (packageVersionString == null) {
            return new OS();
        }
        String[] os = packageVersionString.split(OS_DELIMITER, 3);
        if (os.length < 2) {
            return new OS();
        }
        final String name = os[0].trim();
        final Matcher versionMatcher = versionPattern.matcher(os[1].trim());
        final Version version;
        if (versionMatcher.find()) {
            if (name != null && name.toLowerCase().startsWith("fedora")) {
                int major = extractVersionPart(versionMatcher.group());
                int minor = extractVersionPart(os[2].trim());
                version = new Version(major, minor);
            } else {
                version = new Version(versionMatcher.group());
            }
        } else if (os.length == 3 && os[2].contains("el6")) {
            version = new Version(6, -1);
        } else if (os.length == 3 && os[2].contains("el7")) {
            version = new Version(7, -1);
        } else {
            version = new Version();
        }

        final String fullVersion = StringUtils.join(Arrays.copyOfRange(os, 1, os.length), OS_DELIMITER);
        return new OS(name, version, fullVersion);
    }

    private static int extractVersionPart(String versionPartStr) {
        if (StringUtils.isBlank(versionPartStr)) {
            return Version.VERSION_NOT_SET;
        }

        try {
            return Integer.parseInt(versionPartStr);
        } catch (NumberFormatException e) {
            return Version.VERSION_NOT_SET;
        }
    }
}
