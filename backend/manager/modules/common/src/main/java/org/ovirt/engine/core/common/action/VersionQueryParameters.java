package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Version;

public class VersionQueryParameters extends VdcQueryParametersBase {
    private Version version;

    public VersionQueryParameters(Version version) {
        this.version = version;
    }

    public VersionQueryParameters() {
    }

    public Version getVersion() {
        return version;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("version", getVersion());
    }
}
