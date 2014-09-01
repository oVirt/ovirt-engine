package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Version in .Net style: a.b.c.d when a: major version, b: minor version , c: major revision, d: minor revision
 */
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3938214651005908651L;

    private int major;
    private int minor;
    private int build;
    private int revision;

    public static final Version v3_0 = new Version(3, 0);
    public static final Version v3_1 = new Version(3, 1);
    public static final Version v3_2 = new Version(3, 2);
    public static final Version v3_3 = new Version(3, 3);
    public static final Version v3_4 = new Version(3, 4);
    public static final Version v3_5 = new Version(3, 5);
    public static final List<Version> ALL = Arrays.asList(v3_0, v3_1, v3_2, v3_3, v3_4, v3_5);


    public Version(String value) {
        this();
        setValue(value);
    }

    public Version() {
        major = minor = build = revision = -1;
    }

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
        build = revision = -1;
    }

    public Version(int major2, int minor2, int build2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = -1;
    }

    public Version(int major2, int minor2, int build2, Integer revision2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = revision2;
    }

    public String getValue() {
        final StringBuilder val = new StringBuilder();

        if (this.major > -1) {
            val.append(this.major);
        }
        appendVersionComponent(val, this.minor);
        appendVersionComponent(val, this.build);
        appendVersionComponent(val, this.revision);
        return val.toString();
    }

    private static void appendVersionComponent(StringBuilder val, int versionNumber) {
        if (versionNumber > -1) {
            if (val.length() != 0) {
                val.append('.');
            }
            val.append(versionNumber);
        }
    }

    public void setValue(String value) {
        if (value == null || value.isEmpty() || value.equals("*")) {
            major = minor = build = revision = -1;
        } else {
            String[] partialVersions = value.split("\\.");
            switch (partialVersions.length) {
            case (4):
                revision = Integer.parseInt(partialVersions[3]);
            case (3):
                build = Integer.parseInt(partialVersions[2]);
            case (2):
                minor = Integer.parseInt(partialVersions[1]);
            case (1):
                major = Integer.parseInt(partialVersions[0]);
            }
        }
    }

    public String toString(int i) {
        StringBuilder sb = new StringBuilder();
        switch (i) {
        case (4):
            sb.append('.').append(revision);
        case (3):
            sb.insert(0, build).insert(0, '.');
        case (2):
            sb.insert(0, minor).insert(0, '.');
        case (1):
            sb.insert(0, major);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public int compareTo(Version other) {
        if (other == null)
            return 5;
        if (this == other)
            return 0;
        int result = major - other.major;
        if (result == 0) {
            result = minor - other.minor;
            if (result == 0) {
                result = build - other.build;
                if (result == 0) {
                    result = revision - other.revision;
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + revision;
        result = prime * result + build;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Version other = (Version) obj;
        return major == other.major && minor == other.minor && revision == other.revision && build == other.build;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getBuild() {
        return this.build;
    }

    public int getRevision() {
        return this.revision;
    }

    public boolean isNotValid() {
        return (major == -1 && minor == -1 && revision == -1 && build == -1);
    }

    public static Version getLast() {
        return ALL.get(ALL.size() - 1);
    }
}
