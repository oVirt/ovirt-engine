package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.uicommon.AbstractConsole;
import org.ovirt.engine.ui.uicommonweb.restapi.HasForeignMenuData;

public abstract class AbstractConsoleWithForeignMenu extends AbstractConsole implements HasForeignMenuData {

    private String ssoToken;
    private Guid vmId;
    private String engineHost;
    private boolean admin;

    @Override
    public String getSsoToken() {
        return ssoToken;
    }

    @Override
    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    @Override
    public Guid getVmId() {
        return vmId;
    }

    @Override
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public String getEngineHost() {
        return engineHost;
    }

    @Override
    public void setEngineHost(String host) {
        this.engineHost = host;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
