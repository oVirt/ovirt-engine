package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.SupportedVersions;
import org.ovirt.engine.api.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionMapper {
    private static final Logger log = LoggerFactory.getLogger(VersionMapper.class);

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

    public static Version fromKernelVersionString(String kernelVersionString) {
        if(StringUtils.isEmpty(kernelVersionString)) {
            return null;
        }
        String[] parts = kernelVersionString.split("\\.");
        if(parts.length < 3) {
            return null;
        }
        String[] last = parts[2].split("-");
        try {
            Version version = new Version();
            version.setMajor(Integer.parseInt(parts[0]));
            version.setMinor(Integer.parseInt(parts[1]));
            version.setBuild(Integer.parseInt(last[0]));
            version.setRevision(Integer.parseInt(last[1]));
            return version;
        }
        catch(NumberFormatException e) {
            log.error("Failed to parse kernel version string", e);
            return null;
        }
    }


    public static Version fromVersionString(String versionString) {
        if(StringUtils.isEmpty(versionString)) {
            return null;
        }
        String[] parts = versionString.split("\\.");
        Version version = new Version();
        if(parts.length < 1) {
            return null;
        }
        try {
            version.setMajor(Integer.parseInt(StringUtils.stripStart(parts[0], "0")));
        }
        catch(NumberFormatException e) {
            log.error("Failed to map version string major component", e);
            return null;
        }
        if (parts.length > 1) {
            try {
                version.setMinor(Integer.parseInt(StringUtils.stripStart(parts[1], "0")));
            }
            catch(NumberFormatException e) {
                log.error("Failed to map version string minor component", e);
                return version;
            }
        }
        if (parts.length > 2) {
            try {
                version.setBuild(Integer.parseInt(StringUtils.stripStart(parts[2], "0")));
            }
            catch(NumberFormatException e) {
                log.error("Failed to map version string build component", e);
                return version;
            }
        }
        if (parts.length > 3) {
            try {
                version.setRevision(Integer.parseInt(StringUtils.stripStart(parts[3], "0")));
            }
            catch(NumberFormatException e) {
                log.error("Failed to map version string revision component", e);
                return version;
            }
        }
        return version;
    }
}

