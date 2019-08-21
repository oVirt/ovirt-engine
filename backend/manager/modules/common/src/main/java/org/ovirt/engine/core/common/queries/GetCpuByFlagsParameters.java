package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Version;

public class GetCpuByFlagsParameters extends QueryParametersBase {

    private static final long serialVersionUID = -3654629773581481993L;

    private String flags;

    private Version newVersion;

    public GetCpuByFlagsParameters() {
    }

    public GetCpuByFlagsParameters(String flags, Version newVersion) {
        super();
        this.flags = flags;
        this.newVersion = newVersion;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getFlags() {
        return flags;
    }

    public void setNewVersion(Version newVersion) {
        this.newVersion = newVersion;
    }

    public Version getNewVersion() {
        return newVersion;
    }
}
