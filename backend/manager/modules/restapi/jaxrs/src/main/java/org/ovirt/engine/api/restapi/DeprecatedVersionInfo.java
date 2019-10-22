/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a deprecated version of the API, including the versions of the engine where it was deprecated
 * and where it will be removed.
 */
public class DeprecatedVersionInfo {
    private static final Logger log = LoggerFactory.getLogger(DeprecatedVersionInfo.class);

    // Regular expression used to extract the versions from the configuration file:
    private static final String VERSION_GROUP = "version";
    private static final String REMOVING_GROUP = "removing";
    private static final String DEPRECATING_GROUP = "deprecating";
    private static final Pattern VERSION_RE = Pattern.compile(
        "^(?<" + VERSION_GROUP + ">[0-9]+):(?<" + DEPRECATING_GROUP + ">[^:]+):(?<" + REMOVING_GROUP + ">[^:]+)$"
    );

    /**
     * Parses the given deprecated version specification and creates an instance of this class.
     *
     * @param specification the text containing the specification of the deprecated version
     * @return the created instance, or {@code null} if the given text doesn't match the expected regular expression
     */
    public static DeprecatedVersionInfo parse(String specification) {
        Matcher matcher = VERSION_RE.matcher(specification);
        if (!matcher.matches()) {
            log.error(
                "The deprecated version specification \"{}\" doesn't match the expected regular expression \"{}\", " +
                "will return null.",
                specification, VERSION_RE
            );
            return null;
        }
        String version = matcher.group(VERSION_GROUP);
        String deprecating = matcher.group(DEPRECATING_GROUP);
        String removing = matcher.group(REMOVING_GROUP);
        return new DeprecatedVersionInfo(version, deprecating, removing);
    }

    // The versions of the API:
    private String version;

    // The versions of the engine where the engine where the version of the API was deprecated and where it will be
    // removed:
    private String deprecating;
    private String removing;

    private DeprecatedVersionInfo(String version, String deprecating, String removing) {
        this.version = version;
        this.deprecating = deprecating;
        this.removing = removing;
    }

    public String getVersion() {
        return version;
    }

    public String getDeprecating() {
        return deprecating;
    }

    public String getRemoving() {
        return removing;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(version.length() + 1 + deprecating.length() + 1 + removing.length());
        buffer.append(version);
        buffer.append(":");
        buffer.append(deprecating);
        buffer.append(":");
        buffer.append(removing);
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeprecatedVersionInfo) {
            DeprecatedVersionInfo that = (DeprecatedVersionInfo) obj;
            return
                Objects.equals(this.version, that.getVersion()) &&
                Objects.equals(this.deprecating, that.getDeprecating()) &&
                Objects.equals(this.removing, that.getRemoving());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, deprecating, removing);
    }
}
