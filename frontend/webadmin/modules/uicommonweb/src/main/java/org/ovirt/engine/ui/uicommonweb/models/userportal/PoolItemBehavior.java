package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class PoolItemBehavior extends ItemBehavior
{
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
        vm_pools entity = (vm_pools) getItem().getEntity();

        Frontend.RunAction(VdcActionType.AttachUserToVmFromPoolAndRun, new VmPoolUserParameters(entity.getvm_pool_id(),
                Frontend.getLoggedInUser(),
                false),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        PoolItemBehavior behavior = (PoolItemBehavior) result.getState();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        Guid Guid = (Guid) returnValueBase.getActionReturnValue();
                        behavior.PostTakeVm(Guid);

                    }
                }, this);
    }

    public void PostTakeVm(Guid vmId)
    {
        AsyncDataProvider.GetVmById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        PoolItemBehavior behavior = (PoolItemBehavior) target;
                        UserPortalItemModel model = behavior.getItem();
                        model.setEntity(returnValue);

                    }
                }), vmId);
    }

    private void UpdateProperties()
    {
        vm_pools entity = (vm_pools) getItem().getEntity();

        getItem().setName(entity.getvm_pool_name());
        getItem().setDescription(entity.getvm_pool_description());
        getItem().setIsPool(true);
        getItem().setIsServer(false);
        getItem().setStatus(VMStatus.Down);
        getItem().setIsFromPool(false);
        getItem().setPoolType(entity.getvm_pool_type());

        AsyncDataProvider.GetAnyVm(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        PoolItemBehavior behavior = (PoolItemBehavior) target;
                        VM vm = (VM) returnValue;
                        if (vm != null)
                        {
                            UserPortalItemModel model = behavior.getItem();
                            model.setOsType(vm.getvm_os());
                        }

                    }
                }), entity.getvm_pool_name());
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
