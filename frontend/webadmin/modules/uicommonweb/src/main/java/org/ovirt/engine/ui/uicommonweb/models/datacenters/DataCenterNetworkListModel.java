package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DataCenterNetworkListModel extends SearchableListModel<StoragePool, Network> {

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    public DataCenterNetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().logicalNetworksTitle());
        setHelpTag(HelpTag.logical_networks);
        setHashName("logical_networks"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
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

        super.syncSearch();

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetAllNetworks, tempVar, new AsyncQuery<QueryReturnValue>(returnValue -> {
            ArrayList<Network> newItems = returnValue.getReturnValue();
            Collections.sort(newItems, new NameableComparator());
            setItems(newItems);
        }));
    }

    public void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new RemoveNetworksModel(this);
        setConfirmWindow(model);
    }

    public void edit() {
        final Network network = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final NetworkModel networkModel = new EditNetworkModel(network, this);
        setWindow(networkModel);

        networkModel.getDataCenters().setItems(Arrays.asList(getEntity()));
        networkModel.getDataCenters().setSelectedItem(getEntity());

    }

    public void newNetwork() {
        if (getWindow() != null) {
            return;
        }

        final NetworkModel networkModel = new NewNetworkModel(this);
        setWindow(networkModel);

        networkModel.getDataCenters().setItems(Arrays.asList(getEntity()));
        networkModel.getDataCenters().setSelectedItem(getEntity());
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
        List tempVar = getSelectedItems();
        ArrayList selectedItems =
                (ArrayList) ((tempVar != null) ? tempVar : new ArrayList());

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newNetwork();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterNetworkListModel"; //$NON-NLS-1$
    }

}
