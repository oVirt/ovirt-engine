package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.ovirt.engine.api.model.SupportedVersions;
import org.ovirt.engine.api.model.Version;

public class VersionMapper {

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
}

