package org.ovirt.engine.ui.uicommonweb.models.configure;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class PermissionListModel extends SearchableListModel
{

    private UICommand privateAddCommand;

    public UICommand getAddCommand()
    {
        return privateAddCommand;
    }

    private void setAddCommand(UICommand value)
    {
        privateAddCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    @Override
    public Object getEntity()
    {
        return super.getEntity();
    }

    @Override
    public void setEntity(Object value)
    {
        super.setEntity(value);
    }

    public PermissionListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHashName("permissions"); //$NON-NLS-1$

        setAddCommand(new UICommand("New", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        getSearchCommand().Execute();
        UpdateActionAvailability();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        VdcObjectType objType = getObjectType();
        boolean directOnly = (objType == VdcObjectType.VM ? true : false);
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(objType);
        tempVar.setDirectOnly(directOnly);
        tempVar.setRefresh(getIsQueryFirstTime());
        super.SyncSearch(VdcQueryType.GetPermissionsForObject, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VdcObjectType objType = getObjectType();
        boolean directOnly = (objType == VdcObjectType.VM ? true : false);

        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(objType);
        tempVar.setDirectOnly(directOnly);
        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetPermissionsForObject, tempVar));

        setItems(getAsyncResult().getData());
    }

    private void add()
    {
        if (getWindow() != null)
        {
            return;
        }

        AdElementListModel model = createAdElementListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addPermissionToUserTitle());
        model.setHashName("add_permission_to_user"); //$NON-NLS-1$

        UICommand tempVar = new UICommand("OnAdd", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    protected AdElementListModel createAdElementListModel() {
        return new AdElementListModel();
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePermissionTitle());
        model.setHashName("remove_permission"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().permissionMsg());
        ArrayList<String> items = new ArrayList<String>();
        for (Object a : getSelectedItems())
        {
            items.add("Role " + ((permissions) a).getRoleName() + " on User " + ((permissions) a).getOwnerName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        if (getSelectedItems() != null && getSelectedItems().size() > 0)
        {
            ConfirmationModel model = (ConfirmationModel) getWindow();

            if (model.getProgress() != null)
            {
                return;
            }

            ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
            for (Object perm : getSelectedItems())
            {
                PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes();
                tempVar.setPermission((permissions) perm);
                list.add(tempVar);
            }

            model.StartProgress(null);

            Frontend.RunMultipleAction(VdcActionType.RemovePermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.StopProgress();
                            Cancel();

                        }
                    }, model);
        }

    }

    private void OnAdd()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.getIsEveryoneSelected() && model.getSelectedItems() == null)
        {
            Cancel();
            return;
        }

        ArrayList<DbUser> items = new ArrayList<DbUser>();
        if (model.getIsEveryoneSelected())
        {
            DbUser tempVar = new DbUser();
            tempVar.setuser_id(ApplicationGuids.everyone.asGuid());
            items.add(tempVar);
        }
        else
        {
            for (Object item : model.getItems())
            {
                EntityModel entityModel = (EntityModel) item;
                if (entityModel.getIsSelected())
                {
                    items.add((DbUser) entityModel.getEntity());
                }
            }
        }

        Role role = (Role) model.getRole().getSelectedItem();
        // adGroup/user

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (DbUser user : items)
        {
            permissions tempVar2 = new permissions();
            tempVar2.setad_element_id(user.getuser_id());
            tempVar2.setrole_id(role.getId());
            permissions perm = tempVar2;
            perm.setObjectId(getEntityGuid());
            perm.setObjectType(this.getObjectType());

            if (user.getIsGroup())
            {
                PermissionsOperationsParametes tempVar3 = new PermissionsOperationsParametes();
                tempVar3.setPermission(perm);
                tempVar3.setAdGroup(new ad_groups(user.getuser_id(), user.getname(), user.getdomain()));
                list.add(tempVar3);
            }
            else
            {
                PermissionsOperationsParametes tempVar4 = new PermissionsOperationsParametes();
                tempVar4.setPermission(perm);
                tempVar4.setVdcUser(new VdcUser(user.getuser_id(), user.getusername(), user.getdomain()));
                list.add(tempVar4);
            }
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AddPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void Cancel()
    {
        setWindow(null);
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

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        getRemoveCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() > 0));
        if (getRemoveCommand().getIsExecutionAllowed() == false)
        {
            return;
        }
        Guid entityGuid = getEntityGuid();
        for (Object p : getSelectedItems())
        {
            if (!entityGuid.equals(((permissions) p).getObjectId()))
            {
                getRemoveCommand().setIsExecutionAllowed(false);
                return;
            }
        }
    }

    protected Guid getEntityGuid()
    {
        return DataProvider.GetEntityGuid(getEntity());
    }

    protected VdcObjectType getObjectType()
    {
        if (getEntity() instanceof VM)
        {
            return VdcObjectType.VM;
        }
        if (getEntity() instanceof storage_pool)
        {
            return VdcObjectType.StoragePool;
        }
        if (getEntity() instanceof VDSGroup)
        {
            return VdcObjectType.VdsGroups;
        }
        if (getEntity() instanceof VDS)
        {
            return VdcObjectType.VDS;
        }
        if (getEntity() instanceof storage_domains)
        {
            return VdcObjectType.Storage;
        }
        if (getEntity() instanceof VmTemplate)
        {
            return VdcObjectType.VmTemplate;
        }
        if (getEntity() instanceof vm_pools)
        {
            return VdcObjectType.VmPool;
        }
        if (getEntity() instanceof Quota)
        {
            return VdcObjectType.Quota;
        }
        if (getEntity() instanceof GlusterVolumeEntity) {
            return VdcObjectType.GlusterVolume;
        }
        if (getEntity() instanceof DiskImage) {
            return VdcObjectType.Disk;
        }
        if (getEntity() instanceof Network) {
            return VdcObjectType.Network;
        }
        return VdcObjectType.Unknown;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAdd")) //$NON-NLS-1$
        {
            OnAdd();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "PermissionListModel"; //$NON-NLS-1$
    }
}
