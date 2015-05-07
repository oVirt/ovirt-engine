package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class ProviderListModel extends ListWithSimpleDetailsModel<Void, Provider> implements ISupportSystemTreeContext {

    private static final String CMD_ADD = "Add"; //$NON-NLS-1$
    private static final String CMD_EDIT = "Edit"; //$NON-NLS-1$
    private static final String CMD_REMOVE = "Remove"; //$NON-NLS-1$

    private UICommand addCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private final ProviderNetworkListModel providerNetworkListModel;
    private final ProviderSecretListModel providerSecretListModel;

    private SystemTreeItemModel systemTreeSelectedItem;

    @Inject
    public ProviderListModel(final ProviderGeneralModel providerGeneralModel,
            ProviderNetworkListModel providerNetworkListModel,
            ProviderSecretListModel providerSecretListModel) {
        this.providerNetworkListModel = providerNetworkListModel;
        this.providerSecretListModel = providerSecretListModel;
        setModelList(providerGeneralModel, providerNetworkListModel, providerSecretListModel);

        setTitle(ConstantsManager.getInstance().getConstants().providersTitle());
        setHelpTag(HelpTag.providers);
        setApplicationPlace(WebAdminApplicationPlaces.providerMainTabPlace);
        setHashName("providers"); //$NON-NLS-1$

        setDefaultSearchString("Provider:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.PROVIDER_OBJ_NAME, SearchObjects.PROVIDER_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setAddCommand(new UICommand(CMD_ADD, this));
        setEditCommand(new UICommand(CMD_EDIT, this));
        setRemoveCommand(new UICommand(CMD_REMOVE, this));

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setModelList(final ProviderGeneralModel providerGeneralModel,
            final ProviderNetworkListModel providerNetworkListModel,
            final ProviderSecretListModel providerSecretListModel) {
        List<HasEntity<Provider>> list = new ArrayList<>();
        list.add(providerGeneralModel);
        list.add(providerNetworkListModel);
        list.add(providerSecretListModel);
        setDetailModels(list);
    }

    public UICommand getAddCommand() {
        return addCommand;
    }

    private void setAddCommand(UICommand value) {
        addCommand = value;
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

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        Provider provider = getSelectedItem();
        if (provider != null) {
            providerNetworkListModel.setIsAvailable(provider.getType()
                    .getProvidedTypes()
                    .contains(VdcObjectType.Network));
            providerSecretListModel.setIsAvailable(provider.getType() == ProviderType.OPENSTACK_VOLUME);
        }
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

    private void updateActionAvailability() {
        Collection<Provider> tempVar = getSelectedItems();
        Collection<Provider> selectedItems = (tempVar != null) ? tempVar : new ArrayList();

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);

        // Hide add/remove commands if a specific provider is chosen in the system tree
        boolean isAvailable =
                getSystemTreeSelectedItem() == null
                        || getSystemTreeSelectedItem().getType() != SystemTreeItemType.Provider;
        getAddCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("provider"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar =
                new SearchParameters(applySortOptions(getSearchString()), SearchType.Provider, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
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
        setWindow(new EditProviderModel(this, getSelectedItem()));
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
