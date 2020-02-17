package org.ovirt.engine.ui.uicommonweb.models.configure;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel.AdSearchType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PermissionListModel<E> extends SearchableListModel<E, Permission> {

    private UICommand privateAddCommand;

    public UICommand getAddCommand() {
        return privateAddCommand;
    }

    private void setAddCommand(UICommand value) {
        privateAddCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private final Provider<AdElementListModel> adElementListModelProvider;

    @Inject
    public PermissionListModel(Provider<AdElementListModel> adElementListModelProvider) {
        this.adElementListModelProvider = adElementListModelProvider;
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHelpTag(HelpTag.permissions);
        setHashName("permissions"); //$NON-NLS-1$

        setAddCommand(new UICommand("New", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    protected Provider<AdElementListModel> getAdElementListModelProvider() {
        return adElementListModelProvider;
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        VdcObjectType objType = getObjectType();
        GetPermissionsForObjectParameters tempVar = new GetPermissionsForObjectParameters();
        tempVar.setObjectId(getEntityGuid());
        tempVar.setVdcObjectType(objType);
        tempVar.setDirectOnly(false);
        tempVar.setRefresh(getIsQueryFirstTime());
        tempVar.setAllUsersWithPermission(getAllUsersWithPermission());
        super.syncSearch(QueryType.GetPermissionsForObject, tempVar);
    }

    public boolean getAllUsersWithPermission() {
        return false;
    }

    private void add() {
        if (getWindow() != null) {
            return;
        }

        AdElementListModel model = adElementListModelProvider.get();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addPermissionToUserTitle());
        model.setHelpTag(HelpTag.add_permission_to_user);
        model.setHashName("add_permission_to_user"); //$NON-NLS-1$

        model.addCommandOperatingOnSelectedItems(UICommand.createDefaultOkUiCommand("OnAdd", this)); //$NON-NLS-1$
        model.addCancelCommand(this);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePermissionTitle());
        model.setHelpTag(HelpTag.remove_permission);
        model.setHashName("remove_permission"); //$NON-NLS-1$
        model.setItems(getSelectedItems());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        if (getSelectedItems() != null && getSelectedItems().size() > 0) {
            ConfirmationModel model = (ConfirmationModel) getWindow();

            if (model.getProgress() != null) {
                return;
            }

            ArrayList<ActionParametersBase> list = new ArrayList<>();
            for (Object perm : getSelectedItems()) {
                PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters();
                tempVar.setPermission((Permission) perm);
                list.add(tempVar);
            }

            model.startProgress();

            Frontend.getInstance().runMultipleAction(ActionType.RemovePermission, list,
                    result -> {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }, model);
        }

    }

    private void onAdd() {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<DbUser> items = new ArrayList<>();
        if (model.getSearchType() == AdSearchType.EVERYONE) {
            DbUser tempVar = new DbUser();
            tempVar.setId(ApplicationGuids.everyone.asGuid());
            items.add(tempVar);
        } else if (model.getItems() != null) {
            for (Object item : model.getItems()) {
                EntityModel entityModel = (EntityModel) item;
                if (entityModel.getIsSelected()) {
                    items.add((DbUser) entityModel.getEntity());
                }
            }
        }

        if (items.isEmpty()) {
            model.setIsValid(false);
            model.setMessage(ConstantsManager.getInstance().getConstants().selectUserOrGroup());
            return;
        }

        Role role = model.getRole().getSelectedItem();
        // adGroup/user

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (DbUser user : items) {
            Permission perm = new Permission(user.getId(), role.getId(), getEntityGuid(), getObjectType());
            if (user.isGroup()) {
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
            } else {
                PermissionsOperationsParameters tempVar4 = new PermissionsOperationsParameters();
                tempVar4.setPermission(perm);
                tempVar4.setUser(user);
                list.add(tempVar4);
            }
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.AddPermission, list,
            result -> {
                AdElementListModel localModel = (AdElementListModel) result.getState();
                localModel.stopProgress();
                cancel();
            }, model);
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
        if (!getRemoveCommand().getIsExecutionAllowed()) {
            return;
        }
        Guid entityGuid = getEntityGuid();
        for (Object p : getSelectedItems()) {
            if (!entityGuid.equals(((Permission) p).getObjectId())) {
                getRemoveCommand().setIsExecutionAllowed(false);
                return;
            }
        }
    }

    protected Guid getEntityGuid() {
        return AsyncDataProvider.getInstance().getEntityGuid(getEntity());
    }

    protected VdcObjectType getObjectType() {
        if (getEntity() instanceof VM) {
            return VdcObjectType.VM;
        }
        if (getEntity() instanceof StoragePool) {
            return VdcObjectType.StoragePool;
        }
        if (getEntity() instanceof Cluster) {
            return VdcObjectType.Cluster;
        }
        if (getEntity() instanceof VDS) {
            return VdcObjectType.VDS;
        }
        if (getEntity() instanceof StorageDomain) {
            return VdcObjectType.Storage;
        }
        if (getEntity() instanceof VmTemplate) {
            return VdcObjectType.VmTemplate;
        }
        if (getEntity() instanceof VmPool) {
            return VdcObjectType.VmPool;
        }
        if (getEntity() instanceof Quota) {
            return VdcObjectType.Quota;
        }
        if (getEntity() instanceof GlusterVolumeEntity) {
            return VdcObjectType.GlusterVolume;
        }
        if (getEntity() instanceof Disk) {
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
        if (getEntity() instanceof CpuProfile) {
            return VdcObjectType.CpuProfile;
        }
        if (getEntity() instanceof MacPool) {
            return VdcObjectType.MacPool;
        }
        return VdcObjectType.Unknown;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            add();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnAdd".equals(command.getName())) { //$NON-NLS-1$
            onAdd();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "PermissionListModel"; //$NON-NLS-1$
    }
}
