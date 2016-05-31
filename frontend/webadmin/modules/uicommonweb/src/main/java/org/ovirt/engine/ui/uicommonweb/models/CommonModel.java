package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxObject;
import org.ovirt.engine.core.searchbackend.SyntaxObjectType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent.ApplySearchStringHandler;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SearchSuggestModel;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.SystemPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ClusterPolicyListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.plugin.PluginModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CommonModel extends ListModel<SearchableListModel> {

    // TODO: "SingedOut" is misspelled.
    public static final EventDefinition signedOutEventDefinition = new EventDefinition("SingedOut", CommonModel.class); //$NON-NLS-1$
    private Event<EventArgs> privateSignedOutEvent;
    private UICommand privateSearchCommand;
    private UICommand privateConfigureCommand;
    private UICommand privateSignOutCommand;
    private UICommand privateClearSearchStringCommand;
    private String searchStringPrefix;
    private boolean hasSearchStringPrefix;
    private String searchString;
    private DbUser loggedInUser;
    private List<AuditLog> privateEvents;
    private AuditLog lastEvent;
    private AuditLog lastAlert;
    private boolean hasSelectedTags;
    private boolean executingSearch;
    private Map<ListModel<?>, String> listModelSearchStringHistory = new HashMap<>();
    private final Map<String, PluginModel> pluginModelMap = new HashMap<>();
    private boolean systemSelected = true;
    private boolean searchEnabled = true;

    private final DataCenterListModel dataCenterListModel;
    private final ClusterListModel<Void> clusterListModel;
    private final HostListModel<Void> hostListModel;
    private final StorageListModel storageListModel;
    private final VmListModel<Void> vmListModel;
    private final PoolListModel poolListModel;
    private final TemplateListModel templateListModel;
    private final UserListModel userListModel;
    private final EventListModel<Void> eventListModel;
    private final EventListModel<Void> mainTabEventListModel;
    private final QuotaListModel quotaListModel;
    private final ReportsListModel reportsListModel;
    private final VolumeListModel volumeListModel;
    private final DiskListModel diskListModel;
    private final NetworkListModel networkListModel;
    private final ProviderListModel providerListModel;
    private final VnicProfileListModel vnicProfileListModel;
    private final RoleListModel roleListModel;
    private final SystemPermissionListModel systemPermissionListModel;
    private final ClusterPolicyListModel clusterPolicyListModel;
    private final InstanceTypeListModel instanceTypeListModel;
    private final SearchSuggestModel searchSuggestModel;
    private final BookmarkListModel bookmarkListModel;
    private final TagListModel tagListModel;
    private final SystemTreeModel systemTreeModel;
    private final AlertListModel alertListModel;
    private final TaskListModel taskListModel;
    private final SessionListModel sessionListModel;
    private final EngineErrataListModel engineErrataListModel;
    private final SharedMacPoolListModel sharedMacPoolListModel;

    @Inject
    private CommonModel(final DataCenterListModel dataCenterListModel,
            final ClusterListModel<Void> clusterListModel,
            final HostListModel<Void> hostListModel,
            final StorageListModel storageListModel,
            final VmListModel<Void> vmListModel,
            final PoolListModel poolListModel,
            final TemplateListModel templateListModel,
            final UserListModel userListModel,
            @Named("footer") final EventListModel<Void> eventListModel,
            @Named("main") final EventListModel<Void> mainTabEventListModel,
            final QuotaListModel quotaListModel,
            final ReportsListModel reportsListModel,
            final VolumeListModel volumeListModel,
            final DiskListModel diskListModel,
            final NetworkListModel networkListModel,
            final ProviderListModel providerListModel,
            final VnicProfileListModel vnicProfileListModel,
            final RoleListModel roleListModel,
            final SystemPermissionListModel systemPermissionListModel,
            final ClusterPolicyListModel clusterPolicyListModel,
            final InstanceTypeListModel instanceTypeListModel,
            final SearchSuggestModel searchSuggestModel,
            final BookmarkListModel bookmarkListModel,
            final TagListModel tagListModel,
            final SystemTreeModel systemTreeModel,
            final AlertListModel alertListModel,
            final TaskListModel taskListModel,
            final SharedMacPoolListModel sharedMacPoolListModel,
            final SessionListModel sessionListModel,
            final EngineErrataListModel engineErrataListModel,
            final EventBus eventBus) {

        this.dataCenterListModel = dataCenterListModel;
        this.clusterListModel = clusterListModel;
        this.hostListModel = hostListModel;
        this.storageListModel = storageListModel;
        this.vmListModel = vmListModel;
        this.poolListModel = poolListModel;
        this.templateListModel = templateListModel;
        this.userListModel = userListModel;
        this.eventListModel = eventListModel;
        this.mainTabEventListModel = mainTabEventListModel;
        this.quotaListModel = quotaListModel;
        this.reportsListModel = reportsListModel;
        this.volumeListModel = volumeListModel;
        this.diskListModel = diskListModel;
        this.networkListModel = networkListModel;
        this.providerListModel = providerListModel;
        this.vnicProfileListModel = vnicProfileListModel;
        this.roleListModel = roleListModel;
        this.systemPermissionListModel = systemPermissionListModel;
        this.clusterPolicyListModel = clusterPolicyListModel;
        this.instanceTypeListModel = instanceTypeListModel;
        this.searchSuggestModel = searchSuggestModel;
        this.bookmarkListModel = bookmarkListModel;
        this.tagListModel = tagListModel;
        this.systemTreeModel = systemTreeModel;
        this.alertListModel = alertListModel;
        this.taskListModel = taskListModel;
        this.sharedMacPoolListModel = sharedMacPoolListModel;
        this.sessionListModel = sessionListModel;
        this.engineErrataListModel = engineErrataListModel;

        setModelList();

        setSignedOutEvent(new Event<>(signedOutEventDefinition));

        UICommand tempVar = new UICommand("Search", this); //$NON-NLS-1$
        tempVar.setIsDefault(true);
        setSearchCommand(tempVar);
        setSignOutCommand(new UICommand("SignOut", this)); //$NON-NLS-1$
        setConfigureCommand(new UICommand("Configure", this)); //$NON-NLS-1$
        setClearSearchStringCommand(new UICommand("ClearSearchString", this)); //$NON-NLS-1$


        getBookmarkList().getNavigatedEvent().addListener(this);

        getTagList().getSelectedItemsChangedEvent().addListener(this);

        getSystemTree().getSelectedItemChangedEvent().addListener(this);
        getSystemTree().getSearchCommand().execute();

        getEventList().getSearchCommand().execute();

        getAlertList().getSearchCommand().execute();

        getTaskList().getSearchCommand().execute();

        initItems();

        setLoggedInUser(Frontend.getInstance().getLoggedInUser());
        getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getEventBus() != null && getSelectedItem() != null) {
                    MainModelSelectionChangeEvent.fire(getEventBus(), getSelectedItem());
                }
            }
        });

        eventBus.addHandler(ApplySearchStringEvent.getType(), new ApplySearchStringHandler() {
            @Override
            public void onApplySearchString(ApplySearchStringEvent event) {
                setSearchString(event.getSearchString(), false);
                getSearchCommand().execute();
            }
        });
    }

    private void setModelList() {
        List<SearchableListModel> modelList = new ArrayList<>();
        modelList.add(this.dataCenterListModel);
        modelList.add(this.clusterListModel);
        modelList.add(this.hostListModel);
        modelList.add(this.storageListModel);
        modelList.add(this.vmListModel);
        modelList.add(this.poolListModel);
        modelList.add(this.templateListModel);
        modelList.add(this.mainTabEventListModel);
        modelList.add(this.quotaListModel);
        modelList.add(this.volumeListModel);
        modelList.add(this.diskListModel);
        modelList.add(this.userListModel);
        modelList.add(this.reportsListModel);
        modelList.add(this.networkListModel);
        modelList.add(this.providerListModel);
        modelList.add(this.vnicProfileListModel);
        modelList.add(this.sessionListModel);
        modelList.add(this.instanceTypeListModel);
        setItems(modelList);
    }

    private void initItems() {
        // Activate the default list model.
        setSelectedItem(getDefaultItem());
    }

    private void updateHasSelectedTags() {
        ArrayList<TagModel> selectedTags =
                getTagList().getSelectedItems() != null ? Linq.<TagModel> cast(getTagList().getSelectedItems())
                        : new ArrayList<TagModel>();

        setHasSelectedTags(getSelectedItem() != null && selectedTags.size() > 0);
    }

    private void tagListModel_SelectedItemsChanged(Object sender, EventArgs e) {

        boolean hadSelectedTags = getHasSelectedTags();
        updateHasSelectedTags();

        // When any tags are selected, only show Hosts, VMs, and Users tabs.
        // These are currently the only nodes for which tags can be assigned.
        // When no tags are selected, show the exact same main tabs that are
        // displayed when the "System" node in the system tree is selected.

        if (getHasSelectedTags()) {
            setAllListModelsUnavailable();
            getHostList().setIsAvailable(true);
            getVmList().setIsAvailable(true);
            getUserList().setIsAvailable(true);
        } else {
            updateAvailability(SystemTreeItemType.System, null);
        }

        // Switch the selected item as neccessary.
        ListModel oldSelectedItem = getSelectedItem();
        if (getHasSelectedTags() && oldSelectedItem != getHostList() && oldSelectedItem != getVolumeList()
                && oldSelectedItem != getVmList()
                && oldSelectedItem != getUserList()) {
            setSelectedItem(getVmList());
        } else if (getHasSelectedTags() || hadSelectedTags) {
            // Update search string only when selecting or de-selecting tags
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<>(prefix);
            RefObject<String> tempRef_search = new RefObject<>(search);
            splitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().execute();
            searchStringChanged();
        }
    }

    private void bookmarkListModel_Navigated(Object sender, final BookmarkEventArgs e) {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().execute();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        // the main tabs that should appear when a bookmark is selected should
        // be the exact same main tabs that are displayed when the "System" node
        // in the system tree is selected.
        updateAvailability(SystemTreeItemType.System, null);

        setSearchStringPrefix(""); //$NON-NLS-1$
        setSearchString(e.getBookmark().getValue());
        getSearchCommand().execute();
    }

    public String getEffectiveSearchString() {
        return getSearchStringPrefix() + getSearchString();
    }

    private void systemTree_ItemChanged(Object sender, EventArgs args) {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().execute();
        updateHasSelectedTags();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        SystemTreeItemModel model = getSystemTree().getSelectedItem();
        if (model == null) {
            return;
        }
        //Check to see if the system node was selected, and we switched to a non system node. If so, store the
        //search string so we can get back to it when system is selected again.
        if (systemSelected && !model.getType().equals(SystemTreeItemType.System)) {
            //Switched away from system, store the search.
            listModelSearchStringHistory.put(getSelectedItem(), getSelectedItem().getSearchString());
        }
        systemSelected = model.getType().equals(SystemTreeItemType.System);
        updateAvailability(model.getType(), model.getEntity());

        // Select a default item depending on system tree selection.
        ListModel oldSelectedItem = getSelectedItem();

        boolean performSearch = false;
        // Do not Change Tab if the Selection is the Reports
        if (!getReportsList().getIsAvailable() ||
                (getSelectedItem() != getReportsList() && !getReportsList().isReportsTabSelected())) {
            changeSelectedTabIfNeeded(model);
            performSearch = true;
        } else {
            getReportsList().refreshReportModel();
        }

        // Update search string if selected item was not changed. If it is,
        // search string will be updated in OnSelectedItemChanged method.
        // dont perform search if refreshing reports
        if (performSearch && getSelectedItem() == oldSelectedItem) {
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<>(prefix);
            RefObject<String> tempRef_search = new RefObject<>(search);
            String searchString = getSelectedItem().getDefaultSearchString();
            // Model is known to be != null
            if (model.getType().equals(SystemTreeItemType.System)
                    && listModelSearchStringHistory.get(getSelectedItem()) != null) {
                searchString = listModelSearchStringHistory.get(getSelectedItem());
            }
            splitSearchString(searchString, tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext) {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem(getSystemTree().getSelectedItem());
            }
        }
    }

    public void updateReportsAvailability() {
        updateReportsAvailability(getSystemTree().getSelectedItem() == null ?
                SystemTreeItemType.System :
                getSystemTree().getSelectedItem().getType());
        getDataCenterList().updateReportsAvailability();
        getClusterList().updateReportsAvailability();
        getHostList().updateReportsAvailability();
        getStorageList().updateReportsAvailability();
        getVmList().updateReportsAvailability();
    }

    private void updateReportsAvailability(SystemTreeItemType type) {
        getReportsList().setIsAvailable(ReportInit.getInstance().isReportsEnabled()
                && ReportInit.getInstance().getDashboard(type.toString()) != null);
    }

    private void updateAvailability(SystemTreeItemType type, Object entity) {

        // Update items availability depending on system tree selection

        getDataCenterList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.System
                || type == SystemTreeItemType.DataCenters);

        getClusterList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Clusters || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.Network
                || type == SystemTreeItemType.System);

        getHostList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Hosts
                || type == SystemTreeItemType.Host || type == SystemTreeItemType.Storage
                || type == SystemTreeItemType.Network || type == SystemTreeItemType.System);

        getVolumeList().setIsAvailable(type == SystemTreeItemType.Cluster_Gluster
                || type == SystemTreeItemType.Volume
                || type == SystemTreeItemType.Volumes
                || type == SystemTreeItemType.System);

        if (type == SystemTreeItemType.Cluster) {
            getVolumeList().setIsAvailable(false);
        }

        getStorageList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Storages || type == SystemTreeItemType.Storage
                || type == SystemTreeItemType.System);

        getQuotaList().setIsAvailable(type == SystemTreeItemType.DataCenter);

        boolean isDataStorage = false;
        if (type == SystemTreeItemType.Storage && entity != null) {
            StorageDomain storage = (StorageDomain) entity;
            isDataStorage = storage.getStorageDomainType().isDataDomain();
        }

        getDiskList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || isDataStorage || type == SystemTreeItemType.System);

        getVmList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Network || isDataStorage
                || type == SystemTreeItemType.VMs
                || type == SystemTreeItemType.System);

        getPoolList().setIsAvailable(type == SystemTreeItemType.System
                || type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster);

        getTemplateList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Network || isDataStorage
                || type == SystemTreeItemType.Templates
                || type == SystemTreeItemType.System);

        if (type == SystemTreeItemType.Cluster_Gluster && entity != null) {
            Cluster cluster = (Cluster) entity;
            if (!cluster.supportsVirtService()) {
                getVmList().setIsAvailable(false);
                getTemplateList().setIsAvailable(false);
                getStorageList().setIsAvailable(false);
                getPoolList().setIsAvailable(false);
            }
        }

        getUserList().setIsAvailable(type == SystemTreeItemType.System);
        getMainTabEventList().setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.System
                || type == SystemTreeItemType.Volume);

        updateReportsAvailability(type);

        getNetworkList().setIsAvailable(type == SystemTreeItemType.Network
                || type == SystemTreeItemType.Networks
                || type == SystemTreeItemType.System || type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster || type == SystemTreeItemType.Host);

        getProviderList().setIsAvailable(type == SystemTreeItemType.Providers
                || type == SystemTreeItemType.Provider);

        getProfileList().setIsAvailable(type == SystemTreeItemType.Network
                || type == SystemTreeItemType.DataCenter);

        getSessionList().setIsAvailable(type == SystemTreeItemType.Sessions);

        getErrataList().setIsAvailable(type == SystemTreeItemType.Errata);
    }

    private void changeSelectedTabIfNeeded(SystemTreeItemModel model) {
        if (getSelectedItem() != null && getSelectedItem().getIsAvailable() && !(selectedItem instanceof PluginModel)) {
            // Do not change tab if we can't show it
            return;
        } else {
            switch (model.getType()) {
            case DataCenters:
            case DataCenter:
                setSelectedItem(getDataCenterList());
                break;
            case Clusters:
            case Cluster:
            case Cluster_Gluster:
                setSelectedItem(getClusterList());
                break;
            case Hosts:
            case Host:
                setSelectedItem(getHostList());
                break;
            case Volumes:
            case Volume:
                setSelectedItem(getVolumeList());
                break;
            case Storages:
            case Storage:
                setSelectedItem(getStorageList());
                break;
            case Templates:
                setSelectedItem(getTemplateList());
                break;
            case VMs:
                setSelectedItem(getVmList());
                break;
            case Disk:
                setSelectedItem(getDiskList());
                break;
            case Networks:
            case Network:
                setSelectedItem(getNetworkList());
                break;
            case Providers:
            case Provider:
                setSelectedItem(getProviderList());
                break;
            case Errata:
                setSelectedItem(getErrataList());
                break;
            case Sessions:
                setSelectedItem(getSessionList());
                break;
            default:
                // webadmin: redirect to default tab in case no tab is selected.
                setSelectedItem(getDefaultItem());
            }
        }
    }

    public void addPluginModel(String historyToken, String searchPrefix) {
        PluginModel model = new PluginModel(historyToken, searchPrefix);
        getItems().add(model);
        pluginModelMap.put(historyToken, model);
    }

    public void setPluginTabSelected(String historyToken) {
        PluginModel model = pluginModelMap.get(historyToken);
        if (model != null) {
            setSelectedItem(model);
            setSearchEnabled(false);
        } else {
            setSelectedItem(getDefaultItem());
        }
    }

    private void searchStringChanged() {
        SystemTreeItemModel model = getSystemTree().getSelectedItem();
        if (model != null && model.getType().equals(SystemTreeItemType.System)) {
            listModelSearchStringHistory.put(getSelectedItem(), getSearchString());
        }
        getBookmarkList().setSearchString(getEffectiveSearchString());
    }

    private void searchStringPrefixChanged() {
        setHasSearchStringPrefix(!StringHelper.isNullOrEmpty(getSearchStringPrefix()));
        getAutoCompleteModel().setPrefix(getSearchStringPrefix());
    }

    private void clearSearchString() {
        setSearchStringImpl(getHasSearchStringPrefix() ? "" : getSelectedItem().getDefaultSearchString(), false); //$NON-NLS-1$
        getSearchCommand().execute();
    }

    public void configure() {
        if (getWindow() != null) {
            return;
        }

        EntityModel model = new EntityModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().ConfigureTitle());
        model.setHelpTag(HelpTag.configure);
        model.setHashName("configure"); //$NON-NLS-1$
        model.setEntity(new Model[] { getRoleList(), getSystemPermissionList(), getClusterPolicyList(),
                getSharedMacPoolListModel() });

        UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    private void setAllListModelsUnavailable() {
        for (ListModel m : getItems()) {
            if (!(m instanceof ReportsListModel)) {
                m.setIsAvailable(false);
            }
        }
    }

    public void cancel() {
        setWindow(null);
    }

    private SearchableListModel getDefaultItem() {
        return getVmList();
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemsChangedEventDefinition) && sender == getTagList()) {
            tagListModel_SelectedItemsChanged(sender, args);
        } else if (ev.matchesDefinition(BookmarkListModel.navigatedEventDefinition) && sender == getBookmarkList()) {
            bookmarkListModel_Navigated(sender, (BookmarkEventArgs) args);
        } else if (ev.matchesDefinition(selectedItemChangedEventDefinition) && sender == getSystemTree()) {
            systemTree_ItemChanged(sender, args);
        }
    }

    @Override
    protected void onSelectedItemChanging(SearchableListModel newValue, SearchableListModel oldValue) {
        super.onSelectedItemChanging(newValue, oldValue);

        if (oldValue != null) {
            // clear the IsEmpty flag, that in the next search the flag will be initialized.
            oldValue.setIsEmpty(false);

            oldValue.setItems(null);

            ListWithDetailsModel<?, ?, ?> listWithDetails =
                    (ListWithDetailsModel<?, ?, ?>) ((oldValue instanceof ListWithDetailsModel) ? oldValue : null);
            if (listWithDetails != null) {
                listWithDetails.setActiveDetailModel(null);
            }

            oldValue.stopRefresh();
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        setSearchEnabled(true);
        super.onSelectedItemChanged();

        if (!executingSearch && getSelectedItem() != null) {
            // Split search string as necessary.
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<>(prefix);
            RefObject<String> tempRef_search = new RefObject<>(search);
            SystemTreeItemModel model = getSystemTree().getSelectedItem();
            String searchString = getSelectedItem().getSearchString();
            if (model != null && model.getType().equals(SystemTreeItemType.System) && !StringUtils.isEmpty(listModelSearchStringHistory.get(getSelectedItem()))) {
                searchString = listModelSearchStringHistory.get(getSelectedItem());
            }
            splitSearchString(searchString, tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSelectedItem().setSearchString(getEffectiveSearchString());
            getSelectedItem().getSearchCommand().execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext) {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem(getSystemTree().getSelectedItem());
            }
        }

        updateHasSelectedTags();
    }

    public void search() {
        executingSearch = true;

        // Prevent from entering an empty search string.
        if (StringHelper.isNullOrEmpty(getEffectiveSearchString()) && getSelectedItem() != null) {
            setSearchString(getSelectedItem().getDefaultSearchString());
        }

        // Determine a list model matching the search string.
        SearchableListModel model = null;
        for (SearchableListModel a : getItems()) {
            if (a.isSearchStringMatch(getEffectiveSearchString())) {
                model = a;
                break;
            }
        }

        if (model != null) {
            // Transfer a search string to the model.
            model.setSearchString(getEffectiveSearchString());

            // Setting it now currently only to false, for case-insensitive search
            model.setCaseSensitiveSearch(false);

            // ListModel.setSelectedItem compares values by reference, following code ensures that
            // "model" will always be selected (even if it's already the currently selected item)
            selectedItem = null;

            // Change active list model as neccesary.
            setSelectedItem(model);

            // Propagate search command to a concrete list model.
            getSelectedItem().getSearchCommand().execute();
        }

        executingSearch = false;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getSearchCommand()) {
            search();
        } else if (command == getConfigureCommand()) {
            configure();
        } else if (command == getClearSearchStringCommand()) {
            clearSearchString();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    /**
     * Splits a search string into two component, the prefix and a search string itself.
     */
    private void splitSearchString(String source, RefObject<String> prefix, RefObject<String> search) {
        ArrayList<TagModel> tags = (ArrayList<TagModel>) getTagList().getSelectedItems();
        SystemTreeItemModel model = getSystemTree().getSelectedItem();

        prefix.argvalue = ""; //$NON-NLS-1$

        // Split for tags.
        if (tags != null && tags.size() > 0) {
            Regex regex = new Regex("tag\\s*=\\s*(?:[\\w-]+)(?:\\sor\\s)?", RegexOptions.IgnoreCase); //$NON-NLS-1$

            String[] array = source.split("[:]", -1); //$NON-NLS-1$
            String entityClause = array[0];
            String searchClause = array[1];

            StringBuilder tagsClause = new StringBuilder();
            for (TagModel tag : tags) {
                tagsClause.append("tag=").append(tag.getName().getEntity()); //$NON-NLS-1$
                if (tag != tags.get(tags.size() - 1)) {
                    tagsClause.append(" or "); //$NON-NLS-1$
                }
            }

            prefix.argvalue = entityClause + ": " + tagsClause.toString(); //$NON-NLS-1$
            search.argvalue = regex.replace(searchClause, "").trim(); //$NON-NLS-1$
        } else if (model != null && model.getType() != SystemTreeItemType.System) {
            // Split for system tree.
            getAutoCompleteModel().setFilter(new String[] { "or", "and" }); //$NON-NLS-1$ //$NON-NLS-2$

            switch (model.getType()) {
            case DataCenters:
                if (getDataCenterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "DataCenter:"; //$NON-NLS-1$
                }
                break;
            case DataCenter:
                if (getDataCenterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "DataCenter: name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Cluster: datacenter.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getHostList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Host: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getStorageList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Storage: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getVmList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Vms: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Template: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getMainTabEventList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Events: event_datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getDiskList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Disk: datacenter.name = " + model.getTitle() + " and disk_type = image"; //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getQuotaList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Quota: storagepoolname = " + model.getTitle(); //$NON-NLS-1$
                } else if (getNetworkList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Network: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getPoolList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Pools: datacenter = " + model.getTitle(); //$NON-NLS-1$
                } else if (getProfileList().isSearchStringMatch(source)) {
                    prefix.argvalue = "VnicProfile: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                break;
            case Clusters:
                if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Cluster: datacenter.name = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
                break;

            case Cluster:
            case Cluster_Gluster:
                if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Cluster: name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getHostList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Host: cluster = " + model.getTitle(); //$NON-NLS-1$
                } else if (getVolumeList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Volume: cluster = " + model.getTitle(); //$NON-NLS-1$
                } else if (getStorageList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Storage: cluster.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getVmList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Vms: cluster = " + model.getTitle(); //$NON-NLS-1$
                } else if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Template: cluster = " + model.getTitle(); //$NON-NLS-1$
                } else if (getMainTabEventList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Events: cluster = " + model.getTitle(); //$NON-NLS-1$
                } else if (getNetworkList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Network: Cluster_network.cluster_name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getPoolList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Pools: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                break;
            case Hosts:
                if (getHostList().isSearchStringMatch(source)) {
                    SystemTreeItemModel cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if (cluster == null) {
                        cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Host: cluster = " + cluster.getTitle(); //$NON-NLS-1$
                }
                break;
            case Host:
                if (getHostList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Host: name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getStorageList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Storage: host.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getVmList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Vms: Host = " + model.getTitle(); //$NON-NLS-1$
                } else if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Template: Hosts.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getMainTabEventList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Events: host.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getNetworkList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Network: Host_network.host_name = " + model.getTitle(); //$NON-NLS-1$
                }
                break;

            case Volumes:
                if (getVolumeList().isSearchStringMatch(source)) {
                    SystemTreeItemModel cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if (cluster == null) {
                        cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Volume: cluster = " + cluster.getTitle(); //$NON-NLS-1$
                }
                break;

            case Volume:
                if (getVolumeList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Volume: name = " + model.getTitle() + " cluster = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Cluster: volume.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getMainTabEventList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Events: volume.name = " + model.getTitle() + " cluster = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                break;
            case Storages:
                if (getStorageList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Storage: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
                break;
            case Storage:
                if (getDataCenterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "DataCenter: storage.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Cluster: storage.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getHostList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Host: storage.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getStorageList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Storage: name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getVmList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Vms: storage.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Templates: storage.name = " + model.getTitle(); //$NON-NLS-1$
                } else if (getMainTabEventList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Events: event_storage = " + model.getTitle(); //$NON-NLS-1$
                } else if (getDiskList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Disk: storages.name = " + model.getTitle(); //$NON-NLS-1$
                }
                break;
            case Templates:
                if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Template: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
                break;
            case VMs:
                if (getVmList().isSearchStringMatch(source)) {
                    SystemTreeItemModel ancestor = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if (ancestor == null) {
                        ancestor = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Vms: cluster = " + ancestor.getTitle(); //$NON-NLS-1$
                }
                break;
            case Networks:
                if (getNetworkList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Network: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
                break;
            case Network:
                if (getNetworkList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Network: name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getClusterList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Cluster: Cluster_network.network_name = " + model.getTitle() + " Datacenter.name = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getHostList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Host : Nic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getVmList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Vm : Vnic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getTemplateList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "Template : Vnic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (getProfileList().isSearchStringMatch(source)) {
                    prefix.argvalue =
                            "VnicProfile : network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                break;
            case Providers:
                if (getProviderList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Provider:"; //$NON-NLS-1$
                }
                break;
            case Provider:
                if (getProviderList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Provider: name = " + model.getTitle(); //$NON-NLS-1$
                }
                break;
            case Sessions:
                if (getSessionList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Session:"; //$NON-NLS-1$
                }
                break;
            case Errata:
                if (getErrataList().isSearchStringMatch(source)) {
                    prefix.argvalue = "Errata:"; //$NON-NLS-1$
                }
                break;
            }

            prefix.argvalue = prefix.argvalue + " "; //$NON-NLS-1$
            search.argvalue = ""; //$NON-NLS-1$
        } else {
            search.argvalue = source;
            getAutoCompleteModel().setFilter(null);
        }
    }

    public Event getSignedOutEvent() {
        return privateSignedOutEvent;
    }

    private void setSignedOutEvent(Event value) {
        privateSignedOutEvent = value;
    }

    public UICommand getSearchCommand() {
        return privateSearchCommand;
    }

    private void setSearchCommand(UICommand value) {
        privateSearchCommand = value;
    }

    public UICommand getConfigureCommand() {
        return privateConfigureCommand;
    }

    private void setConfigureCommand(UICommand value) {
        privateConfigureCommand = value;
    }

    public UICommand getSignOutCommand() {
        return privateSignOutCommand;
    }

    private void setSignOutCommand(UICommand value) {
        privateSignOutCommand = value;
    }

    public UICommand getClearSearchStringCommand() {
        return privateClearSearchStringCommand;
    }

    private void setClearSearchStringCommand(UICommand value) {
        privateClearSearchStringCommand = value;
    }

    @Override
    public List<SearchableListModel> getItems() {
        return (List<SearchableListModel>) super.getItems();
    }

    @Override
    public SearchableListModel getSelectedItem() {
        return super.getSelectedItem();
    }

    public RoleListModel getRoleList() {
        return roleListModel;
    }

    public SystemPermissionListModel getSystemPermissionList() {
        return systemPermissionListModel;
    }

    public ClusterPolicyListModel getClusterPolicyList() {
        return clusterPolicyListModel;
    }

    public InstanceTypeListModel getInstanceTypeList() {
        return instanceTypeListModel;
    }

    public UserListModel getUserList() {
        return userListModel;
    }

    public QuotaListModel getQuotaList() {
        return quotaListModel;
    }

    public SharedMacPoolListModel getSharedMacPoolListModel() {
        return sharedMacPoolListModel;
    }

    public ReportsListModel getReportsList() {
        return reportsListModel;
    }

    public PoolListModel getPoolList() {
        return poolListModel;
    }

    public VolumeListModel getVolumeList() {
        return volumeListModel;
    }

    public EventListModel getEventList() {
        return eventListModel;
    }

    public EventListModel getMainTabEventList() {
        return mainTabEventListModel;
    }

    public DiskListModel getDiskList() {
        return diskListModel;
    }

    public StorageListModel getStorageList() {
        return storageListModel;
    }

    public DataCenterListModel getDataCenterList() {
        return dataCenterListModel;
    }

    public HostListModel<Void> getHostList() {
        return hostListModel;
    }

    public TemplateListModel getTemplateList() {
        return templateListModel;
    }

    public VmListModel<Void> getVmList() {
        return vmListModel;
    }

    public VnicProfileListModel getProfileList() {
        return vnicProfileListModel;
    }

    public ProviderListModel getProviderList() {
        return providerListModel;
    }

    public SessionListModel getSessionList() {
        return sessionListModel;
    }

    public EngineErrataListModel getErrataList() {
        return engineErrataListModel;
    }

    public NetworkListModel getNetworkList() {
        return networkListModel;
    }

    public ClusterListModel<Void> getClusterList() {
        return clusterListModel;
    }

    public BookmarkListModel getBookmarkList() {
        return bookmarkListModel;
    }

    public TagListModel getTagList() {
        return tagListModel;
    }

    public SystemTreeModel getSystemTree() {
        return systemTreeModel;
    }

    public TaskListModel getTaskList() {
        return taskListModel;
    }

    public AlertListModel getAlertList() {
        return alertListModel;
    }

    public SearchSuggestModel getAutoCompleteModel() {
        return searchSuggestModel;
    }

    public String getSearchStringPrefix() {
        return searchStringPrefix;
    }

    public void setSearchStringPrefix(String value) {
        if (!Objects.equals(searchStringPrefix, value)) {
            searchStringPrefix = value;
            searchStringPrefixChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchStringPrefix")); //$NON-NLS-1$
        }
    }

    public boolean getHasSearchStringPrefix() {
        return hasSearchStringPrefix;
    }

    private void setHasSearchStringPrefix(boolean value) {
        if (hasSearchStringPrefix != value) {
            hasSearchStringPrefix = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSearchStringPrefix")); //$NON-NLS-1$
        }
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String value) {
        setSearchString(value, true);
    }

    public void setSearchString(String value, boolean checkIfNewValue) {
        if (value == null || !containsTokens(value, SyntaxObjectType.SORTBY, SyntaxObjectType.PAGE,
                SyntaxObjectType.SORT_DIRECTION)) {
            setSearchStringImpl(value, checkIfNewValue);
        }
    }

    private boolean containsTokens(String searchString, SyntaxObjectType... tokens) {
        if (tokens == null) {
            return false;
        }
        ISyntaxChecker syntaxChecker = getAutoCompleteModel().getConfigurator().getSyntaxChecker();
        if (syntaxChecker == null) {
            return false;
        }
        SyntaxContainer syntaxCont = syntaxChecker.analyzeSyntaxState(searchString, true);
        Set<SyntaxObjectType> searchTokenSet = new HashSet<>();
        for (SyntaxObject syntaxObject : syntaxCont) {
            searchTokenSet.add(syntaxObject.getType());
        }
        Set<SyntaxObjectType> tokenSet = new HashSet<>();
        tokenSet.addAll(Arrays.asList(tokens));
        searchTokenSet.retainAll(tokenSet);
        return !searchTokenSet.isEmpty();
    }

    private void setSearchStringImpl(String value, boolean checkIfNewValue) {
        if (!checkIfNewValue || !Objects.equals(searchString, value)) {
            searchString = value;
            searchStringChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchString")); //$NON-NLS-1$
        }
    }

    public DbUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(DbUser value) {
        if (loggedInUser != value) {
            loggedInUser = value;
            onPropertyChanged(new PropertyChangedEventArgs("LoggedInUser")); //$NON-NLS-1$
        }
    }

    private void setSearchEnabled(boolean value) {
        if (searchEnabled != value) {
            searchEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("SearchEnabled")); //$NON-NLS-1$
        }
    }

    public boolean getSearchEnabled() {
        return this.searchEnabled;
    }

    public List<AuditLog> getEvents() {
        return privateEvents;
    }

    public void setEvents(List<AuditLog> value) {
        privateEvents = value;
    }

    public AuditLog getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(AuditLog value) {
        if (lastEvent != value) {
            lastEvent = value;
            onPropertyChanged(new PropertyChangedEventArgs("LastEvent")); //$NON-NLS-1$
        }
    }

    public AuditLog getLastAlert() {
        return lastAlert;
    }

    public void setLastAlert(AuditLog value) {
        if (lastAlert != value) {
            lastAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("LastAlert")); //$NON-NLS-1$
        }
    }

    public boolean getHasSelectedTags() {
        return hasSelectedTags;
    }

    public void setHasSelectedTags(boolean value) {
        if (hasSelectedTags != value) {
            hasSelectedTags = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSelectedTags")); //$NON-NLS-1$
        }
    }

}
