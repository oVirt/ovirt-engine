package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public class ProviderListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private UICommand addCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    public ProviderListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().providersTitle());
        setHashName("providers"); //$NON-NLS-1$

        setDefaultSearchString("Provider:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        // setSearchObjects(new String[] { SearchObjects.PROVIDER_OBJ_NAME, SearchObjects.PROVIDER_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void add() {
    }

    public void remove() {
    }

    @Override
    protected void initDetailModels() {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();

        list.add(new ProviderGeneralModel());

        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("provider"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                setItems((List<Provider>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        VdcQueryParametersBase params =
                new VdcQueryParametersBase();
        params.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllProviders,
                params,
                asyncQuery);
    }

    @Override
    protected void asyncSearch() {
        // super.AsyncSearch();
        //
        // setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.Provider, getSearchPageSize()));
        // setItems(getAsyncResult().getData());
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

        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand())
        {
            add();
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
    public Provider getEntity() {
        return (Provider) super.getEntity();
    }

    @Override
    protected String getListName() {
        return "ProviderListModel"; //$NON-NLS-1$
    }

    public UICommand getAddCommand() {
        return addCommand;
    }

    private void setAddCommand(UICommand value) {
        addCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }
}
