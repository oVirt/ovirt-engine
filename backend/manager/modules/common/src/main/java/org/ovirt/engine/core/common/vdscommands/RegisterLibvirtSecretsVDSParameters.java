package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class RegisterLibvirtSecretsVDSParameters extends VdsIdVDSCommandParametersBase {

    private List<LibvirtSecret> libvirtSecrets;
    private boolean clearUnusedSecrets;

    public RegisterLibvirtSecretsVDSParameters(Guid vdsId, List<LibvirtSecret> libvirtSecrets) {
        super(vdsId);
        this.libvirtSecrets = libvirtSecrets;
    }

    public RegisterLibvirtSecretsVDSParameters(Guid vdsId, List<LibvirtSecret> libvirtSecrets,
                                               boolean clearUnusedSecrets) {
        super(vdsId);
        this.libvirtSecrets = libvirtSecrets;
        this.clearUnusedSecrets = clearUnusedSecrets;
    }

    public RegisterLibvirtSecretsVDSParameters() {
    }

    public List<LibvirtSecret> getLibvirtSecrets() {
        return libvirtSecrets;
    }

    public void setLibvirtSecrets(List<LibvirtSecret> libvirtSecrets) {
        this.libvirtSecrets = libvirtSecrets;
    }

    public boolean isClearUnusedSecrets() {
        return clearUnusedSecrets;
    }

    public void setClearUnusedSecrets(boolean clearUnusedSecrets) {
        this.clearUnusedSecrets = clearUnusedSecrets;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("libvirtSecrets", getLibvirtSecrets())
                .append("clearUnusedSecrets", isClearUnusedSecrets());
    }
}
