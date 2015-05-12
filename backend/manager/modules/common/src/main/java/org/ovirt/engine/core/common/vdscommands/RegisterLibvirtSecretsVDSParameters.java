package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public class RegisterLibvirtSecretsVDSParameters extends VdsIdVDSCommandParametersBase {

    private List<LibvirtSecret> libvirtSecrets;

    public RegisterLibvirtSecretsVDSParameters(Guid vdsId, List<LibvirtSecret> libvirtSecrets) {
        super(vdsId);
        this.libvirtSecrets = libvirtSecrets;
    }

    public RegisterLibvirtSecretsVDSParameters() {
    }

    public List<LibvirtSecret> getLibvirtSecrets() {
        return libvirtSecrets;
    }

    public void setLibvirtSecrets(List<LibvirtSecret> libvirtSecrets) {
        this.libvirtSecrets = libvirtSecrets;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("libvirtSecrets", getLibvirtSecrets());
    }
}
