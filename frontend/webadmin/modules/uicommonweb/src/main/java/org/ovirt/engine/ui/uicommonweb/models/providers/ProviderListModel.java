package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class ProviderListModel extends ListWithSimpleDetailsModel<Void, Provider> {

    private static final String CMD_ADD = "Add"; //$NON-NLS-1$
    private static final String CMD_EDIT = "Edit"; //$NON-NLS-1$
    private static final String CMD_REMOVE = "Remove"; //$NON-NLS-1$
    private static final String CMD_FORCE_REMOVE = "ForceRemove"; //$NON-NLS-1$

    private UICommand addCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private UICommand forceRemoveCommand;

    private final ProviderNetworkListModel networkListModel;
    private final ProviderSecretListModel secretListModel;
    private final ProviderGeneralModel generalModel;

    @Inject
    public ProviderListModel(final ProviderGeneralModel providerGeneralModel,
            final ProviderNetworkListModel providerNetworkListModel,
            final ProviderSecretListModel providerSecretListModel) {
        this.networkListModel = providerNetworkListModel;
        this.secretListModel = providerSecretListModel;
        this.generalModel = providerGeneralModel;
        setModelList();

        setTitle(ConstantsManager.getInstance().getConstants().providersTitle());
        setHelpTag(HelpTag.providers);
        setApplicationPlace(WebAdminApplicationPlaces.providerMainPlace);
        setHashName("providers"); //$NON-NLS-1$

        setDefaultSearchString(SearchStringMapping.PROVIDER_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.PROVIDER_OBJ_NAME, SearchObjects.PROVIDER_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setAddCommand(new UICommand(CMD_ADD, this));
        setEditCommand(new UICommand(CMD_EDIT, this));
        setRemoveCommand(new UICommand(CMD_REMOVE, this));
        setForceRemoveCommand(new UICommand(CMD_FORCE_REMOVE, this));

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setModelList() {
        List<HasEntity<Provider>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(networkListModel);
        list.add(secretListModel);
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

    public UICommand getForceRemoveCommand() {
        return forceRemoveCommand;
    }

    public void setForceRemoveCommand(UICommand forceRemoveCommand) {
        this.forceRemoveCommand = forceRemoveCommand;
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        Provider provider = getSelectedItem();
        if (provider != null) {
            networkListModel.setIsAvailable(provider.getType()
                    .getProvidedTypes()
                    .contains(VdcObjectType.Network));
        }
    }

    @Override
    protected String getListName() {
        return "ProviderListModel"; //$NON-NLS-1$
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
        getForceRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);

        getAddCommand().setIsAvailable(true);
        getRemoveCommand().setIsAvailable(true);
    }

    private boolean isSelectedProviderOfType(ProviderType providerType) {
        return getSelectedItems() != null && getSelectedItems().size() == 1 &&
                getSelectedItems().get(0).getType() == providerType;
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
        super.syncSearch(QueryType.Search, tempVar);
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
        setConfirmWindow(new RemoveProvidersModel(this, false));
    }

    private void forceRemove() {
        if (getConfirmWindow() != null) {
            return;
        }
        setConfirmWindow(new RemoveProvidersModel(this, true));
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
        } else if (command == getForceRemoveCommand()) {
            forceRemove();
        }
    }

    public ProviderGeneralModel getGeneralModel() {
        return generalModel;
    }

    public ProviderNetworkListModel getNetworkListModel() {
        return networkListModel;
    }

    public ProviderSecretListModel getSecretListModel() {
        return secretListModel;
    }

}
