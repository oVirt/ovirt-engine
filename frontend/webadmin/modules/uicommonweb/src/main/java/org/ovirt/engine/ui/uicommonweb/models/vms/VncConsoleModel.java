package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.ShowErrorAsyncQuery;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VncConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, NoVnc }

    private static final DynamicMessages dynamicMessages = (DynamicMessages) TypeResolver.getInstance().resolve(DynamicMessages.class);

    private ClientConsoleMode consoleMode;
    private ConsoleClient vncImpl;

    public VncConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().vncTitle());

        boolean webBasedClientsSupported =
                ((ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class)).webBasedClientsSupported();
        ClientConsoleMode desiredMode =
                myVm.isManaged() ? readDefaultConsoleClientMode(ConfigValues.ClientModeVncDefault)
                        : readDefaultConsoleClientMode(ConfigValues.ClientModeVncDefaultNonManagedVm);
        if (desiredMode == ClientConsoleMode.NoVnc && !webBasedClientsSupported) {
            desiredMode = ClientConsoleMode.Native; // fallback
        }
        setVncImplementation(desiredMode);
    }

    /**
     * Safely determine the default client mode for VNC.
     * @return default VNC client mode read from engine config or 'Native' if there is a problem when retrieving the value.
     */
    private ClientConsoleMode readDefaultConsoleClientMode(ConfigValues key) {
        try {
            return ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(key));
        } catch (Exception e) {
            return ClientConsoleMode.Native;
        }
    }

    public void setVncImplementation(ClientConsoleMode consoleMode) {
        Class implClass = consoleMode == ClientConsoleMode.NoVnc ? INoVnc.class : IVncNative.class;
        this.consoleMode = consoleMode;
        this.vncImpl = (ConsoleClient) TypeResolver.getInstance().resolve(implClass);
    }

    public ConsoleClient getVncImpl() {
        return vncImpl;
    }

    public ClientConsoleMode getClientConsoleMode() {
        return consoleMode;
    }

    @Override
    protected void connect() {
        if (getEntity() == null || getEntity().getRunOnVds() == null) {
            return;
        }
        getLogger().debug("VNC console info..."); //$NON-NLS-1$

        UICommand invokeConsoleCommand = new UICommand("invokeConsoleCommand", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                invokeConsole();
            }
        });

        executeCommandWithConsoleSafenessWarning(invokeConsoleCommand);
    }

    @Override
    public boolean canBeSelected() {
        return getEntity().getGraphicsInfos().containsKey(GraphicsType.VNC);
    }

    private void invokeConsole() {
        final GraphicsInfo vncInfo = getEntity().getGraphicsInfos().get(GraphicsType.VNC);
        if (vncInfo == null) {
            throw new IllegalStateException("Trying to invoke VNC console but VM GraphicsInfo is null."); //$NON-NLS-1$
        }

        vncImpl.getOptions().setVmId(getEntity().getId());
        ConfigureConsoleOptionsParams parameters = new ConfigureConsoleOptionsParams(vncImpl.getOptions(), true);
        parameters.setEngineBaseUrl(FrontendUrlUtils.getRootURL());
        parameters.setConsoleClientResourcesUrl(dynamicMessages.consoleClientResourcesUrl());
        Frontend.getInstance().runQuery(
                QueryType.ConfigureConsoleOptions,
                parameters,
                new ShowErrorAsyncQuery(returnValue -> {
                    ConsoleOptions configuredOptions = ((QueryReturnValue) returnValue).getReturnValue();
                    // overriding global server settings by frontend settings
                    configuredOptions.setRemapCtrlAltDelete(vncImpl.getOptions().isRemapCtrlAltDelete());
                    vncImpl.setOptions(configuredOptions);
                    vncImpl.getOptions().setTitle(getClientTitle());
                    vncImpl.getOptions().setVmName(getEntity().getName());
                    vncImpl.invokeClient();
                }));
    }

}
