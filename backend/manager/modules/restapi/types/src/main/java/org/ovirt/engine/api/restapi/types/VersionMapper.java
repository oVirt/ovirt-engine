package org.ovirt.engine.api.restapi.types;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionMapper {
    private static final Logger log = LoggerFactory.getLogger(VersionMapper.class);

    // Regular expression used to extract information from version numbers.
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^(?<major>\\d+)(\\.(?<minor>\\d+)(\\.(?<build>\\d+))?)?[^-]*(-(?<revision>\\d+))?.*$"
    );

    @Mapping(from = List.class, to = Versions.class)
    public static Versions map(List<org.ovirt.engine.core.compat.Version> entity, Versions template) {
        Versions model = template != null ? template : new Versions();
        for (org.ovirt.engine.core.compat.Version version : entity) {
            Version v = new Version();
            v.setMajor(version.getMajor());
            v.setMinor(version.getMinor());
            model.getVersions().add(v);
        }
        return model;
    }

    public static Version map(org.ovirt.engine.core.compat.Version versionEngine) {
        Version versionApi = new Version();
        versionApi.setMajor(versionEngine.getMajor() >= 0 ? versionEngine.getMajor() : null);
        versionApi.setMinor(versionEngine.getMinor() >= 0 ? versionEngine.getMinor() : null);
        versionApi.setBuild(versionEngine.getBuild() >= 0 ? versionEngine.getBuild() : null);
        versionApi.setRevision(versionEngine.getRevision() >= 0 ? versionEngine.getRevision() : null);
        return versionApi;
    }

    public static org.ovirt.engine.core.compat.Version map(Version versionApi) {
        return new org.ovirt.engine.core.compat.Version(
                versionApi.getMajor() != null ? versionApi.getMajor() : -1,
                versionApi.getMinor() != null ? versionApi.getMinor() : -1,
                versionApi.getBuild() != null ? versionApi.getBuild() : -1,
                versionApi.getRevision() != null ? versionApi.getRevision() : -1);
    }

    public static Version fromVersionString(String versionString) {
        if (versionString == null) {
            return null;
        }
        Version version = new Version();
        version.setFullVersion(versionString);
        Matcher match = VERSION_PATTERN.matcher(versionString);
        if (match.matches()) {
            String major = match.group("major");
            String minor = match.group("minor");
            String build = match.group("build");
            String revision = match.group("revision");
            if (major != null) {
                version.setMajor(Integer.parseInt(major));
            }
            if (minor != null) {
                version.setMinor(Integer.parseInt(minor));
            }
            if (build != null) {
                version.setBuild(Integer.parseInt(build));
            }
            if (revision != null) {
                version.setRevision(Integer.parseInt(revision));
            }
        } else {
            log.warn(
                "The version string \"{}\" doesn't match the expected pattern, only the full version will be reported.",
                versionString
            );
        }
        return version;
    }
}

