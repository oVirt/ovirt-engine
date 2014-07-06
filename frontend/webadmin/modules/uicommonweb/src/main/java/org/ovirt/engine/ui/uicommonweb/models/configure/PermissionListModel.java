package org.ovirt.engine.ui.uicommonweb.models.configure;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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

    public PermissionListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHelpTag(HelpTag.permissions);
        setHashName("permissions"); //$NON-NLS-1$

        setAddCommand(new UICommand("New", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        VdcObjectType objType = getObjectType();
        boolean directOnly = (objType == VdcObjectType.VM ? true : false);
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(objType);
        tempVar.setDirectOnly(directOnly);
        tempVar.setRefresh(getIsQueryFirstTime());
        tempVar.setAllUsersWithPermission(getAllUsersWithPermission());
        super.syncSearch(VdcQueryType.GetPermissionsForObject, tempVar);
    }

    public boolean getAllUsersWithPermission() {
        return false;
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
        model.setHelpTag(HelpTag.add_permission_to_user);
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
        model.setHelpTag(HelpTag.remove_permission);
        model.setHashName("remove_permission"); //$NON-NLS-1$
        model.setItems(getSelectedItems());

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void onRemove()
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
                PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters();
                tempVar.setPermission((Permissions) perm);
                list.add(tempVar);
            }

            model.startProgress(null);

            Frontend.getInstance().runMultipleAction(VdcActionType.RemovePermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }

    }

    private void onAdd()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.getIsEveryoneSelected() && model.getSelectedItems() == null)
        {
            cancel();
            return;
        }

        ArrayList<DbUser> items = new ArrayList<DbUser>();
        if (model.getIsEveryoneSelected())
        {
            DbUser tempVar = new DbUser();
            tempVar.setId(ApplicationGuids.everyone.asGuid());
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
            Permissions tempVar2 = new Permissions();
            tempVar2.setad_element_id(user.getId());
            tempVar2.setrole_id(role.getId());
            Permissions perm = tempVar2;
            perm.setObjectId(getEntityGuid());
            perm.setObjectType(this.getObjectType());

            if (user.isGroup())
            {
                DbGroup group = new DbGroup();
                group.setId(user.getId());
                group.setExternalId(user.getExternalId());
                group.setName(user.getFirstName());
                group.setDomain(user.getDomain());
                group.setNamespace(user.getNamespace());
                PermissionsOperationsParameters tempVar3 = new PermissionsOperationsParameters();
                tempVar3.setPermission(perm);
                tempVar3.setGroup(group);
                list.add(tempVar3);
            }
            else
            {
                PermissionsOperationsParameters tempVar4 = new PermissionsOperationsParameters();
                tempVar4.setPermission(perm);
                tempVar4.setUser(user);
                list.add(tempVar4);
            }
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.AddPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    private void updateActionAvailability()
    {
        getRemoveCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() > 0));
        if (getRemoveCommand().getIsExecutionAllowed() == false)
        {
            return;
        }
        Guid entityGuid = getEntityGuid();
        for (Object p : getSelectedItems())
        {
            if (!entityGuid.equals(((Permissions) p).getObjectId()))
            {
                getRemoveCommand().setIsExecutionAllowed(false);
                return;
            }
        }
    }

    protected Guid getEntityGuid()
    {
        return AsyncDataProvider.getInstance().getEntityGuid(getEntity());
    }

    protected VdcObjectType getObjectType()
    {
        if (getEntity() instanceof VM)
        {
            return VdcObjectType.VM;
        }
        if (getEntity() instanceof StoragePool)
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
        if (getEntity() instanceof StorageDomain)
        {
            return VdcObjectType.Storage;
        }
        if (getEntity() instanceof VmTemplate)
        {
            return VdcObjectType.VmTemplate;
        }
        if (getEntity() instanceof VmPool)
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
        if (getEntity() instanceof VnicProfile){
            return VdcObjectType.VnicProfile;
        }
        if (getEntity() instanceof DiskProfile) {
            return VdcObjectType.DiskProfile;
        }
        return VdcObjectType.Unknown;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if ("OnAdd".equals(command.getName())) //$NON-NLS-1$
        {
            onAdd();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "PermissionListModel"; //$NON-NLS-1$
    }
}
