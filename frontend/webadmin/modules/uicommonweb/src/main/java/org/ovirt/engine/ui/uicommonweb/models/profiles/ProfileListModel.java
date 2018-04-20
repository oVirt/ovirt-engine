package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public abstract class ProfileListModel<P extends ProfileBase, Q extends QosBase, R extends BusinessEntity<Guid>> extends ListWithSimpleDetailsModel<R, P> {
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private Map<Guid, Q> qosMap;
    final PermissionListModel<P> permissionListModel;

    @Inject
    public ProfileListModel(final PermissionListModel<P> permissionListModel) {
        this.permissionListModel = permissionListModel;
        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().diskProfilesTitle());
        setHelpTag(HelpTag.disk_profiles);
        setHashName("disk_profiles"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    private void setDetailList() {
        List<HasEntity<P>> list = new ArrayList<>();
        list.add(permissionListModel);
        setDetailModels(list);
    }

    protected abstract ProfileBaseModel<P, Q, R> getNewProfileModel();

    protected abstract ProfileBaseModel<P, Q, R> getEditProfileModel();

    protected abstract RemoveProfileModel<P> getRemoveProfileModel();

    protected abstract QosType getQosType();

    protected abstract Guid getStoragePoolId();

    protected abstract QueryType getQueryType();

    public void newProfile() {
        if (getWindow() != null) {
            return;
        }

        ProfileBaseModel<P, Q, R> model = getNewProfileModel();
        setWindow(model);

        initProfileParentList(model);
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        ProfileBaseModel<P, Q, R> model = getEditProfileModel();
        setWindow(model);

        initProfileParentList(model);
    }


    public void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveProfileModel<P> model = getRemoveProfileModel();
        setWindow(model);
    }

    private void initProfileParentList(ProfileBaseModel<P, Q, R> model) {
        model.getParentListModel().setItems(Collections.singletonList(getEntity()));
        model.getParentListModel().setSelectedItem(getEntity());
        model.getParentListModel().setIsChangeable(false);
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }

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
        if (getEntity() == null) {
            return;
        }
        Guid dcId = getStoragePoolId();
        if (dcId == null) { // not attached to data center
            fetchProfiles();
        } else {
        Frontend.getInstance().runQuery(QueryType.GetAllQosByStoragePoolIdAndType,
                new QosQueryParameterBase(dcId, getQosType()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                        List<Q> qosList = returnValue.getReturnValue();
                        qosMap = new HashMap<>();
                    if (qosList != null) {
                            for (Q qos : qosList) {
                                qosMap.put(qos.getId(), qos);
                        }
                    }
                        fetchProfiles();
                }));
        }
    }

    private void fetchProfiles() {
        if (getEntity() == null) {
            return;
        }
        Frontend.getInstance().runQuery(getQueryType(),
                new IdQueryParameters(getEntity().getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> setItems(returnValue.getReturnValue())));
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        R parentEntity = getEntity();

        getNewCommand().setIsExecutionAllowed(parentEntity != null);
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newProfile();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public Q getQos(Guid qosId) {
        return qosMap.get(qosId);
    }

    public PermissionListModel<P> getPermissionListModel() {
        return permissionListModel;
    }
}
