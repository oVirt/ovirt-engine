package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.RemoveNetworksModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class NetworkListModel extends ListWithSimpleDetailsModel<NetworkView, NetworkView> implements ISupportSystemTreeContext {
    private UICommand newCommand;
    private UICommand importCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    private final NetworkExternalSubnetListModel networkExternalSubnetListModel;
    private final Provider<ImportNetworksModel> importNetworkModelProvider;

    @Inject
    public NetworkListModel(final Provider<ImportNetworksModel> importNetworkModelProvider,
            final NetworkExternalSubnetListModel networkExternalSubnetListModel,
            final NetworkGeneralModel networkGeneralModel, final NetworkProfileListModel networkProfileListModel,
            final NetworkClusterListModel networkClusterListModel,
            final NetworkHostListModel networkHostListModel, final NetworkVmListModel networkVmListModel,
            final NetworkTemplateListModel networkTemplateListModel,
            final PermissionListModel<NetworkView> permissionListModel) {
        this.networkExternalSubnetListModel = networkExternalSubnetListModel;
        this.importNetworkModelProvider = importNetworkModelProvider;
        setDetailList(networkGeneralModel, networkProfileListModel, networkClusterListModel, networkHostListModel,
                networkVmListModel, networkTemplateListModel, permissionListModel);
        setTitle(ConstantsManager.getInstance().getConstants().networksTitle());
        setHelpTag(HelpTag.networks);
        setApplicationPlace(WebAdminApplicationPlaces.networkMainTabPlace);
        setHashName("networks"); //$NON-NLS-1$

        setDefaultSearchString("Network:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.NETWORK_OBJ_NAME, SearchObjects.NETWORK_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setImportCommand(new UICommand("Import", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final NetworkGeneralModel networkGeneralModel,
            final NetworkProfileListModel networkProfileListModel,
            final NetworkClusterListModel networkClusterListModel,
            final NetworkHostListModel networkHostListModel, final NetworkVmListModel networkVmListModel,
            final NetworkTemplateListModel networkTemplateListModel,
            final PermissionListModel<NetworkView> permissionListModel) {
        List<HasEntity<NetworkView>> list = new ArrayList<>();

        list.add(networkGeneralModel);
        list.add(networkProfileListModel);
        list.add(networkExternalSubnetListModel);
        list.add(networkClusterListModel);
        list.add(networkHostListModel);
        list.add(networkVmListModel);
        list.add(networkTemplateListModel);
        list.add(permissionListModel);

        setDetailModels(list);
    }

    public void newNetwork() {
        if (getWindow() != null) {
            return;
        }

        final NetworkModel networkModel = new NewNetworkModel(this);
        setWindow(networkModel);

        initDcList(networkModel);
    }

    public void importNetworks() {
        if (getWindow() != null) {
            return;
        }

        setWindow(importNetworkModelProvider.get());
    }

    public void edit() {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final Network network = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final NetworkModel networkModel = new EditNetworkModel(network, this);
        setWindow(networkModel);

        initDcList(networkModel);

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Network) {
            networkModel.getName().setIsChangeable(false);
            networkModel.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
        }
    }

    public void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new RemoveNetworksModel(this);
        setConfirmWindow(model);
    }

    private void initDcList(final NetworkModel networkModel) {
        SystemTreeItemModel treeSelectedDc = SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
        if (treeSelectedDc != null) {
            StoragePool dc = (StoragePool) treeSelectedDc.getEntity();
            networkModel.getDataCenters().setItems(Arrays.asList(dc));
            networkModel.getDataCenters().setSelectedItem(dc);
            networkModel.getDataCenters().setIsChangeable(false);
            return;
        }

        // Get all data centers
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(networkModel, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {

                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                networkModel.getDataCenters().setItems(dataCenters);

                if (networkModel instanceof EditNetworkModel) {
                    StoragePool currentDc =
                            findDc(networkModel.getNetwork().getDataCenterId(), dataCenters);
                    networkModel.getDataCenters().setSelectedItem(currentDc);
                } else {
                    networkModel.getDataCenters().setSelectedItem(Linq.firstOrNull(dataCenters));
                }
            }
        }));
    }

    private StoragePool findDc(Guid dcId, List<StoragePool> dataCenters) {
        for (StoragePool dc : dataCenters) {
            if (dcId.equals(dc.getId())) {
                return dc;
            }
        }
        return null;
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        NetworkView network = getSelectedItem();
        networkExternalSubnetListModel.setIsAvailable(network != null && network.isExternal());
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("network"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar =
                new SearchParameters(applySortOptions(getSearchString()), SearchType.Network, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
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
        List selectedItems = (tempVar != null) ? tempVar : new ArrayList();

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Network);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newNetwork();
        }
        else if (command == getImportCommand()) {
            importNetworks();
        }
        else if (command == getEditCommand()) {
            edit();
        }

        else if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value) {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged() {
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "NetworkListModel"; //$NON-NLS-1$
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    public UICommand getImportCommand() {
        return importCommand;
    }

    private void setImportCommand(UICommand value) {
        importCommand = value;
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
}
