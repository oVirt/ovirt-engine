package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
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
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class VncConsoleModel extends ConsoleModel {

    private ClientConsoleMode consoleMode;

    public enum ClientConsoleMode { Native, NoVnc }

    private String host;

    private String otp64 = null;

    private IVnc vncImpl;

    private String getPort() {
        if (getEntity() == null && getEntity().getDisplay() == null) {
            return null;
        }

        return getEntity().getDisplay().toString();
    }

    private String getHost() {
        return host;
    }

    private String getOtp64() {
        return otp64;
    }

    public VncConsoleModel(VM myVm, Model parentModel) {
        super(myVm, parentModel);

        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());

        boolean webSocketProxyDefined = ((ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class)).isWebSocketProxyDefined();
        ClientConsoleMode desiredMode = readDefaultConsoleClientMode();
        if (desiredMode == ClientConsoleMode.NoVnc && !webSocketProxyDefined) {
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

        UICommand setVmTicketCommand = new UICommand("setVmCommand", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                setVmTicket();
            }
        });

        executeCommandWithConsoleSafenessWarning(setVmTicketCommand);
    }

    @Override
    public boolean canBeSelected() {
        DisplayType vmDisplayType = getEntity().getDisplayType() != null
                ? getEntity().getDisplayType()
                : getEntity().getDefaultDisplayType();

        return vmDisplayType == DisplayType.vga;
    }

    private void setVmTicket() {
        Frontend.getInstance().runAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(),
                    null,
                    TICKET_VALIDITY_SECONDS), new IFrontendActionAsyncCallback() {

                @Override
                public void executed(FrontendActionAsyncResult result) {

                    VdcReturnValueBase ticketReturnValue = result.getReturnValue();
                    if (ticketReturnValue != null && ticketReturnValue.getActionReturnValue() != null)
                    {
                        otp64 = (String) ticketReturnValue.getActionReturnValue();
                        // Determine the display IP.
                        if (StringHelper.isNullOrEmpty(getEntity().getDisplayIp())
                                || "0".equals(getEntity().getDisplayIp())) //$NON-NLS-1$
                        {
                            AsyncQuery _asyncQuery = new AsyncQuery();
                            _asyncQuery.setModel(this);
                            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                                @Override
                                public void onSuccess(Object model, Object ReturnValue)
                                {
                                    VncConsoleModel consoleModel = (VncConsoleModel) model;
                                    VncConsoleModel.this.host = (String) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                    consoleModel.setAndInvokeClient();
                                }
                            };

                            Frontend.getInstance().runQuery(VdcQueryType.GetManagementInterfaceAddressByVmId,
                                    new IdQueryParameters(getEntity().getId()), _asyncQuery);
                        }
                        else {
                            VncConsoleModel.this.host = getEntity().getDisplayIp();
                            setAndInvokeClient();
                        }
                    }
                }
            });
    }

    private void setAndInvokeClient() {
        vncImpl.setVncHost(getHost());
        vncImpl.setVncPort(getPort());
        vncImpl.setTicket(getOtp64());
        vncImpl.setTitle(getClientTitle());
        vncImpl.setToggleFullscreenHotKey(getToggleFullScreenKeys());
        vncImpl.setReleaseCursorHotKey(getReleaseCursorKeys());
        vncImpl.setTicketValiditySeconds(TICKET_VALIDITY_SECONDS);

        vncImpl.invokeClient();
    }

}
