package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "InstallVdsParameters")
public class InstallVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 5066290843683399113L;

    public InstallVdsParameters(Guid vdsId, String password) {
        super(vdsId);
        setRootPassword(password);
    }

    @XmlElement(name = "RootPassword")
    private String privateRootPassword;

    public String getRootPassword() {
        return privateRootPassword;
    }

    private void setRootPassword(String value) {
        privateRootPassword = value;
    }

    @XmlElement(name = "IsReinstallOrUpgrade")
    private boolean privateIsReinstallOrUpgrade;

    public boolean getIsReinstallOrUpgrade() {
        return privateIsReinstallOrUpgrade;
    }

    public void setIsReinstallOrUpgrade(boolean value) {
        privateIsReinstallOrUpgrade = value;
    }

    @XmlElement(name = "oVirtIsoFile")
    private String privateoVirtIsoFile;

    public String getoVirtIsoFile() {
        return privateoVirtIsoFile;
    }

    public void setoVirtIsoFile(String value) {
        privateoVirtIsoFile = value;
    }

    @XmlElement(name = "OverrideFirewall")
    private boolean overrideFirewall;

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }

    public InstallVdsParameters() {
    }
}
