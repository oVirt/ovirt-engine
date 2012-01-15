package org.ovirt.engine.ui.uicommonweb.models.users;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmToAdElementParameters;
import org.ovirt.engine.core.common.action.VmToAdGroupParameters;
import org.ovirt.engine.core.common.action.VmToUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

@SuppressWarnings("unused")
public class UserVmListModel extends VmListModel
{

    private UICommand privateAttachCommand;

    public UICommand getAttachCommand()
    {
        return privateAttachCommand;
    }

    private void setAttachCommand(UICommand value)
    {
        privateAttachCommand = value;
    }

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
    public DbUser getEntity()
    {
        return (DbUser) ((super.getEntity() instanceof DbUser) ? super.getEntity() : null);
    }

    public void setEntity(DbUser value)
    {
        super.setEntity(value);
    }

    public UserVmListModel()
    {
        setTitle("Virtual Machines");

        setAttachCommand(new UICommand("Attach", this));
        setDetachCommand(new UICommand("Detach", this));

        UpdateActionAvailability();
    }

    private void Attach()
    {
        if (getWindow() != null)
        {
            return;
        }

        FindDesktopModel model = new FindDesktopModel();
        setWindow(model);
        model.setTitle("Add Desktop(s) to User/AD Group");
        model.setHashName("add_desktop_to_user_ad_group");
        model.setExcludeItems(getItems());

        UICommand tempVar = new UICommand("OnAttach", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnAttach()
    {
        FindDesktopModel model = (FindDesktopModel) getWindow();
        if (model.getSelectedItems() == null)
        {
            Cancel();
            return;
        }

        // var items = model.Items.Cast<EntityModel>()
        // .Where(Selector.GetIsSelected)
        // .Select(a => (VM)a.Entity)
        // .ToList();
        java.util.ArrayList<VM> items = new java.util.ArrayList<VM>();
        for (Object item : model.getItems())
        {
            EntityModel a = (EntityModel) item;
            if (a.getIsSelected())
            {
                items.add((VM) a.getEntity());
            }
        }

        java.util.ArrayList<VdcActionParametersBase> prms = new java.util.ArrayList<VdcActionParametersBase>();

        if (getEntity().getIsGroup())
        {
            for (VM a : items)
            {
                prms.add(new VmToAdGroupParameters(a.getvm_guid(), new ad_groups(getEntity().getuser_id(),
                        getEntity().getname(),
                        getEntity().getdomain())));
            }
            // TODO: Remove Model! VmUserListModel should be removed!
            // Frontend.RunMultipleActions(VdcActionType.AttachVmToAdGroup, prms);
        }
        else
        {
            for (VM a : items)
            {
                prms.add(new VmToUserParameters(new VdcUser(getEntity().getuser_id(),
                        getEntity().getusername(),
                        getEntity().getdomain()), a.getvm_guid()));
            }
            // Attach vm to users
            // TODO: Remove Model! VmUserListModel should be removed!
            // Frontend.RunMultipleActions(VdcActionType.AttachVmToUser, prms);
        }

        // if (Entity.IsGroup)
        // {
        // Frontend.RunMultipleActions(VdcActionType.AttachVmToAdGroup,
        // items
        // .Select(a => (VdcActionParametersBase)new VmToAdGroupParameters(a.vm_guid,
        // new ad_groups(Entity.user_id, Entity.name, Entity.domain)))
        // .ToList()
        // );
        // }

        // else
        // {
        // Frontend.RunMultipleActions(VdcActionType.AttachVmToUser,
        // items
        // .Select(a => (VdcActionParametersBase)new VmToUserParameters(
        // new VdcUser(Entity.user_id, Entity.username, Entity.domain), a.vm_guid))
        // .ToList()
        // );
        // }

        Cancel();
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
        model.setMessage("Are you sure you want to Detach from the user the following Virtual Machine(s)");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(a.getvm_name());
        }
        model.setItems(list);

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
        java.util.ArrayList<VM> items = Linq.<VM> Cast(getSelectedItems());

        java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
        //
        for (VM a : items)
        {
            parameters.add(new VmToAdElementParameters(getEntity().getuser_id(), a.getvm_guid()));
        }

        // var parameters = items
        // .Select(a => (VdcActionParametersBase)new VmToAdElementParameters(Entity.user_id, a.vm_guid))
        // .ToList();

        // TODO: Remove Model! VmUserListModel should be removed!

        // if (Entity.IsGroup)
        // {
        // Frontend.RunMultipleActions(VdcActionType.DetachVmFromAdGroup, parameters);
        // }
        // else
        // {
        // Frontend.RunMultipleActions(VdcActionType.DetachVmFromUser, parameters);
        // }

        Cancel();
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
            setSearchString(StringFormat.format("VMs: users.name=%1$s", getEntity().getname()));
            super.Search();
        }
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
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name"))
        {
            getSearchCommand().Execute();
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        java.util.ArrayList items =
                getSelectedItems() != null && getSelectedItem() != null ? (java.util.ArrayList) getSelectedItems()
                        : new java.util.ArrayList();

        getDetachCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, DbUser.class, VdcActionType.RemoveUser));
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAttachCommand())
        {
            Attach();
        }
        if (command == getDetachCommand())
        {
            Detach();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAttach"))
        {
            OnAttach();
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
