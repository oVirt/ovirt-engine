package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

public class VdsOperationActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 4156122527623908516L;

    @Valid
    private VdsStatic vdsStatic;

    private String password;

    private boolean overrideFirewall;

    /**
     * reboot the installed Host when done
     */
    private boolean rebootAfterInstallation = true;

    public VdsOperationActionParameters(VdsStatic vdsStaticVal, String passwordVal) {
        super(vdsStaticVal.getId());
        if ("".equals(vdsStaticVal.getManagementIp())) {
            vdsStaticVal.setManagementIp(null);
        }
        vdsStatic = vdsStaticVal;
        password = passwordVal;
    }

    public VdsOperationActionParameters(VdsStatic vdsStatic) {
        this(vdsStatic, null);
    }

    public VdsStatic getVdsStaticData() {
        return vdsStatic;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password = value;
    }

    public VdsOperationActionParameters() {
    }

    // Deprecated to keep old api with root password
    public String getRootPassword() {
        return password;
    }

    public void setRootPassword(String value) {
        password = value;
    }

    public VDS getvds() {
        VDS vds = new VDS();
        vds.setStaticData(vdsStatic);
        return vds;
    }

    public void setvds(VDS value) {
        vdsStatic = value.getStaticData();
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
