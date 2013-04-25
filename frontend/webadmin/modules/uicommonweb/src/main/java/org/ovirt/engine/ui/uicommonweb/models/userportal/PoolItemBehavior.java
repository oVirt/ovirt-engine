package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class PoolItemBehavior extends ItemBehavior
{

    // this has to be static because in every request a new instance of this class is created
    private static Map<Guid, VmOsType> poolToOsType = new HashMap<Guid, VmOsType>();

    public PoolItemBehavior(UserPortalItemModel item)
    {
        super(item);
    }

    @Override
    public void OnEntityChanged()
    {
        UpdateProperties();
        UpdateActionAvailability();
    }

    @Override
    public void EntityPropertyChanged(PropertyChangedEventArgs e)
    {
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        if (command == getItem().getTakeVmCommand())
        {
            TakeVm();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        // Do nothing. There are no events to handle for pools.
    }

    private void TakeVm()
    {
        VmPool entity = (VmPool) getItem().getEntity();

        Frontend.RunAction(VdcActionType.AttachUserToVmFromPoolAndRun, new VmPoolUserParameters(entity.getVmPoolId(),
                Frontend.getLoggedInUser(),
                false),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        if (!result.getReturnValue().getSucceeded()) {
                            return;
                        }

                        PoolItemBehavior behavior = (PoolItemBehavior) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        Guid Guid = (Guid) returnValueBase.getActionReturnValue();
                    }
                }, this);
    }

    private void UpdateProperties()
    {
        VmPool entity = (VmPool) getItem().getEntity();

        getItem().setName(entity.getName());
        getItem().setDescription(entity.getVmPoolDescription());
        getItem().setIsPool(true);
        getItem().setIsServer(false);
        getItem().setStatus(VMStatus.Down);
        getItem().setIsFromPool(false);
        getItem().setPoolType(entity.getVmPoolType());
        if (poolToOsType.containsKey(entity.getVmPoolId())) {
            getItem().setOsType(poolToOsType.get(entity.getVmPoolId()));
        }

        Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                new GetVmdataByPoolIdParameters(entity.getVmPoolId()),
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                PoolItemBehavior behavior = (PoolItemBehavior) target;
                                if (returnValue != null)
                                {
                                    VM vm = (VM) ((VdcQueryReturnValue) returnValue).getReturnValue();
                                    if (vm == null) {
                                        return;
                                    }
                                    UserPortalItemModel model = behavior.getItem();
                                    model.setOsType(vm.getVmOs());
                                    model.setSpiceDriverVersion(vm.getSpiceDriverVersion());
                                    poolToOsType.put(((VmPool) model.getEntity()).getVmPoolId(), vm.getVmOs());
                                }

                            }
                        }));
    }

    private void UpdateActionAvailability()
    {
        getItem().getTakeVmCommand().setIsAvailable(true);

        getItem().getRunCommand().setIsAvailable(false);
        getItem().getPauseCommand().setIsAvailable(true);
        getItem().getShutdownCommand().setIsAvailable(true);
        getItem().getStopCommand().setIsAvailable(true);
        getItem().getReturnVmCommand().setIsAvailable(false);

        getItem().getPauseCommand().setIsExecutionAllowed(false);
        getItem().getShutdownCommand().setIsExecutionAllowed(false);
        getItem().getStopCommand().setIsExecutionAllowed(false);
    }
}
