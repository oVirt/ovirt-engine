package org.ovirt.engine.api.restapi.types;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.api.model.SupportedVersions;
import org.ovirt.engine.api.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionMapper {
    private static final Logger log = LoggerFactory.getLogger(VersionMapper.class);

    // Regular expression used to extract information from version numbers.
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^(?<major>\\d+)(\\.(?<minor>\\d+)(\\.(?<build>\\d+))?)?[^-]*(-(?<revision>\\d+))?.*$"
    );

    @Mapping(from = List.class, to = SupportedVersions.class)
    public static SupportedVersions map(List<org.ovirt.engine.core.compat.Version> entity, SupportedVersions template) {
        SupportedVersions model = template != null ? template : new SupportedVersions();
        for (org.ovirt.engine.core.compat.Version version : entity) {
            Version v = new Version();
            v.setMajor(version.getMajor());
            v.setMinor(version.getMinor());
            model.getVersions().add(v);
        }
        return model;
    }

    @Mapping(from = org.ovirt.engine.core.compat.Version.class, to = Version.class)
    public static Version map(org.ovirt.engine.core.compat.Version versionEngine, Version versionApi) {
        if (versionApi == null) {
            versionApi = new Version();
        }
        versionApi.setMajor(versionEngine.getMajor());
        versionApi.setMinor(versionEngine.getMinor());
        versionApi.setBuild(versionEngine.getBuild());
        versionApi.setRevision(versionEngine.getRevision());
        return versionApi;
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
        }
        else {
            log.warn(
                "The version string \"{}\" doesn't match the expected pattern, only the full version will be reported.",
                versionString
            );
        }
        return version;
    }
}
