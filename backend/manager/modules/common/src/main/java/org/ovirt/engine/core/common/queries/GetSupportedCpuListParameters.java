package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Version;

public class GetSupportedCpuListParameters extends QueryParametersBase {

    private static final long serialVersionUID = -6961495931946670043L;

    private String maxCpuName;
    private Version version;

    public GetSupportedCpuListParameters(String maxCpuName, Version version) {
        this.maxCpuName = maxCpuName;
        this.version = version;
    }

    public GetSupportedCpuListParameters(String maxCpuName) {
        this.maxCpuName = maxCpuName;
    }

    public GetSupportedCpuListParameters() {
    }

    public String getMaxCpuName() {
        return maxCpuName;
    }

    public void setMaxCpuName(String maxCpuName) {
        this.maxCpuName = maxCpuName;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
