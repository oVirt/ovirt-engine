package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
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
    public static final Version v4_2 = new Version(4, 2);
    public static final Version v4_3 = new Version(4, 3);
    public static final Version v4_4 = new Version(4, 4);
    public static final Version v4_5 = new Version(4, 5);
    public static final Version v4_6 = new Version(4, 6);
    public static final Version v4_7 = new Version(4, 7);
    public static final List<Version> ALL =
            Collections.unmodifiableList(Arrays.asList(v4_2, v4_3, v4_4, v4_5, v4_6, v4_7));
    public static final int VERSION_NOT_SET = -1;

    public Version(String value) {
        this();
        setValue(value);
    }

    public Version() {
        major = minor = build = revision = VERSION_NOT_SET;
    }

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
        build = revision = VERSION_NOT_SET;
    }

    public Version(int major2, int minor2, int build2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = VERSION_NOT_SET;
    }

    public Version(int major2, int minor2, int build2, Integer revision2) {
        this.major = major2;
        this.minor = minor2;
        this.build = build2;
        this.revision = revision2;
    }

    public String getValue() {
        final StringBuilder val = new StringBuilder();

        if (this.major > VERSION_NOT_SET) {
            val.append(this.major);
        }
        appendVersionComponent(val, this.minor);
        appendVersionComponent(val, this.build);
        appendVersionComponent(val, this.revision);
        return val.toString();
    }

    private static void appendVersionComponent(StringBuilder val, int versionNumber) {
        if (versionNumber > VERSION_NOT_SET) {
            if (val.length() != 0) {
                val.append('.');
            }
            val.append(versionNumber);
        }
    }

    public void setValue(String value) {
        if (value == null || value.isEmpty() || value.equals("*")) {
            major = minor = build = revision = VERSION_NOT_SET;
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

    /**
     * @return true if this version is within the range of the candidate versions or below it
     */
    public boolean lessOrEquals(Collection<Version> candidates) {
        return candidates.stream().anyMatch(this::lessOrEquals);
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
        return major == VERSION_NOT_SET && minor == VERSION_NOT_SET && revision == VERSION_NOT_SET
                && build == VERSION_NOT_SET;
    }

    public static Version getLast() {
        return ALL.get(ALL.size() - 1);
    }

    public static Version getLowest() {
        return ALL.get(0);
    }
}
