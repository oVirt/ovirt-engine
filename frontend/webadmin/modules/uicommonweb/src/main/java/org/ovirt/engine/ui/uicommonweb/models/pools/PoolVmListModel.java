package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class PoolVmListModel extends VmListModel
{

    private UICommand privateDetachCommand;

    public UICommand getDetachCommand()
    {
        return privateDetachCommand;
    }

    private void setDetachCommand(UICommand value)
    {
        privateDetachCommand = value;
    }

    @Override
    public VmPool getEntity()
    {
        return (VmPool) super.getEntity();
    }

    public void setEntity(VmPool value)
    {
        super.setEntity(value);
    }

    public PoolVmListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$

        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            setSearchString("Vms: pool=" + getEntity().getVmPoolName()); //$NON-NLS-1$
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("vm_pool_name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }

    public void Detach()
    {
        if (getConfirmWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().detachVirtualMachinesTitle());
        model.setHashName("detach_virtual_machine"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(a.getVmName());
        }
        Collections.sort(list);
        model.setItems(list);

        if (list.size() == getEntity().getAssignedVmsCount()) {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);
            model.setNote(ConstantsManager.getInstance().getConstants().detachAllVmsWarning());
        }

        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSurYouWantToDetachSelectedVirtualMachinesMsg());

        UICommand tempVar = new UICommand("OnDetach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnDetach()
    {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        boolean latchChecked = !model.Validate();

        if (model.getProgress() != null || latchChecked)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM vm = (VM) item;
            list.add(new RemoveVmFromPoolParameters(vm.getId(), true));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmFromPool, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        // var items = SelectedItems != null ? SelectedItems.Cast<VM>().ToList() : new List<VM>();
        ArrayList<VM> items =
                getSelectedItems() != null ? Linq.<VM> Cast(getSelectedItems()) : new ArrayList<VM>();

        // DetachCommand.IsExecutionAllowed = items.Count > 0 && items.All(a => a.status == VMStatus.Down);
        boolean value = true;
        for (VM a : items)
        {
            if (a.getStatus() != VMStatus.Down)
            {
                value = false;
                break;
            }
        }
        getDetachCommand().setIsExecutionAllowed(items.size() > 0 && value);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getDetachCommand())
        {
            Detach();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnDetach")) //$NON-NLS-1$
        {
            OnDetach();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }
}
