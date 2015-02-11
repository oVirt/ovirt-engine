package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VncConsoleModel extends ConsoleModel {

    public enum ClientConsoleMode { Native, NoVnc }

    private ClientConsoleMode consoleMode;
    private IVnc vncImpl;

    public VncConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());

        boolean webBasedClientsSupported =
                ((ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class)).webBasedClientsSupported();
        ClientConsoleMode desiredMode = readDefaultConsoleClientMode();
        if (desiredMode == ClientConsoleMode.NoVnc && !webBasedClientsSupported) {
            desiredMode = ClientConsoleMode.Native; // fallback
        }
        setVncImplementation(desiredMode);
    }

    /**
     * Safely determine the default client mode for VNC.
     * @return default VNC client mode read from engine config or 'Native' if there is a problem when retrieving the value.
     */
    private ClientConsoleMode readDefaultConsoleClientMode() {
        try {
            return ClientConsoleMode.valueOf((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.ClientModeVncDefault));
        } catch (Exception e) {
            return ClientConsoleMode.Native;
        }
    }

    public void setVncImplementation(ClientConsoleMode consoleMode) {
        Class implClass = consoleMode == ClientConsoleMode.NoVnc ? INoVnc.class : IVncNative.class;
        this.consoleMode = consoleMode;
        this.vncImpl = (IVnc) TypeResolver.getInstance().resolve(implClass);
    }

    public IVnc getVncImpl() {
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

        final AsyncQuery configureCallback = new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                vncImpl.setOptions((ConsoleOptions) ((VdcQueryReturnValue) returnValue).getReturnValue());
                vncImpl.getOptions().setTitle(getClientTitle());
                vncImpl.invokeClient();
            }
        });

        vncImpl.getOptions().setVmId(getEntity().getId());
        Frontend.getInstance().runQuery(
                VdcQueryType.ConfigureConsoleOptions,
                new ConfigureConsoleOptionsParams(vncImpl.getOptions(), true),
                configureCallback);
    }

}
