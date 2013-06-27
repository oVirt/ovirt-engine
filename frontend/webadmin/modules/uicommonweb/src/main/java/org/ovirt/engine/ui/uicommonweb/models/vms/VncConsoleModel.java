package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class VncConsoleModel extends ConsoleModel {

    private ClientConsoleMode consoleMode;

    public enum ClientConsoleMode { Native, NoVnc }

    private static final int TICKET_VALIDITY_SECONDS = 120;

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

    public VncConsoleModel() {
        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());
        setVncImplementation(ClientConsoleMode.Native); //Native (MIME) way is the default
    }

    public void setVncImplementation(ClientConsoleMode consoleMode) {
        Class implClass = consoleMode == ClientConsoleMode.NoVnc ? INoVnc.class : IVncNative.class;
        this.consoleMode = consoleMode;
        this.vncImpl = (IVnc) TypeResolver.getInstance().resolve(implClass);
    }

    public ClientConsoleMode getClientConsoleMode() {
        return consoleMode;
    }

    @Override
    protected void connect()
    {
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

    private void setVmTicket() {
        Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(),
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
                                || StringHelper.stringsEqual(getEntity().getDisplayIp(), "0")) //$NON-NLS-1$
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

                            Frontend.RunQuery(VdcQueryType.GetManagementInterfaceAddressByVmId,
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
        vncImpl.setTitle(getTitle());

        vncImpl.invokeClient();
    }

    @Override
    protected void updateActionAvailability()
    {
        super.updateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getEntity() != null
                && getEntity().getDisplayType() == DisplayType.vnc
                && (getEntity().getStatus() == VMStatus.PoweringUp || getEntity().getStatus() == VMStatus.Up
                        || getEntity().getStatus() == VMStatus.RebootInProgress
                        || getEntity().getStatus() == VMStatus.PoweringDown || getEntity().getStatus() == VMStatus.Paused));
    }
}
