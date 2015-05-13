package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class UnregisterLibvirtSecretsVDSParameters extends VdsIdVDSCommandParametersBase {

    private List<Guid> libvirtSecretsUuids;

    public UnregisterLibvirtSecretsVDSParameters(Guid vdsId, List<Guid> libvirtSecretsUuids) {
        super(vdsId);
        this.libvirtSecretsUuids = libvirtSecretsUuids;
    }

    public UnregisterLibvirtSecretsVDSParameters() {
    }

    public List<Guid> getLibvirtSecretsUuids() {
        return libvirtSecretsUuids;
    }

    public void setLibvirtSecretsUuids(List<Guid> libvirtSecretsUuids) {
        this.libvirtSecretsUuids = libvirtSecretsUuids;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("libvirtSecretsUuids", getLibvirtSecretsUuids());
    }
}
