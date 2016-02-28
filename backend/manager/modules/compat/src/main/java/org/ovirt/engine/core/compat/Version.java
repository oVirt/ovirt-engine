package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Version in .Net style: a.b.c.d when a: major version, b: minor version , c: major revision, d: minor revision
 */
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3938214651005908651L;

    private int major;
    private int minor;
    private int build;
    private int revision;

    // please note that versions must be in sync with dbscripts/common_sp.sql::fn_db_add_config_value_for_versions_up_to
    public static final Version v3_6 = new Version(3, 6);
    public static final Version v4_0 = new Version(4, 0);
    public static final List<Version> ALL = Collections.unmodifiableList(Arrays.asList(v3_6, v4_0));


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
            case 4:
                revision = Integer.parseInt(partialVersions[3]);
            case 3:
                build = Integer.parseInt(partialVersions[2]);
            case 2:
                minor = Integer.parseInt(partialVersions[1]);
            case 1:
                major = Integer.parseInt(partialVersions[0]);
            }
        }
    }


    /**
     * @return true if this instance version is greater than candidate
     */
    public boolean greater(Version candidate) {
        return this.compareTo(candidate) > 0;
    }

    /**
     * @return true if this instance version is less than candidate
     */
    public boolean less(Version candidate) {
        return this.compareTo(candidate) < 0;
    }

    /**
     * @return true if this instance version is greater or equals candidate
     */
    public boolean greaterOrEquals(Version candidate) {
        return this.compareTo(candidate) >= 0;
    }

    /**
     * @return true if this instance version is less or equals to candidate
     */
    public boolean lessOrEquals(Version candidate) {
        return this.compareTo(candidate) <= 0;
    }

    public String toString(int i) {
        StringBuilder sb = new StringBuilder();
        switch (i) {
        case 4:
            sb.append('.').append(revision);
        case 3:
            sb.insert(0, build).insert(0, '.');
        case 2:
            sb.insert(0, minor).insert(0, '.');
        case 1:
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
        if (other == null) {
            return 5;
        }
        if (this == other) {
            return 0;
        }
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
        return Objects.hash(
                major,
                minor,
                revision,
                build
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Version)) {
            return false;
        }
        Version other = (Version) obj;
        return major == other.major
                && minor == other.minor
                && revision == other.revision
                && build == other.build;
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
        return major == -1 && minor == -1 && revision == -1 && build == -1;
    }

    public static Version getLast() {
        return ALL.get(ALL.size() - 1);
    }
}
