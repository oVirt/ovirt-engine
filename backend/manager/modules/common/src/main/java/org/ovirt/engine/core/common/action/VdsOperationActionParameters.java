package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

public class VdsOperationActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 4156122527623908516L;

    @Valid
    private VdsStatic _vdsStatic;

    private String _rootPassword;

    private boolean overrideFirewall;

    /**
     * reboot the installed Host when done
     */
    private boolean rebootAfterInstallation = true;

    public VdsOperationActionParameters(VdsStatic vdsStatic, String rootPassword) {
        super(vdsStatic.getId());
        if ("".equals(vdsStatic.getManagementIp())) {
            vdsStatic.setManagementIp(null);
        }
        _vdsStatic = vdsStatic;
        _rootPassword = rootPassword;
    }

    public VdsOperationActionParameters(VdsStatic vdsStatic) {
        this(vdsStatic, null);
    }

    public VdsStatic getVdsStaticData() {
        return _vdsStatic;
    }

    public String getRootPassword() {
        return _rootPassword;
    }

    public void setRootPassword(String value) {
        _rootPassword = value;
    }

    public VdsOperationActionParameters() {
    }

    public VDS getvds() {
        VDS vds = new VDS();
        vds.setStaticData(_vdsStatic);
        return vds;
    }

    public void setvds(VDS value) {
        _vdsStatic = value.getStaticData();
    }

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }

    public boolean isRebootAfterInstallation() {
        return rebootAfterInstallation;
    }

    public void setRebootAfterInstallation(boolean rebootAfterInstallation) {
        this.rebootAfterInstallation = rebootAfterInstallation;
    }

}
