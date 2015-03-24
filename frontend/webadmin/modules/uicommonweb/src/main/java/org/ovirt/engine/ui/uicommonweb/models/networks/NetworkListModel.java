package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.RemoveNetworksModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class NetworkListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private static String ENGINE_NETWORK =
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private UICommand newCommand;
    private UICommand importCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    private NetworkExternalSubnetListModel networkExternalSubnetListModel;

    public NetworkListModel() {
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

    public void newNetwork() {
        if (getWindow() != null)
        {
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

        setWindow(new ImportNetworksModel(this));
    }

    public void edit() {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final Network network = (Network) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final NetworkModel networkModel = new EditNetworkModel(network, this);
        setWindow(networkModel);

        initDcList(networkModel);

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Network)
        {
            networkModel.getName().setIsChangable(false);
            networkModel.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
        }


    }

    public void remove() {
        if (getConfirmWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new RemoveNetworksModel(this);
        setConfirmWindow(model);
    }

    private void initDcList(final NetworkModel networkModel) {
        networkModel.startProgress(null);

        SystemTreeItemModel treeSelectedDc = SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
        if (treeSelectedDc != null) {
            StoragePool dc = (StoragePool) treeSelectedDc.getEntity();
            networkModel.getDataCenters().setItems(Arrays.asList(dc));
            networkModel.getDataCenters().setSelectedItem(dc);
            networkModel.getDataCenters().setIsChangable(false);
            networkModel.stopProgress();
            return;
        }

        // Get all data centers
        AsyncDataProvider.getDataCenterList(new AsyncQuery(NetworkListModel.this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {

                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                networkModel.getDataCenters().setItems(dataCenters);

                if (networkModel instanceof EditNetworkModel) {
                    StoragePool currentDc =
                            findDc(networkModel.getNetwork().getDataCenterId(), dataCenters);
                    networkModel.getDataCenters().setSelectedItem(currentDc);
                } else {
                    networkModel.getDataCenters().setSelectedItem(Linq.firstOrDefault(dataCenters));
                }
                networkModel.stopProgress();
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
    protected void initDetailModels() {
        super.initDetailModels();

        networkExternalSubnetListModel = new NetworkExternalSubnetListModel();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();

        list.add(new NetworkGeneralModel());
        list.add(new NetworkProfileListModel());
        list.add(networkExternalSubnetListModel);
        list.add(new NetworkClusterListModel());
        list.add(new NetworkHostListModel());
        list.add(new NetworkVmListModel());
        list.add(new NetworkTemplateListModel());
        list.add(new PermissionListModel());

        setDetailModels(list);
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        NetworkView network = (NetworkView) getSelectedItem();
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
        ArrayList selectedItems =
                (ArrayList) ((tempVar != null) ? tempVar : new ArrayList());

        boolean anyEngine = false;
        for (Object item : selectedItems)
        {
            Network network = (Network) item;
            if (ObjectUtils.objectsEqual(network.getName(), ENGINE_NETWORK))
            {
                anyEngine = true;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Network);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newNetwork();
        }
        else if (command == getImportCommand())
        {
            importNetworks();
        }
        else if (command == getEditCommand())
        {
            edit();
        }

        else if (command == getRemoveCommand())
        {
            remove();
        }
    }

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged() {
        updateActionAvailability();
    }

    @Override
    public NetworkView getEntity() {
        return (NetworkView) super.getEntity();
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
