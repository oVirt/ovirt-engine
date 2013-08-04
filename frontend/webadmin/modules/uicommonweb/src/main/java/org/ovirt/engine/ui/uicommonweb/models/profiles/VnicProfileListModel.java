package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVnicProfileModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public class VnicProfileListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    private SystemTreeItemModel systemTreeSelectedItem;

    public VnicProfileListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfilesTitle());
        setHashName("vnicProfiles"); //$NON-NLS-1$

        setDefaultSearchString("VnicProfile:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        // setSearchObjects(new String[] { SearchObjects.PROFILE_OBJ_NAME, SearchObjects.PROFILE_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void newProfile() {
        if (getWindow() != null)
        {
            return;
        }

        // TODO - fix
        // final VnicProfileModel profileModel = new NewVnicProfileModel(this, getEntity().getCompatibilityVersion());
        final VnicProfileModel profileModel = new NewVnicProfileModel(this, Version.v3_3);
        setWindow(profileModel);

        initNetworkList(profileModel);
    }

    public void edit() {
        final VnicProfileView profile = (VnicProfileView) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        // TODO - fix
        // final VnicProfileModel profileModel = new EditVnicProfileModel(this, getEntity().getCompatibilityVersion(),
        // getEntity());
        final VnicProfileModel profileModel = new EditVnicProfileModel(this, Version.v3_3, profile);
        setWindow(profileModel);

        initNetworkList(profileModel);

    }

    public void remove() {
        if (getConfirmWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new RemoveVnicProfileModel(this, getSelectedItems(), true);
        setConfirmWindow(model);
    }

    private void initNetworkList(final VnicProfileModel profileModel) {
        profileModel.startProgress(null);

        SystemTreeItemModel treeSelectedItem =
                (SystemTreeItemModel) CommonModel.getInstance().getSystemTree().getSelectedItem();

        SystemTreeItemModel treeSelectedNetwork =
                treeSelectedItem.getType() == SystemTreeItemType.Network ? treeSelectedItem : null;
        if (treeSelectedNetwork != null) {
            Network network = (Network) treeSelectedNetwork.getEntity();
            profileModel.getNetwork().setItems(Arrays.asList(network));
            profileModel.getNetwork().setSelectedItem(network);
            profileModel.getNetwork().setIsChangable(false);
            return;
        }

        SystemTreeItemModel treeSelectedDc =
                treeSelectedItem.getType() == SystemTreeItemType.DataCenter ? treeSelectedItem : null;

        if (treeSelectedDc != null) {
            StoragePool dc = (StoragePool) treeSelectedDc.getEntity();

            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object ReturnValue)
                {
                    ArrayList<Network> networks =
                            (ArrayList<Network>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                    profileModel.getNetwork().setItems(networks);

                    if (profileModel instanceof EditVnicProfileModel) {
                        Network currentNetwork =
                                findNetwork(profileModel.getProfile().getNetworkId(), networks);
                        profileModel.getNetwork().setSelectedItem(currentNetwork);
                        profileModel.getNetwork().setIsChangable(false);
                    } else {
                        profileModel.getNetwork().setSelectedItem(Linq.firstOrDefault(networks));
                    }
                }
            };

            IdQueryParameters queryParams = new IdQueryParameters(dc.getId());
            Frontend.RunQuery(VdcQueryType.GetAllNetworks, queryParams, _asyncQuery);
        }
    }

    private Network findNetwork(Guid networkId, List<Network> networks) {
        for (Network network : networks) {
            if (networkId.equals(network.getId())) {
                return network;
            }
        }
        return null;
    }

    @Override
    protected void initDetailModels() {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();

        list.add(new VnicProfileVmListModel());
        list.add(new VnicProfileTemplateListModel());
        list.add(new PermissionListModel());

        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("profile"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        // TODO - fix
        // SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Profile);
        // tempVar.setMaxCount(getSearchPageSize());
        // super.syncSearch(VdcQueryType.Search, tempVar);

        SystemTreeItemModel treeSelectedItem =
                (SystemTreeItemModel) CommonModel.getInstance().getSystemTree().getSelectedItem();

        if (treeSelectedItem == null) {
            return;
        }

        SystemTreeItemModel treeSelectedNetwork =
                treeSelectedItem.getType() == SystemTreeItemType.Network ? treeSelectedItem : null;

        if (treeSelectedNetwork != null) {
            Network network = (Network) treeSelectedNetwork.getEntity();
            AsyncQuery asyncQuery = new AsyncQuery();
            asyncQuery.setModel(this);
            asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue)
                {
                    List<VnicProfileView> newItems = (List<VnicProfileView>) returnValue;
                    SearchableListModel searchableListModel = (SearchableListModel) model;
                    searchableListModel.setItems(newItems);
                }
            };
            AsyncDataProvider.getVnicProfilesByNetworkId(asyncQuery, network.getId());
            return;
        }

        SystemTreeItemModel treeSelectedDc =
                treeSelectedItem.getType() == SystemTreeItemType.DataCenter ? treeSelectedItem : null;

        if (treeSelectedDc != null) {
            StoragePool dc = (StoragePool) treeSelectedDc.getEntity();

            AsyncQuery asyncQuery = new AsyncQuery();
            asyncQuery.setModel(this);
            asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue)
                {
                    List<VnicProfileView> notFilteredProfiles = (List<VnicProfileView>) returnValue;
                    List<VnicProfileView> filteredProfiles = new ArrayList<VnicProfileView>();

                    for (VnicProfileView profile : notFilteredProfiles) {
                        // TODO - fix
                        // if (profile.getDataCenterName().equals(dc.getName())){
                        filteredProfiles.add(profile);
                        // }
                    }

                    SearchableListModel searchableListModel = (SearchableListModel) model;
                    searchableListModel.setItems(filteredProfiles);
                }
            };
            AsyncDataProvider.getAllVnicProfiles(asyncQuery);
        }
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

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newProfile();
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
    public VnicProfileView getEntity() {
        return (VnicProfileView) super.getEntity();
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
