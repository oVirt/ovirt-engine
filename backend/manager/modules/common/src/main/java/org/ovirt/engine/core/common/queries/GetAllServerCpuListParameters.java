package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Version;

public class GetAllServerCpuListParameters extends QueryParametersBase {
    private static final long serialVersionUID = -6048741913142095068L;

    public GetAllServerCpuListParameters(Version version) {
        _version = version;
    }

    private Version _version;

    public Version getVersion() {
        return _version;
    }

    public GetAllServerCpuListParameters() {
    }
}
