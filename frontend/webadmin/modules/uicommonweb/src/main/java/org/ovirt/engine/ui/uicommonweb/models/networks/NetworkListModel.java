package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.RemoveNetworksModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class NetworkListModel extends ListWithSimpleDetailsModel<NetworkView, NetworkView> {

    private UICommand newCommand;
    private UICommand importCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private final Provider<ImportNetworksModel> importNetworkModelProvider;
    private final NetworkExternalSubnetListModel externalSubnetListModel;
    private final NetworkGeneralModel generalModel;
    private final NetworkProfileListModel profileListModel;
    private final NetworkClusterListModel clusterListModel;
    private final NetworkHostListModel hostListModel;
    private final NetworkVmListModel vmListModel;
    private final NetworkTemplateListModel templateListModel;
    private final PermissionListModel<NetworkView> permissionListModel;

    @Inject
    public NetworkListModel(final Provider<ImportNetworksModel> importNetworkModelProvider,
            final NetworkExternalSubnetListModel networkExternalSubnetListModel,
            final NetworkGeneralModel networkGeneralModel,
            final NetworkProfileListModel networkProfileListModel,
            final NetworkClusterListModel networkClusterListModel,
            final NetworkHostListModel networkHostListModel,
            final NetworkVmListModel networkVmListModel,
            final NetworkTemplateListModel networkTemplateListModel,
            final PermissionListModel<NetworkView> permissionListModel) {
        this.importNetworkModelProvider = importNetworkModelProvider;
        this.externalSubnetListModel = networkExternalSubnetListModel;
        this.generalModel = networkGeneralModel;
        this.profileListModel = networkProfileListModel;
        this.clusterListModel = networkClusterListModel;
        this.hostListModel = networkHostListModel;
        this.vmListModel = networkVmListModel;
        this.templateListModel = networkTemplateListModel;
        this.permissionListModel = permissionListModel;
        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().networksTitle());
        setHelpTag(HelpTag.networks);
        setApplicationPlace(WebAdminApplicationPlaces.networkMainPlace);
        setHashName("networks"); //$NON-NLS-1$

        setDefaultSearchString(SearchStringMapping.NETWORK_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
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

    private void setDetailList() {
        List<HasEntity<NetworkView>> list = new ArrayList<>();

        list.add(generalModel);
        list.add(profileListModel);
        list.add(externalSubnetListModel);
        list.add(clusterListModel);
        list.add(hostListModel);
        list.add(vmListModel);
        list.add(templateListModel);
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
        final Network network = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final NetworkModel networkModel = new EditNetworkModel(network, this);
        setWindow(networkModel);

        initDcList(networkModel);
    }

    public void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new RemoveNetworksModel(this);
        setConfirmWindow(model);
    }

    private void initDcList(final NetworkModel networkModel) {
        // Get all data centers
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {

            networkModel.getDataCenters().setItems(dataCenters);

            if (networkModel instanceof EditNetworkModel) {
                StoragePool currentDc =
                        findDc(networkModel.getNetwork().getDataCenterId(), dataCenters);
                networkModel.getDataCenters().setSelectedItem(currentDc);
            } else {
                networkModel.getDataCenters().setSelectedItem(Linq.firstOrNull(dataCenters));
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
        if (network == null || !network.isExternal()) {
            externalSubnetListModel.setIsAvailable(false);
        } else {
            AsyncDataProvider.getInstance().getProviderById(
                    new AsyncQuery<>(provider -> {
                        boolean available = provider != null ? !provider.getIsUnmanaged() : false;
                        externalSubnetListModel.setIsAvailable(available);
                    }),
                    network.getProvidedBy().getProviderId());
        }
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
        super.syncSearch(QueryType.Search, tempVar);
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
        List<NetworkView> tempVar = getSelectedItems();
        List<NetworkView> selectedItems = (tempVar != null) ? tempVar : new ArrayList<>();

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);

        getNewCommand().setIsAvailable(true);
        getRemoveCommand().setIsAvailable(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newNetwork();
        } else if (command == getImportCommand()) {
            importNetworks();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
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

    public NetworkGeneralModel getGeneralModel() {
        return generalModel;
    }

    public NetworkProfileListModel getProfileListModel() {
        return profileListModel;
    }

    public NetworkExternalSubnetListModel getExternalSubnetListModel() {
        return externalSubnetListModel;
    }

    public NetworkClusterListModel getClusterListModel() {
        return clusterListModel;
    }

    public NetworkHostListModel getHostListModel() {
        return hostListModel;
    }

    public NetworkVmListModel getVmListModel() {
        return vmListModel;
    }

    public NetworkTemplateListModel getTemplateListModel() {
        return templateListModel;
    }

    public PermissionListModel<NetworkView> getPermissionListModel() {
        return permissionListModel;
    }

}
