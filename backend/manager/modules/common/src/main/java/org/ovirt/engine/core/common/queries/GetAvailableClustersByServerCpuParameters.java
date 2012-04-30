package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAvailableClustersByServerCpuParameters extends GetAllServerCpuListParameters {
    private static final long serialVersionUID = -6088424248179452456L;

    public GetAvailableClustersByServerCpuParameters(String cpuName, Version version) {
        super(version);
        _cpuName = cpuName;
    }

    private String _cpuName;

    public String getCpuName() {
        return _cpuName;
    }

    public GetAvailableClustersByServerCpuParameters() {
    }
}
