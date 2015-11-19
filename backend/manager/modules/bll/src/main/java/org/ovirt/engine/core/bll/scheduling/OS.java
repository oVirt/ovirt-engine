package org.ovirt.engine.core.bll.scheduling;

import java.util.Objects;

import org.ovirt.engine.core.compat.Version;

public class OS {

    private String name;

    private Version version;

    public OS() {
        name = "";
        version = new Version();
    }

    public OS(String name, Version version) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        this.name = name;
        this.version = new Version(version.getMajor(), version.getMinor(), version.getBuild(), version.getRevision());

    }

    public boolean isValid() {
        return !version.isNotValid() && !name.isEmpty();
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

    public Version getVersion() {
        return version;
    }

    public static OS fromPackageVersionString(String packageVersionString) {
        if (packageVersionString == null) {
            return new OS();
        }
        String[] os = packageVersionString.split(" - ", 3);
        if (os.length < 2) {
            return new OS();
        }
        final String name = os[0].trim();
        final Version version = new Version(os[1].trim());
        return new OS(name, version);
    }
}
