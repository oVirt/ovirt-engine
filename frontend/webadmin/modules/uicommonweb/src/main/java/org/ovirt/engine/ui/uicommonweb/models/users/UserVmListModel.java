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
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$

        setAttachCommand(new UICommand("Attach", this)); //$NON-NLS-1$
        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$

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
        model.setTitle(ConstantsManager.getInstance().getConstants().addDesktopsToUserADGroupTitle());
        model.setHashName("add_desktop_to_user_ad_group"); //$NON-NLS-1$
        model.setExcludeItems(getItems());

        UICommand tempVar = new UICommand("OnAttach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
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
                prms.add(new VmToAdGroupParameters(a.getId(), new ad_groups(getEntity().getuser_id(),
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
                        getEntity().getdomain()), a.getId()));
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
        model.setTitle(ConstantsManager.getInstance().getConstants().detachVirtualMachinesTitle());
        model.setHashName("detach_virtual_machine"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDetachFromUserFollowingVmsMsg());

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(a.getvm_name());
        }
        model.setItems(list);

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
        java.util.ArrayList<VM> items = Linq.<VM> Cast(getSelectedItems());

        java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
        //
        for (VM a : items)
        {
            parameters.add(new VmToAdElementParameters(getEntity().getuser_id(), a.getId()));
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
            setSearchString(StringFormat.format("VMs: users.name=%1$s", getEntity().getname())); //$NON-NLS-1$
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

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
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
        if (StringHelper.stringsEqual(command.getName(), "OnAttach")) //$NON-NLS-1$
        {
            OnAttach();
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
