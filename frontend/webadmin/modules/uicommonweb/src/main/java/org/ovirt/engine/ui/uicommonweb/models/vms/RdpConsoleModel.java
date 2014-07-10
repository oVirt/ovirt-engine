package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RdpConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, Plugin, Auto }

    private IRdp privaterdp;
    private ClientConsoleMode clientConsoleMode;
    private boolean useFQDNIfAvailable;

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
                IRdpPlugin plugin = (IRdpPlugin) TypeResolver.getInstance().resolve(IRdpPlugin.class);
                plugin.setParentModel(this);
                setrdp(plugin);
                break;
            default:
                IRdpPlugin impl = (IRdpPlugin) TypeResolver.getInstance().resolve(IRdpPlugin.class);
                impl.setParentModel(this);
                setrdp(consoleUtils.isBrowserPluginSupported(ConsoleProtocol.RDP) ? impl
                        : (IRdp) TypeResolver.getInstance().resolve(IRdpNative.class));
                break;
        }
    }

    private void setUseFqdnIfAvailable(boolean value) {
        useFQDNIfAvailable = value;
    }

    private boolean getUseFqdnIfAvailable() {
        return useFQDNIfAvailable;
    }

    private void setrdp(IRdp value) {
        privaterdp = value;
    }

    public RdpConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().RDPTitle());
        this.consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);
        setRdpImplementation(
                ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClientModeRdpDefault)));
        setUseFqdnIfAvailable((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.UseFqdnForRdpIfAvailable));
    }

    @Override
    protected void connect() {
        if (getEntity() != null) {
            getLogger().debug("Connecting to RDP console..."); //$NON-NLS-1$

            boolean haveFqdn = false;
            if (getUseFqdnIfAvailable()) {
                getLogger().debug("RDP connection is using FQDN if available"); //$NON-NLS-1$
                if (!StringHelper.isNullOrEmpty(getEntity().getVmFQDN())) {
                    getLogger().debug("RDP connection is using FQDN because it is available"); //$NON-NLS-1$
                    haveFqdn = true;
                    getrdp().setAddress(getEntity().getVmFQDN());
                }
            }

            if (!haveFqdn) {
                getLogger().debug("RDP connection is not using FQDN"); //$NON-NLS-1$
                getrdp().setAddress(getEntity().getVmHost().split("[ ]", -1)[0]); //$NON-NLS-1$
            }

            getrdp().setGuestID(getEntity().getId().toString());

            // Try to connect.
            try {
                getrdp().connect();
            } catch (RuntimeException ex) {
                getLogger().error("Exception on RDP connect", ex); //$NON-NLS-1$
            }
        }
    }

    @Override
    public boolean canBeSelected() {
        return AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getOs());
    }

    @Override
    public boolean canConnect() {
        return (getEntity().getStatus() == VMStatus.Up || getEntity().getStatus() == VMStatus.PoweringDown)
                && AsyncDataProvider.getInstance().isWindowsOsType(getEntity().getVmOsId());
    }

    public void raiseErrorEvent(ErrorCodeEventArgs e) {
        getErrorEvent().raise(this, e);
    }
}
