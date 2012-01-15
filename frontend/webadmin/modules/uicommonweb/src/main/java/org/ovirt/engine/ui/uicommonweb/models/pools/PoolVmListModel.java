package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.Collections;

import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
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
    public vm_pools getEntity()
    {
        return (vm_pools) super.getEntity();
    }

    public void setEntity(vm_pools value)
    {
        super.setEntity(value);
    }

    public PoolVmListModel()
    {
        setTitle("Virtual Machines");

        setDetachCommand(new UICommand("Detach", this));

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
            setSearchString(StringFormat.format("Vms: pool=%1$s", getEntity().getvm_pool_name()));
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("vm_pool_name"))
        {
            getSearchCommand().Execute();
        }
    }

    public void Detach()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Detach Virtual Machine(s)");
        model.setHashName("detach_virtual_machine");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(a.getvm_name());
        }
        Collections.sort(list);
        model.setItems(list);

        model.setMessage("Are you sure you want to detach selected Virtual Machine(s)?");

        UICommand tempVar = new UICommand("OnDetach", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnDetach()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM vm = (VM) item;
            list.add(new RemoveVmFromPoolParameters(vm.getvm_guid()));
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

        // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java
        // 'if-else' logic:
        // switch (e.PropertyName)
        // ORIGINAL LINE: case "status":
        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        // var items = SelectedItems != null ? SelectedItems.Cast<VM>().ToList() : new List<VM>();
        java.util.ArrayList<VM> items =
                getSelectedItems() != null ? Linq.<VM> Cast(getSelectedItems()) : new java.util.ArrayList<VM>();

        // DetachCommand.IsExecutionAllowed = items.Count > 0 && items.All(a => a.status == VMStatus.Down);
        boolean value = true;
        for (VM a : items)
        {
            if (a.getstatus() != VMStatus.Down)
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
        if (StringHelper.stringsEqual(command.getName(), "OnDetach"))
        {
            OnDetach();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }
}
