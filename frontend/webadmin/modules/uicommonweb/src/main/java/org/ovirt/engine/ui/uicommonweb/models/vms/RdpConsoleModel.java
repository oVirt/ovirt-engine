package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RdpConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, Plugin, Auto }

    private IRdp privaterdp;
    private ClientConsoleMode clientConsoleMode;

    private final ConsoleUtils consoleUtils;

    public IRdp getrdp() {
        return privaterdp;
    }

    public ClientConsoleMode getClientConsoleMode() {
        return clientConsoleMode;
    }

    public void setRdpImplementation(ClientConsoleMode consoleMode) {
        this.clientConsoleMode = consoleMode;

        switch (consoleMode) {
            case Native:
                setrdp((IRdp) TypeResolver.getInstance().resolve(IRdpNative.class));
                break;
            case Plugin:
                setrdp((IRdp) TypeResolver.getInstance().resolve(IRdpPlugin.class));
                break;
            default:
                IRdpPlugin impl = (IRdpPlugin) TypeResolver.getInstance().resolve(IRdpPlugin.class);
                setrdp(consoleUtils.isBrowserPluginSupported(ConsoleProtocol.RDP) ? impl
                        : (IRdp) TypeResolver.getInstance().resolve(IRdpNative.class));
                break;
        }
    }

    private void setrdp(IRdp value) {
        privaterdp = value;
    }

    public RdpConsoleModel() {
        setTitle(ConstantsManager.getInstance().getConstants().RDPTitle());
        this.consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
        setRdpImplementation(
                ClientConsoleMode.valueOf((String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ClientModeRdpDefault)));
    }

    @Override
    protected void connect() {
        if (getEntity() != null) {
            getLogger().debug("Connecting to RDP console..."); //$NON-NLS-1$

            getrdp().setAddress(getEntity().getVmHost().split("[ ]", -1)[0]); //$NON-NLS-1$
            getrdp().setGuestID(getEntity().getId().toString());

            // Try to connect.
            try {
                getrdp().connect();
                updateActionAvailability();
            } catch (RuntimeException ex) {
                getLogger().error("Exception on RDP connect", ex); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void updateActionAvailability() {
        super.updateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getEntity() != null
                && (getEntity().getStatus() == VMStatus.Up || getEntity().getStatus() == VMStatus.PoweringDown)
                && AsyncDataProvider.isWindowsOsType(getEntity().getVmOsId()));
    }

    public void raiseErrorEvent(ErrorCodeEventArgs e) {
        getErrorEvent().raise(this, e);
    }
}
