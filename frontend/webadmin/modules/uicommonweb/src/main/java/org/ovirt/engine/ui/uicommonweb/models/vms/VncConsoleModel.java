package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Iterator;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class VncConsoleModel extends ConsoleModel
{
    private final IVnc vnc;
    String otp64 = null;
    private Model model;
    private static final int seconds = 120;

    public VncConsoleModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());

        vnc = (IVnc) TypeResolver.getInstance().Resolve(IVnc.class);
    }

    @Override
    protected void Connect()
    {
        if (getEntity() != null)
        {
            getLogger().Debug("VNC console info..."); //$NON-NLS-1$

            // Don't connect if there VM is not running on any host.
            if (getEntity().getrun_on_vds() == null)
            {
                return;
            }
            Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getId(),
                    null,
                    seconds), new IFrontendActionAsyncCallback() {

                @Override
                public void Executed(FrontendActionAsyncResult result) {

                    VdcReturnValueBase ticketReturnValue = result.getReturnValue();
                    if (ticketReturnValue != null && ticketReturnValue.getActionReturnValue() != null)
                    {
                        otp64 = (String) ticketReturnValue.getActionReturnValue();
                        // Determine the display IP.
                        if (StringHelper.isNullOrEmpty(getEntity().getdisplay_ip())
                                || StringHelper.stringsEqual(getEntity().getdisplay_ip(), "0")) //$NON-NLS-1$
                        {
                            AsyncQuery _asyncQuery = new AsyncQuery();
                            _asyncQuery.setModel(this);
                            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                                @Override
                                public void OnSuccess(Object model, Object ReturnValue)
                                {
                                    VncConsoleModel consoleModel = (VncConsoleModel) model;
                                    Iterable networkInterfaces =
                                            (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                                    Iterator networkInterfacesIterator = networkInterfaces.iterator();
                                    while (networkInterfacesIterator.hasNext())
                                    {
                                        VdsNetworkInterface currentNetworkInterface =
                                                (VdsNetworkInterface) networkInterfacesIterator.next();
                                        if (currentNetworkInterface == null)
                                        {
                                            continue;
                                        }
                                        if (currentNetworkInterface.getIsManagement())
                                        {
                                            consoleModel.postGetHost(currentNetworkInterface.getAddress());
                                            return;
                                        }
                                    }
                                }
                            };

                            Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId,
                                    new GetVdsByVdsIdParameters(getEntity().getrun_on_vds().getValue()),
                                    _asyncQuery);
                        }
                        else {
                            postGetHost(getEntity().getdisplay_ip());
                        }
                    }
                }
            });
        }
    }

    protected void postGetHost(String hostName) {
        VncInfoModel infoModel = new VncInfoModel();

        infoModel.setTitle("VNC - " + getEntity().getvm_name()); //$NON-NLS-1$
        infoModel.getVncMessage().setEntity(ConstantsManager.getInstance()
                .getMessages()
                .vncInfoMessage(hostName,
                        (getEntity().getdisplay() == null ? 0 : getEntity().getdisplay()),
                        otp64,
                        seconds));
        infoModel.setCloseCommand(new UICommand("closeVncInfo", model)); //$NON-NLS-1$
        infoModel.getCloseCommand().setTitle(ConstantsManager.getInstance().getConstants().close());
        infoModel.getCommands().add(infoModel.getCloseCommand());
        model.setWindow(infoModel);
    }

    @Override
    protected void UpdateActionAvailability()
    {
        super.UpdateActionAvailability();

        getConnectCommand().setIsExecutionAllowed(getConfigurator().IsDisplayTypeSupported(DisplayType.vnc)
                && getEntity() != null
                && getEntity().getdisplay_type() == DisplayType.vnc
                && (getEntity().getstatus() == VMStatus.PoweringUp || getEntity().getstatus() == VMStatus.Up
                        || getEntity().getstatus() == VMStatus.RebootInProgress
                        || getEntity().getstatus() == VMStatus.PoweringDown || getEntity().getstatus() == VMStatus.Paused));
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
