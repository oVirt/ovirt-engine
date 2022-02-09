package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class VnicProfileListModel extends ListWithSimpleDetailsModel<VnicProfileView, VnicProfileView> {

    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private final VnicProfileVmListModel vmListModel;

    public VnicProfileVmListModel getVmListModel() {
        return vmListModel;
    }

    private final PermissionListModel<VnicProfileView> permissionListModel;

    public PermissionListModel<VnicProfileView> getPermissionListModel() {
        return permissionListModel;
    }

    private final VnicProfileTemplateListModel templateListModel;

    public VnicProfileTemplateListModel getTemplateListModel() {
        return templateListModel;
    }

    @Inject
    public VnicProfileListModel(final VnicProfileVmListModel vNicProfileVmListModel,
            final VnicProfileTemplateListModel vNicProfileTemplateListModel,
            final PermissionListModel<VnicProfileView> permissionListModel) {
        this.vmListModel = vNicProfileVmListModel;
        this.permissionListModel = permissionListModel;
        this.templateListModel = vNicProfileTemplateListModel;

        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfilesTitle());
        setHelpTag(HelpTag.vnicProfiles);
        setApplicationPlace(WebAdminApplicationPlaces.vnicProfileMainPlace);
        setHashName("vnicProfiles"); //$NON-NLS-1$)

        setDefaultSearchString(SearchStringMapping.VNIC_PROFILE_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VNIC_PROFILE_OBJ_NAME, SearchObjects.VNIC_PROFILE_PLU_OBJ_NAME});
        setAvailableInModes(ApplicationMode.VirtOnly);

        setComparator(Linq.VnicProfileViewComparator);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList() {
        List<HasEntity<VnicProfileView>> list = new ArrayList<>();

        list.add(vmListModel);
        list.add(templateListModel);
        list.add(permissionListModel);

        setDetailModels(list);
    }

    public void newProfile() {
        if (getWindow() != null) {
            return;
        }

        final VnicProfileModel profileModel =
                new NewVnicProfileModel(this, null);
        setWindow(profileModel);
    }

    public void edit() {
        final VnicProfileView profile = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        VnicProfileView profileView = getSelectedItem();
        final VnicProfileModel profileModel =
                new EditVnicProfileModel(this, profile, profileView.getDataCenterId());
        setWindow(profileModel);
    }

    public void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new RemoveVnicProfileModel(this, getSelectedItems(), true);
        setConfirmWindow(model);

    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("profile"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar =
                new SearchParameters(applySortOptions(getSearchString()), SearchType.VnicProfile, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
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
        Collection<VnicProfileView> tempVar = getSelectedItems();
        Collection<VnicProfileView> selectedItems = tempVar != null ? tempVar : new ArrayList();

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newProfile();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    protected String getListName() {
        return "VnicProfileListModel"; //$NON-NLS-1$
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
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
