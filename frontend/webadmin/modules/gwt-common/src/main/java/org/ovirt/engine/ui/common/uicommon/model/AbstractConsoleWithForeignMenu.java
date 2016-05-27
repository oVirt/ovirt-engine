package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.common.console.ConsoleOptions;
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

    protected void writeOVirtSection(StringBuilder configBuilder, ConsoleOptions options) {
        configBuilder.append("\n[ovirt]") //$NON-NLS-1$
                .append("\nhost=").append(engineHost) //$NON-NLS-1$
                .append("\nvm-guid=").append(vmId.toString()) //$NON-NLS-1$
                .append("\nsso-token=").append(ssoToken) //$NON-NLS-1$
                .append("\nadmin=").append(admin ? 1 : 0); //$NON-NLS-1$

        if (options.getTrustStore() != null && !options.isCustomHttpsCertificateUsed()) {
            String trustStore= options.getTrustStore().replace("\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
            configBuilder.append("\nca=").append(trustStore); //$NON-NLS-1$
        }

    }

}
