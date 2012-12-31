package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.RemoveNetworksModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private static String ENGINE_NETWORK =
            (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    public NetworkListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networksTitle());
        setHashName("networks"); //$NON-NLS-1$

        setDefaultSearchString("Network:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.NETWORK_OBJ_NAME, SearchObjects.NETWORK_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();

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

    public void edit() {
        final Network network = (Network) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final NetworkModel networkModel = new EditNetworkModel(network, this);
        setWindow(networkModel);

        initDcList(networkModel);

    }

    public void apply() {
        EditNetworkModel model = (EditNetworkModel) getWindow();
        model.apply();
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
        // Get all data centers
        AsyncDataProvider.GetDataCenterList(new AsyncQuery(NetworkListModel.this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {

                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) returnValue;
                networkModel.getDataCenters().setItems(dataCenters);

                if (networkModel instanceof EditNetworkModel) {
                    storage_pool currentDc =
                            findDc(networkModel.getNetwork().getstorage_pool_id().getValue(), dataCenters);
                    networkModel.getDataCenters().setSelectedItem(currentDc);
                } else {
                    networkModel.getDataCenters().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                }

            }
        }));
    }

    private storage_pool findDc(Guid dcId, List<storage_pool> dataCenters) {
        for (storage_pool dc : dataCenters) {
            if (dcId.equals(dc.getId())) {
                return dc;
            }
        }
        return null;
    }

    @Override
    protected void InitDetailModels() {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();

        list.add(new NetworkGeneralModel());
        list.add(new NetworkClusterListModel());
        list.add(new NetworkHostListModel());
        list.add(new NetworkVmListModel());
        list.add(new NetworkTemplateListModel());
        list.add(new PermissionListModel());

        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("network"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch() {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Network);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch() {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.Network, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged() {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability() {
        List tempVar = getSelectedItems();
        ArrayList selectedItems =
                (ArrayList) ((tempVar != null) ? tempVar : new ArrayList());

        boolean anyEngine = false;
        for (Object item : selectedItems)
        {
            Network network = (Network) item;
            if (StringHelper.stringsEqual(network.getName(), ENGINE_NETWORK))
            {
                anyEngine = true;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            newNetwork();
        }
        else if (command == getEditCommand())
        {
            edit();
        }

        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), EditNetworkModel.APPLY_COMMAND_NAME))
        {
            apply();
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
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged() {
        UpdateActionAvailability();
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
