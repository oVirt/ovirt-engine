package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DataCenterNetworkQoSListModel extends SearchableListModel<StoragePool, NetworkQoS> {

    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    public DataCenterNetworkQoSListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networkQoSTitle());
        setHelpTag(HelpTag.network_qos);
        setHashName("network_qos"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                setItems((ArrayList<NetworkQoS>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllNetworkQosByStoragePoolId,
                parameters,
                asyncQuery);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterNetworkQoSListModel"; //$NON-NLS-1$
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    public void setNewCommand(UICommand newCommand) {
        this.newCommand = newCommand;
    }

    public UICommand getEditCommand() {
        return editCommand;
    }

    public void setEditCommand(UICommand editCommand) {
        this.editCommand = editCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    public void setRemoveCommand(UICommand removeCommand) {
        this.removeCommand = removeCommand;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newNetworkQoS();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

    public void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new RemoveNetworkQoSModel(this);
        setConfirmWindow(model);
    }

    public void edit() {
        final NetworkQoS networkQoS = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final NetworkQoSModel networkQoSModel = new EditNetworkQoSModel(networkQoS, this, getEntity());
        setWindow(networkQoSModel);

        networkQoSModel.getDataCenters().setItems(Arrays.asList(getEntity()));
        networkQoSModel.getDataCenters().setSelectedItem(getEntity());

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

    private void updateActionAvailability() {
        List selectedItems = getSelectedItems();

        getEditCommand().setIsExecutionAllowed(selectedItems != null && selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems != null && selectedItems.size() > 0);
    }

    public void newNetworkQoS() {
        if (getWindow() != null) {
            return;
        }

        final NewNetworkQoSModel newNetworkQoSModel = new NewNetworkQoSModel(this, getEntity());
        setWindow(newNetworkQoSModel);

        newNetworkQoSModel.getDataCenters().setItems(Arrays.asList(getEntity()));
        newNetworkQoSModel.getDataCenters().setSelectedItem(getEntity());
    }
}
