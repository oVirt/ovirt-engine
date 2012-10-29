package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private static String ENGINE_NETWORK;

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
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

    public NetworkListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networksTitle());
        setHashName("networks"); //$NON-NLS-1$

        setDefaultSearchString("Network:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.NETWORK_OBJ_NAME, SearchObjects.NETWORK_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

     // get management network name
        AsyncDataProvider.GetManagementNetworkName(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ENGINE_NETWORK = (String) returnValue;
                UpdateActionAvailability();
            }
        }));
    }


    public void newNetwork()
    {
    }

    public void edit()
    {
    }

    public void remove()
    {
    }

    @Override
    protected void InitDetailModels()
    {
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
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("network"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Network);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.Network, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    public void cancel()
    {
        setWindow(null);
        setConfirmWindow(null);
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



    private void UpdateActionAvailability()
    {
        List tempVar = getSelectedItems();
        ArrayList selectedItems =
                (ArrayList) ((tempVar != null) ? tempVar : new ArrayList());

        boolean anyEngine = false;
        for (Object item : selectedItems)
        {
            Network network = (Network) item;
            if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
            {
                anyEngine = true;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);
    }



    @Override
    public void ExecuteCommand(UICommand command)
    {
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
    }


    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged()
    {
        UpdateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "NetworkListModel"; //$NON-NLS-1$
    }

}
