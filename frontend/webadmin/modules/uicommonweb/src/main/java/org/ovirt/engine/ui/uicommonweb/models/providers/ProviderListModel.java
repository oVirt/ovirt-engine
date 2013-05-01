package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
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

public class ProviderListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {

    private static final String CMD_ADD = "Add"; //$NON-NLS-1$
    private static final String CMD_EDIT = "Edit"; //$NON-NLS-1$
    private static final String CMD_REMOVE = "Remove"; //$NON-NLS-1$

    private UICommand addCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    public ProviderListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().providersTitle());
        setHashName("providers"); //$NON-NLS-1$

        setDefaultSearchString("Provider:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        // setSearchObjects(new String[] { SearchObjects.PROVIDER_OBJ_NAME, SearchObjects.PROVIDER_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setAddCommand(new UICommand(CMD_ADD, this));
        setEditCommand(new UICommand(CMD_EDIT, this));
        setRemoveCommand(new UICommand(CMD_REMOVE, this));

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public UICommand getAddCommand() {
        return addCommand;
    }

    private void setAddCommand(UICommand value) {
        addCommand = value;
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

    @Override
    protected void initDetailModels() {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();

        list.add(new ProviderGeneralModel());

        setDetailModels(list);
    }

    @Override
    protected String getListName() {
        return "ProviderListModel"; //$NON-NLS-1$
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
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @SuppressWarnings("rawtypes")
    private void updateActionAvailability() {
        List tempVar = getSelectedItems();
        List selectedItems = (tempVar != null) ? tempVar : new ArrayList();

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("provider"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                setItems((List<Provider>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        GetAllProvidersParameters params =
                new GetAllProvidersParameters();
        params.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllProviders,
                params,
                asyncQuery);
    }

    private void add() {
        if (getWindow() != null) {
            return;
        }
        setWindow(new AddProviderModel(this));
    }

    private void edit() {
        if (getWindow() != null) {
            return;
        }
        setWindow(new EditProviderModel(this, (Provider) getSelectedItem()));
    }

    private void remove() {
        if (getConfirmWindow() != null) {
            return;
        }
        setConfirmWindow(new RemoveProvidersModel(this));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            add();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

}
