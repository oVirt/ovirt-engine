package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
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
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class CommonModel extends ListModel
{

    // TODO: "SingedOut" is misspelled.
    public static final EventDefinition signedOutEventDefinition = new EventDefinition("SingedOut", CommonModel.class); //$NON-NLS-1$
    private Event privateSignedOutEvent;
    private UICommand privateSearchCommand;
    private UICommand privateConfigureCommand;
    private UICommand privateSignOutCommand;
    private UICommand privateClearSearchStringCommand;
    private BookmarkListModel privateBookmarkList;
    private TagListModel privateTagList;
    private SystemTreeModel privateSystemTree;
    private SearchableListModel privateEventList;
    private TaskListModel privateTaskList;
    private AlertListModel privateAlertList;
    private SearchSuggestModel privateAutoCompleteModel;
    private String searchStringPrefix;
    private boolean hasSearchStringPrefix;
    private String searchString;
    private DbUser loggedInUser;
    private List<AuditLog> privateEvents;
    private AuditLog lastEvent;
    private AuditLog lastAlert;
    private boolean hasSelectedTags;
    private boolean executingSearch;
    private RoleListModel roleListModel;
    private SystemPermissionListModel systemPermissionListModel;
    private ClusterPolicyListModel clusterPolicyListModel;
    private InstanceTypeListModel instanceTypeListModel;
    private SharedMacPoolListModel sharedMacPoolListModel;

    // NOTE: when adding a new ListModel here, be sure to add it to the list in initItems()
    private SearchableListModel dataCenterList;
    private SearchableListModel clusterList;
    private SearchableListModel hostList;
    private SearchableListModel storageList;
    private SearchableListModel vmList;
    private SearchableListModel poolList;
    private SearchableListModel templateList;
    private SearchableListModel userList;
    private SearchableListModel eventList;
    private ReportsListModel reportsList;
    private SearchableListModel quotaList;
    private SearchableListModel volumeList;
    private SearchableListModel diskList;
    private SearchableListModel networkList;
    private SearchableListModel providerList;
    private SearchableListModel profileList;

    private static CommonModel instance = null;

    public static CommonModel newInstance() {
        instance = new CommonModel();
        return instance;
    }

    public static CommonModel getInstance() {
        return instance;
    }

    private CommonModel()
    {
        setSignedOutEvent(new Event(signedOutEventDefinition));

        UICommand tempVar = new UICommand("Search", this); //$NON-NLS-1$
        tempVar.setIsDefault(true);
        setSearchCommand(tempVar);
        setSignOutCommand(new UICommand("SignOut", this)); //$NON-NLS-1$
        setConfigureCommand(new UICommand("Configure", this)); //$NON-NLS-1$
        setClearSearchStringCommand(new UICommand("ClearSearchString", this)); //$NON-NLS-1$

        setAutoCompleteModel(new SearchSuggestModel());

        setBookmarkList(new BookmarkListModel());
        getBookmarkList().getNavigatedEvent().addListener(this);

        setTagList(new TagListModel());
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        setSystemTree(new SystemTreeModel());
        getSystemTree().getSelectedItemChangedEvent().addListener(this);
        getSystemTree().getSearchCommand().execute();

        setEventList(new EventListModel());
        getEventList().getSearchCommand().execute();

        setAlertList(new AlertListModel());
        getAlertList().getSearchCommand().execute();

        setTaskList(new TaskListModel());
        getTaskList().getSearchCommand().execute();

        initItems();

        setLoggedInUser(Frontend.getInstance().getLoggedInUser());
    }

    private void initItems()
    {
        ObservableCollection<SearchableListModel> list = new ObservableCollection<SearchableListModel>();

        dataCenterList = new DataCenterListModel();
        list.add(dataCenterList);
        clusterList = new ClusterListModel();
        list.add(clusterList);
        hostList = new HostListModel();
        list.add(hostList);
        storageList = new StorageListModel();
        list.add(storageList);
        vmList = new VmListModel();
        list.add(vmList);
        poolList = new PoolListModel();
        list.add(poolList);
        templateList = new TemplateListModel();
        list.add(templateList);
        eventList = new EventListModel();
        list.add(eventList);

        quotaList = new QuotaListModel();
        list.add(quotaList);

        volumeList = new VolumeListModel();
        list.add(volumeList);

        diskList = new DiskListModel();
        list.add(diskList);

        userList = new UserListModel();
        list.add(userList);

        reportsList = new ReportsListModel(ReportInit.getInstance().getReportBaseUrl(),
                ReportInit.getInstance().getSsoToken());
        list.add(reportsList);

        reportsList.setIsAvailable(false);

        networkList = new NetworkListModel();
        list.add(networkList);

        providerList = new ProviderListModel();
        list.add(providerList);

        profileList = new VnicProfileListModel();
        list.add(profileList);

        instanceTypeListModel = new InstanceTypeListModel();
        list.add(instanceTypeListModel);

        setItems(list);

        roleListModel = new RoleListModel();
        systemPermissionListModel = new SystemPermissionListModel();
        clusterPolicyListModel = new ClusterPolicyListModel();
        sharedMacPoolListModel = new SharedMacPoolListModel();

        // Activate the default list model.
        setSelectedItem(getDefaultItem());
    }

    private void updateHasSelectedTags()
    {
        ArrayList<TagModel> selectedTags =
                getTagList().getSelectedItems() != null ? Linq.<TagModel> cast(getTagList().getSelectedItems())
                        : new ArrayList<TagModel>();

        setHasSelectedTags(getSelectedItem() != null && selectedTags.size() > 0);
    }

    private void tagListModel_SelectedItemsChanged(Object sender, EventArgs e)
    {
        // Reset system tree to the root item.
        getSystemTree().getSelectedItemChangedEvent().removeListener(this);
        getSystemTree().getResetCommand().execute();
        getSystemTree().getSelectedItemChangedEvent().addListener(this);

        boolean hadSelectedTags = getHasSelectedTags();
        updateHasSelectedTags();

        // When any tags are selected, only show Hosts, VMs, and Users tabs.
        // These are currently the only nodes for which tags can be assigned.
        // When no tags are selected, show the exact same main tabs that are
        // displayed when the "System" node in the system tree is selected.

        if (getHasSelectedTags()) {
            setAllListModelsUnavailable();
            hostList.setIsAvailable(true);
            vmList.setIsAvailable(true);
            userList.setIsAvailable(true);
        }
        else {
            updateAvailability(SystemTreeItemType.System, null);
        }

        // Switch the selected item as neccessary.
        ListModel oldSelectedItem = getSelectedItem();
        if (getHasSelectedTags() && oldSelectedItem != hostList && oldSelectedItem != volumeList
                && oldSelectedItem != vmList
                && oldSelectedItem != userList)
        {
            setSelectedItem(vmList);
        }
        // Update search string only when selecting or de-selecting tags
        else if (getHasSelectedTags() || hadSelectedTags)
        {
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            splitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().execute();
            searchStringChanged();
        }
    }

    private void bookmarkListModel_Navigated(Object sender, BookmarkEventArgs e)
    {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().execute();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        // Reset system tree to the root item.
        getSystemTree().getSelectedItemChangedEvent().removeListener(this);
        getSystemTree().getResetCommand().execute();
        getSystemTree().getSelectedItemChangedEvent().addListener(this);

        // the main tabs that should appear when a bookmark is selected should
        // be the exact same main tabs that are displayed when the "System" node
        // in the system tree is selected.
        updateAvailability(SystemTreeItemType.System, null);

        setSearchStringPrefix(""); //$NON-NLS-1$
        setSearchString(e.getBookmark().getbookmark_value());
        getSearchCommand().execute();
    }

    public String getEffectiveSearchString()
    {
        return getSearchStringPrefix() + getSearchString();
    }

    private void systemTree_ItemChanged(Object sender, EventArgs args)
    {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().execute();
        updateHasSelectedTags();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        SystemTreeItemModel model = getSystemTree().getSelectedItem();
        if (model == null)
        {
            return;
        }

        updateAvailability(model.getType(), model.getEntity());

        // Select a default item depending on system tree selection.
        ListModel oldSelectedItem = getSelectedItem();

        // Do not Change Tab if the Selection is the Reports
        if (!reportsList.getIsAvailable() || getSelectedItem() != reportsList) {
            changeSelectedTabIfNeeded(model);
        } else {
            reportsList.refreshReportModel();
        }

        // Update search string if selected item was not changed. If it is,
        // search string will be updated in OnSelectedItemChanged method.
        if (getSelectedItem() == oldSelectedItem)
        {
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            splitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext)
            {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem(getSystemTree().getSelectedItem());
            }
        }
    }

    private void updateAvailability(SystemTreeItemType type, Object entity) {

        // Update items availability depending on system tree selection

        dataCenterList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.System
                || type == SystemTreeItemType.DataCenters);

        clusterList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Clusters || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.Network
                || type == SystemTreeItemType.System);

        hostList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Hosts
                || type == SystemTreeItemType.Host || type == SystemTreeItemType.Storage
                || type == SystemTreeItemType.Network || type == SystemTreeItemType.System);

        volumeList.setIsAvailable(type == SystemTreeItemType.Cluster_Gluster
                || type == SystemTreeItemType.Volume
                || type == SystemTreeItemType.Volumes
                || type == SystemTreeItemType.System);

        if (type == SystemTreeItemType.Cluster) {
            volumeList.setIsAvailable(false);
        }

        storageList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Storages || type == SystemTreeItemType.Storage
                || type == SystemTreeItemType.System);

        quotaList.setIsAvailable(type == SystemTreeItemType.DataCenter);

        boolean isDataStorage = false;
        if (type == SystemTreeItemType.Storage && entity != null)
        {
            StorageDomain storage = (StorageDomain) entity;
            isDataStorage = storage.getStorageDomainType().isDataDomain();
        }

        diskList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || isDataStorage || type == SystemTreeItemType.System);

        vmList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Network || isDataStorage
                || type == SystemTreeItemType.VMs
                || type == SystemTreeItemType.System);

        poolList.setIsAvailable(type == SystemTreeItemType.System
                || type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster);

        templateList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Network || isDataStorage
                || type == SystemTreeItemType.Templates
                || type == SystemTreeItemType.System);

        if (type == SystemTreeItemType.Cluster_Gluster && entity != null) {
            VDSGroup cluster = (VDSGroup) entity;
            if (!cluster.supportsVirtService()) {
                vmList.setIsAvailable(false);
                templateList.setIsAvailable(false);
                storageList.setIsAvailable(false);
                poolList.setIsAvailable(false);
            }
        }

        userList.setIsAvailable(type == SystemTreeItemType.System);
        eventList.setIsAvailable(type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster
                || type == SystemTreeItemType.Cluster_Gluster || type == SystemTreeItemType.Host
                || type == SystemTreeItemType.Storage || type == SystemTreeItemType.System
                || type == SystemTreeItemType.Volume);

        reportsList.setIsAvailable(ReportInit.getInstance().isReportsEnabled()
                && ReportInit.getInstance().getDashboard(type.toString()) != null);

        networkList.setIsAvailable(type == SystemTreeItemType.Network
                || type == SystemTreeItemType.Networks
                || type == SystemTreeItemType.System || type == SystemTreeItemType.DataCenter
                || type == SystemTreeItemType.Cluster || type == SystemTreeItemType.Host);

        providerList.setIsAvailable(type == SystemTreeItemType.Providers
                || type == SystemTreeItemType.Provider);

        profileList.setIsAvailable(type == SystemTreeItemType.Network
                || type == SystemTreeItemType.DataCenter);
    }

    private void changeSelectedTabIfNeeded(SystemTreeItemModel model) {
        if (getSelectedItem() != null && getSelectedItem().getIsAvailable()) {
            // Do not change tab if we can show it
            return;
        } else {
            switch (model.getType())
            {
            case DataCenters:
            case DataCenter:
                setSelectedItem(dataCenterList);
                break;
            case Clusters:
            case Cluster:
            case Cluster_Gluster:
                setSelectedItem(clusterList);
                break;
            case Hosts:
            case Host:
                setSelectedItem(hostList);
                break;
            case Volumes:
            case Volume:
                setSelectedItem(volumeList);
                break;
            case Storages:
            case Storage:
                setSelectedItem(storageList);
                break;
            case Templates:
                setSelectedItem(templateList);
                break;
            case VMs:
                setSelectedItem(vmList);
                break;
            case Disk:
                setSelectedItem(diskList);
                break;
            case Networks:
            case Network:
                setSelectedItem(networkList);
                break;
            case Providers:
            case Provider:
                setSelectedItem(providerList);
                break;
            default:
                // webadmin: redirect to default tab in case no tab is selected.
                setSelectedItem(getDefaultItem());
            }
        }
    }

    private void searchStringChanged()
    {
        getBookmarkList().setSearchString(getEffectiveSearchString());
    }

    private void searchStringPrefixChanged()
    {
        setHasSearchStringPrefix(!StringHelper.isNullOrEmpty(getSearchStringPrefix()));
        getAutoCompleteModel().setPrefix(getSearchStringPrefix());
    }

    private void clearSearchString()
    {
        setSearchString(getHasSearchStringPrefix() ? "" : getSelectedItem().getDefaultSearchString(), false); //$NON-NLS-1$
        getSearchCommand().execute();
    }

    public void signOut()
    {
        // Stop search on all list models.
        for (SearchableListModel listModel : getItems())
        {
            listModel.stopRefresh();
        }

        getEventList().stopRefresh();
        getAlertList().stopRefresh();
        getTaskList().stopRefresh();
        getBookmarkList().stopRefresh();
        getRoleListModel().stopRefresh();
        getSystemPermissionListModel().stopRefresh();
        getClusterPolicyListModel().stopRefresh();
        getInstanceTypeListModel().stopRefresh();

        if (Frontend.getInstance().getIsUserLoggedIn())
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setHandleFailure(true);
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object ReturnValue) {
                }
            };
            setLoggedInUser(null);
            getSignedOutEvent().raise(this, EventArgs.EMPTY);

            Frontend.getInstance().logoffAsync(Frontend.getInstance().getLoggedInUser(), _asyncQuery);
        }
    }

    public void configure()
    {
        if (getWindow() != null)
        {
            return;
        }

        EntityModel model = new EntityModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().ConfigureTitle());
        model.setHelpTag(HelpTag.configure);
        model.setHashName("configure"); //$NON-NLS-1$
        model.setEntity(new Model[] { roleListModel, systemPermissionListModel, clusterPolicyListModel, sharedMacPoolListModel });

        UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    private void setAllListModelsUnavailable() {
        for (ListModel m : getItems()) {
            m.setIsAvailable(false);
        }
    }

    public void cancel()
    {
        setWindow(null);
    }

    private SearchableListModel getDefaultItem()
    {
        return vmList;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemsChangedEventDefinition) && sender == getTagList())
        {
            tagListModel_SelectedItemsChanged(sender, args);
        }
        else if (ev.matchesDefinition(BookmarkListModel.navigatedEventDefinition) && sender == getBookmarkList())
        {
            bookmarkListModel_Navigated(sender, (BookmarkEventArgs) args);
        }
        else if (ev.matchesDefinition(selectedItemChangedEventDefinition) && sender == getSystemTree())
        {
            systemTree_ItemChanged(sender, args);
        }
    }

    @Override
    protected void onSelectedItemChanging(Object newValue, Object oldValue)
    {
        super.onSelectedItemChanging(newValue, oldValue);

        SearchableListModel oldModel = (SearchableListModel) oldValue;

        if (oldValue != null)
        {
            // clear the IsEmpty flag, that in the next search the flag will be initialized.
            oldModel.setIsEmpty(false);

            oldModel.setItems(null);

            ListWithDetailsModel listWithDetails =
                    (ListWithDetailsModel) ((oldValue instanceof ListWithDetailsModel) ? oldValue : null);
            if (listWithDetails != null)
            {
                listWithDetails.setActiveDetailModel(null);
            }

            oldModel.stopRefresh();
        }
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();

        if (!executingSearch && getSelectedItem() != null)
        {
            // Split search string as necessary.
            String prefix = ""; //$NON-NLS-1$
            String search = ""; //$NON-NLS-1$
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            splitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSelectedItem().setSearchString(getEffectiveSearchString());
            getSelectedItem().getSearchCommand().execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext)
            {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem(getSystemTree().getSelectedItem());
            }
        }

        updateHasSelectedTags();
    }

    public void search()
    {
        executingSearch = true;

        // Prevent from entering an empty search string.
        if (StringHelper.isNullOrEmpty(getEffectiveSearchString()) && getSelectedItem() != null)
        {
            setSearchString(getSelectedItem().getDefaultSearchString());
        }

        // Determine a list model matching the search string.
        SearchableListModel model = null;
        for (SearchableListModel a : getItems())
        {
            if (a.isSearchStringMatch(getEffectiveSearchString()))
            {
                model = a;
                break;
            }
        }

        if (model != null)
        {
            // Transfer a search string to the model.
            model.setSearchString(getEffectiveSearchString());

            // Setting it now currently only to false, for case-insensitive search
            model.setCaseSensitiveSearch(false);

            // Change active list model as neccesary.
            setSelectedItem(model);

            // Propagate search command to a concrete list model.
            getSelectedItem().getSearchCommand().execute();
        }

        executingSearch = false;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getSearchCommand())
        {
            search();
        }
        else if (command == getSignOutCommand())
        {
            signOut();
        }
        else if (command == getConfigureCommand())
        {
            configure();
        }
        else if (command == getClearSearchStringCommand())
        {
            clearSearchString();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }

    /**
     * Splits a search string into two component, the prefix and a search string itself.
     */
    private void splitSearchString(String source, RefObject<String> prefix, RefObject<String> search)
    {
        ArrayList<TagModel> tags = (ArrayList<TagModel>) getTagList().getSelectedItems();
        SystemTreeItemModel model = getSystemTree().getSelectedItem();

        prefix.argvalue = ""; //$NON-NLS-1$

        // Split for tags.
        if (tags != null && tags.size() > 0)
        {
            Regex regex = new Regex("tag\\s*=\\s*(?:[\\w-]+)(?:\\sor\\s)?", RegexOptions.IgnoreCase); //$NON-NLS-1$

            String[] array = source.split("[:]", -1); //$NON-NLS-1$
            String entityClause = array[0];
            String searchClause = array[1];

            StringBuilder tagsClause = new StringBuilder();
            for (TagModel tag : tags)
            {
                tagsClause.append("tag=").append(tag.getName().getEntity()); //$NON-NLS-1$
                if (tag != tags.get(tags.size() - 1))
                {
                    tagsClause.append(" or "); //$NON-NLS-1$
                }
            }

            prefix.argvalue = entityClause + ": " + tagsClause.toString(); //$NON-NLS-1$
            search.argvalue = regex.replace(searchClause, "").trim(); //$NON-NLS-1$
        }
        // Split for system tree.
        else if (model != null && model.getType() != SystemTreeItemType.System)
        {
            getAutoCompleteModel().setFilter(new String[] { "or", "and" }); //$NON-NLS-1$ //$NON-NLS-2$

            switch (model.getType())
            {
            case DataCenters:
                if (dataCenterList.isSearchStringMatch(source)) {
                    prefix.argvalue = "DataCenter:"; //$NON-NLS-1$
                }
                break;
            case DataCenter: {
                if (dataCenterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "DataCenter: name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: datacenter.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (hostList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Host: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (storageList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Storage: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (vmList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Vms: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Template: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (eventList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Events: event_datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (diskList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Disk: datacenter.name = " + model.getTitle() + " and disk_type = image"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (quotaList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Quota: storagepoolname = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (networkList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Network: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (poolList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Pools: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (profileList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "VnicProfile: datacenter = " + model.getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Clusters: {
                if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: datacenter.name = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
            }
                break;

            case Cluster:
            case Cluster_Gluster: {
                if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (hostList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Host: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (volumeList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Volume: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (storageList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Storage: cluster.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (vmList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Vms: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Template: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (eventList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Events: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (networkList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Network: Cluster_network.cluster_name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (poolList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Pools: cluster = " + model.getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Hosts: {
                if (hostList.isSearchStringMatch(source))
                {
                    SystemTreeItemModel cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if(cluster == null) {
                        cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Host: cluster = " + cluster.getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Host: {
                if (hostList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Host: name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (storageList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Storage: host.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (vmList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Vms: Host = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Template: Hosts.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (eventList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Events: host.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (networkList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Network: Host_network.host_name = " + model.getTitle(); //$NON-NLS-1$
                }
            }
                break;

            case Volumes: {
                if (volumeList.isSearchStringMatch(source))
                {
                    SystemTreeItemModel cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if (cluster == null) {
                        cluster = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Volume: cluster = " + cluster.getTitle(); //$NON-NLS-1$
                }
            }
                break;

            case Volume: {
                if (volumeList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Volume: name = " + model.getTitle() + " cluster = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: volume.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (eventList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Events: volume.name = " + model.getTitle() + " cluster = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
                break;
            case Storages: {
                if (storageList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Storage: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Storage: {
                if (dataCenterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "DataCenter: storage.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: storage.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (hostList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Host: storage.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (storageList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Storage: name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (vmList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Vms: storage.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Templates: storage.name = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (eventList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Events: event_storage = " + model.getTitle(); //$NON-NLS-1$
                }
                else if (diskList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Disk: storages.name = " + model.getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Templates: {
                if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Template: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case VMs: {
                if (vmList.isSearchStringMatch(source))
                {
                    SystemTreeItemModel ancestor = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster, model);
                    if (ancestor == null) {
                        ancestor = SystemTreeItemModel.findAncestor(SystemTreeItemType.Cluster_Gluster, model);
                    }
                    prefix.argvalue = "Vms: cluster = " + ancestor.getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Networks: {
                if (networkList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Network: datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$
                }
            }
                break;
            case Network: {
                if (networkList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Network: name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (clusterList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Cluster: Cluster_network.network_name = " + model.getTitle() + " Datacenter.name = " +  SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (hostList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Host : Nic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (vmList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Vm : Vnic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (templateList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "Template : Vnic.network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if (profileList.isSearchStringMatch(source))
                {
                    prefix.argvalue = "VnicProfile : network_name = " + model.getTitle() + " datacenter = " + SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, model).getTitle(); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
                break;
            case Providers:
                if (providerList.isSearchStringMatch(source)) {
                    prefix.argvalue = "Provider:"; //$NON-NLS-1$
                }
                break;
            case Provider:
                if (providerList.isSearchStringMatch(source)) {
                    prefix.argvalue = "Provider: name = " + model.getTitle(); //$NON-NLS-1$
                }
                break;
            }

            prefix.argvalue = prefix.argvalue + " "; //$NON-NLS-1$
            search.argvalue = ""; //$NON-NLS-1$
        }
        else
        {
            search.argvalue = source;
            getAutoCompleteModel().setFilter(null);
        }
    }

    public RoleListModel getRoleListModel() {
        return roleListModel;
    }

    public SystemPermissionListModel getSystemPermissionListModel() {
        return systemPermissionListModel;
    }

    public ClusterPolicyListModel getClusterPolicyListModel() {
        return clusterPolicyListModel;
    }

    public InstanceTypeListModel getInstanceTypeListModel() {
        return instanceTypeListModel;
    }

    public SharedMacPoolListModel getSharedMacPoolListModel() {
        return sharedMacPoolListModel;
    }

    public Event getSignedOutEvent()
    {
        return privateSignedOutEvent;
    }

    private void setSignedOutEvent(Event value)
    {
        privateSignedOutEvent = value;
    }

    public UICommand getSearchCommand()
    {
        return privateSearchCommand;
    }

    private void setSearchCommand(UICommand value)
    {
        privateSearchCommand = value;
    }

    public UICommand getConfigureCommand()
    {
        return privateConfigureCommand;
    }

    private void setConfigureCommand(UICommand value)
    {
        privateConfigureCommand = value;
    }

    public UICommand getSignOutCommand()
    {
        return privateSignOutCommand;
    }

    private void setSignOutCommand(UICommand value)
    {
        privateSignOutCommand = value;
    }

    public UICommand getClearSearchStringCommand()
    {
        return privateClearSearchStringCommand;
    }

    private void setClearSearchStringCommand(UICommand value)
    {
        privateClearSearchStringCommand = value;
    }

    @Override
    public List<SearchableListModel> getItems()
    {
        return (List<SearchableListModel>) super.getItems();
    }

    public void setItems(List<SearchableListModel> value)
    {
        super.setItems(value);
    }

    @Override
    public SearchableListModel getSelectedItem()
    {
        return (SearchableListModel) super.getSelectedItem();
    }

    public void setSelectedItem(SearchableListModel value)
    {
        super.setSelectedItem(value);
    }

    public BookmarkListModel getBookmarkList()
    {
        return privateBookmarkList;
    }

    private void setBookmarkList(BookmarkListModel value)
    {
        privateBookmarkList = value;
    }

    public TagListModel getTagList()
    {
        return privateTagList;
    }

    private void setTagList(TagListModel value)
    {
        privateTagList = value;
    }

    public SystemTreeModel getSystemTree()
    {
        return privateSystemTree;
    }

    private void setSystemTree(SystemTreeModel value)
    {
        privateSystemTree = value;
    }

    public SearchableListModel getEventList()
    {
        return privateEventList;
    }

    private void setEventList(SearchableListModel value)
    {
        privateEventList = value;
    }

    public TaskListModel getTaskList() {
        return privateTaskList;
    }

    public void setTaskList(TaskListModel taskList) {
        this.privateTaskList = taskList;
    }

    public AlertListModel getAlertList()
    {
        return privateAlertList;
    }

    private void setAlertList(AlertListModel value)
    {
        privateAlertList = value;
    }
    public SearchSuggestModel getAutoCompleteModel()
    {
        return privateAutoCompleteModel;
    }

    private void setAutoCompleteModel(SearchSuggestModel value)
    {
        privateAutoCompleteModel = value;
    }

    public String getSearchStringPrefix()
    {
        return searchStringPrefix;
    }

    public void setSearchStringPrefix(String value)
    {
        if (!ObjectUtils.objectsEqual(searchStringPrefix, value))
        {
            searchStringPrefix = value;
            searchStringPrefixChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchStringPrefix")); //$NON-NLS-1$
        }
    }

    public boolean getHasSearchStringPrefix()
    {
        return hasSearchStringPrefix;
    }

    private void setHasSearchStringPrefix(boolean value)
    {
        if (hasSearchStringPrefix != value)
        {
            hasSearchStringPrefix = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSearchStringPrefix")); //$NON-NLS-1$
        }
    }

    public String getSearchString()
    {
        return searchString;
    }

    public void setSearchString(String value)
    {
        setSearchString(value, true);
    }

    public void setSearchString(String value, boolean checkIfNewValue)
    {
        if (!checkIfNewValue || !ObjectUtils.objectsEqual(searchString, value))
        {
            searchString = value;
            searchStringChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchString")); //$NON-NLS-1$
        }
    }

    public DbUser getLoggedInUser()
    {
        return loggedInUser;
    }

    public void setLoggedInUser(DbUser value)
    {
        if (loggedInUser != value)
        {
            loggedInUser = value;
            onPropertyChanged(new PropertyChangedEventArgs("LoggedInUser")); //$NON-NLS-1$
        }
    }

    public List<AuditLog> getEvents()
    {
        return privateEvents;
    }

    public void setEvents(List<AuditLog> value)
    {
        privateEvents = value;
    }

    public AuditLog getLastEvent()
    {
        return lastEvent;
    }

    public void setLastEvent(AuditLog value)
    {
        if (lastEvent != value)
        {
            lastEvent = value;
            onPropertyChanged(new PropertyChangedEventArgs("LastEvent")); //$NON-NLS-1$
        }
    }

    public AuditLog getLastAlert()
    {
        return lastAlert;
    }

    public void setLastAlert(AuditLog value)
    {
        if (lastAlert != value)
        {
            lastAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("LastAlert")); //$NON-NLS-1$
        }
    }

    public boolean getHasSelectedTags()
    {
        return hasSelectedTags;
    }

    public void setHasSelectedTags(boolean value)
    {
        if (hasSelectedTags != value)
        {
            hasSelectedTags = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSelectedTags")); //$NON-NLS-1$
        }
    }

}
