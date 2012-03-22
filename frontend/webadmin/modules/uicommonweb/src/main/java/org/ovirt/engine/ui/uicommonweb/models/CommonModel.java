package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Configurator.GlusterModeEnum;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SearchSuggestModel;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.common.AboutModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.SystemPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;

@SuppressWarnings("unused")
public class CommonModel extends ListModel
{

    public static EventDefinition SignedOutEventDefinition;
    private Event privateSignedOutEvent;

    public Event getSignedOutEvent()
    {
        return privateSignedOutEvent;
    }

    private void setSignedOutEvent(Event value)
    {
        privateSignedOutEvent = value;
    }

    private UICommand privateSearchCommand;

    public UICommand getSearchCommand()
    {
        return privateSearchCommand;
    }

    private void setSearchCommand(UICommand value)
    {
        privateSearchCommand = value;
    }

    private UICommand privateConfigureCommand;

    public UICommand getConfigureCommand()
    {
        return privateConfigureCommand;
    }

    private void setConfigureCommand(UICommand value)
    {
        privateConfigureCommand = value;
    }

    private UICommand privateAboutCommand;

    public UICommand getAboutCommand()
    {
        return privateAboutCommand;
    }

    private void setAboutCommand(UICommand value)
    {
        privateAboutCommand = value;
    }

    private UICommand privateSignOutCommand;

    public UICommand getSignOutCommand()
    {
        return privateSignOutCommand;
    }

    private void setSignOutCommand(UICommand value)
    {
        privateSignOutCommand = value;
    }

    private UICommand privateClearSearchStringCommand;

    public UICommand getClearSearchStringCommand()
    {
        return privateClearSearchStringCommand;
    }

    private void setClearSearchStringCommand(UICommand value)
    {
        privateClearSearchStringCommand = value;
    }

    @Override
    public java.util.List<SearchableListModel> getItems()
    {
        return (java.util.List<SearchableListModel>) super.getItems();
    }

    public void setItems(java.util.List<SearchableListModel> value)
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

    private BookmarkListModel privateBookmarkList;

    public BookmarkListModel getBookmarkList()
    {
        return privateBookmarkList;
    }

    private void setBookmarkList(BookmarkListModel value)
    {
        privateBookmarkList = value;
    }

    private TagListModel privateTagList;

    public TagListModel getTagList()
    {
        return privateTagList;
    }

    private void setTagList(TagListModel value)
    {
        privateTagList = value;
    }

    private SystemTreeModel privateSystemTree;

    public SystemTreeModel getSystemTree()
    {
        return privateSystemTree;
    }

    private void setSystemTree(SystemTreeModel value)
    {
        privateSystemTree = value;
    }

    private SearchableListModel privateEventList;

    public SearchableListModel getEventList()
    {
        return privateEventList;
    }

    private void setEventList(SearchableListModel value)
    {
        privateEventList = value;
    }

    private TaskListModel privateTaskList;

    public TaskListModel getTaskList() {
        return privateTaskList;
    }

    public void setTaskList(TaskListModel taskList) {
        this.privateTaskList = taskList;
    }

    private AlertListModel privateAlertList;

    public AlertListModel getAlertList()
    {
        return privateAlertList;
    }

    private void setAlertList(AlertListModel value)
    {
        privateAlertList = value;
    }

    private SearchSuggestModel privateAutoCompleteModel;

    public SearchSuggestModel getAutoCompleteModel()
    {
        return privateAutoCompleteModel;
    }

    private void setAutoCompleteModel(SearchSuggestModel value)
    {
        privateAutoCompleteModel = value;
    }

    private String searchStringPrefix;

    public String getSearchStringPrefix()
    {
        return searchStringPrefix;
    }

    public void setSearchStringPrefix(String value)
    {
        if (!StringHelper.stringsEqual(searchStringPrefix, value))
        {
            searchStringPrefix = value;
            SearchStringPrefixChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("SearchStringPrefix"));
        }
    }

    private boolean hasSearchStringPrefix;

    public boolean getHasSearchStringPrefix()
    {
        return hasSearchStringPrefix;
    }

    private void setHasSearchStringPrefix(boolean value)
    {
        if (hasSearchStringPrefix != value)
        {
            hasSearchStringPrefix = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasSearchStringPrefix"));
        }
    }

    private String searchString;

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
        if (!checkIfNewValue || !StringHelper.stringsEqual(searchString, value))
        {
            searchString = value;
            SearchStringChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("SearchString"));
        }
    }

    private VdcUser loggedInUser;

    public VdcUser getLoggedInUser()
    {
        return loggedInUser;
    }

    public void setLoggedInUser(VdcUser value)
    {
        if (loggedInUser != value)
        {
            loggedInUser = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LoggedInUser"));
        }
    }

    private java.util.List<AuditLog> privateEvents;

    public java.util.List<AuditLog> getEvents()
    {
        return privateEvents;
    }

    public void setEvents(java.util.List<AuditLog> value)
    {
        privateEvents = value;
    }

    private AuditLog lastEvent;

    public AuditLog getLastEvent()
    {
        return lastEvent;
    }

    public void setLastEvent(AuditLog value)
    {
        if (lastEvent != value)
        {
            lastEvent = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LastEvent"));
        }
    }

    private AuditLog lastAlert;

    public AuditLog getLastAlert()
    {
        return lastAlert;
    }

    public void setLastAlert(AuditLog value)
    {
        if (lastAlert != value)
        {
            lastAlert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LastAlert"));
        }
    }

    private boolean hasSelectedTags;

    public boolean getHasSelectedTags()
    {
        return hasSelectedTags;
    }

    public void setHasSelectedTags(boolean value)
    {
        if (hasSelectedTags != value)
        {
            hasSelectedTags = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasSelectedTags"));
        }
    }

    static
    {
        SignedOutEventDefinition = new EventDefinition("SingedOut", CommonModel.class);
    }

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
        setSignedOutEvent(new Event(SignedOutEventDefinition));

        UICommand tempVar = new UICommand("Search", this);
        tempVar.setIsDefault(true);
        setSearchCommand(tempVar);
        setAboutCommand(new UICommand("About", this));
        setSignOutCommand(new UICommand("SignOut", this));
        setConfigureCommand(new UICommand("Configure", this));
        setClearSearchStringCommand(new UICommand("ClearSearchString", this));

        setAutoCompleteModel(new SearchSuggestModel());

        setBookmarkList(new BookmarkListModel());
        getBookmarkList().getNavigatedEvent().addListener(this);

        setTagList(new TagListModel());
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        setSystemTree(new SystemTreeModel());
        getSystemTree().getSelectedItemChangedEvent().addListener(this);
        getSystemTree().getSearchCommand().Execute();

        setEventList(new EventListModel());
        getEventList().getSearchCommand().Execute();

        setAlertList(new AlertListModel());
        getAlertList().getSearchCommand().Execute();

        setTaskList(new TaskListModel());
        getTaskList().getSearchCommand().Execute();

        InitItems();

        setLoggedInUser(Frontend.getLoggedInUser());
    }

    private void UpdateHasSelectedTags()
    {
        java.util.ArrayList<TagModel> selectedTags =
                getTagList().getSelectedItems() != null ? Linq.<TagModel> Cast(getTagList().getSelectedItems())
                        : new java.util.ArrayList<TagModel>();

        setHasSelectedTags(getSelectedItem() != null && selectedTags.size() > 0);
    }

    private void TagListModel_SelectedItemsChanged(Object sender, EventArgs e)
    {
        // Reset system tree to the root item.
        getSystemTree().getSelectedItemChangedEvent().removeListener(this);
        getSystemTree().getResetCommand().Execute();
        getSystemTree().getSelectedItemChangedEvent().addListener(this);

        boolean hadSelectedTags = getHasSelectedTags();
        UpdateHasSelectedTags();

        dataCenterList.setIsAvailable(!getHasSelectedTags());
        clusterList.setIsAvailable(!getHasSelectedTags());
        hostList.setIsAvailable(true);
        storageList.setIsAvailable(!getHasSelectedTags());
        vmList.setIsAvailable(true);

        if (poolList != null)
        {
            poolList.setIsAvailable(!getHasSelectedTags());
        }

        templateList.setIsAvailable(!getHasSelectedTags());
        userList.setIsAvailable(true);
        eventList.setIsAvailable(!getHasSelectedTags());
        reportsList.setIsAvailable(ReportInit.getInstance().isReportsEnabled() && !getHasSelectedTags());

        /*
         * --- JUICOMMENT_BEGIN monitor.setIsAvailable(!getHasSelectedTags()); JUICOMMENT_END ---
         */

        // Switch the selected item as neccessary.
        ListModel oldSelectedItem = getSelectedItem();
        if (getHasSelectedTags() && oldSelectedItem != hostList && oldSelectedItem != vmList
                && oldSelectedItem != userList)
        {
            setSelectedItem(vmList);
        }
        // Update search string only when selecting or de-selecting tags
        else if (getHasSelectedTags() || hadSelectedTags)
        {
            String prefix = "";
            String search = "";
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            SplitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().Execute();
            SearchStringChanged();
        }
    }

    private void BookmarkListModel_Navigated(Object sender, BookmarkEventArgs e)
    {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().Execute();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        // Reset system tree to the root item.
        getSystemTree().getSelectedItemChangedEvent().removeListener(this);
        getSystemTree().getResetCommand().Execute();
        getSystemTree().getSelectedItemChangedEvent().addListener(this);

        for (SearchableListModel item : getItems())
        {
            item.setIsAvailable(true);
        }

        setSearchStringPrefix("");
        setSearchString(e.getBookmark().getbookmark_value());
        getSearchCommand().Execute();
    }

    public String getEffectiveSearchString()
    {
        return getSearchStringPrefix() + getSearchString();
    }

    private void SystemTree_ItemChanged(Object sender, EventArgs args)
    {
        // Reset tags tree to the root item.
        getTagList().getSelectedItemsChangedEvent().removeListener(this);
        getTagList().getResetCommand().Execute();
        UpdateHasSelectedTags();
        getTagList().getSelectedItemsChangedEvent().addListener(this);

        SystemTreeItemModel model = (SystemTreeItemModel) getSystemTree().getSelectedItem();
        if (model == null)
        {
            return;
        }

        // Update items availability depending on system tree selection.
        dataCenterList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Storage || model.getType() == SystemTreeItemType.System);

        clusterList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Clusters || model.getType() == SystemTreeItemType.Cluster
                || model.getType() == SystemTreeItemType.Storage || model.getType() == SystemTreeItemType.System);

        hostList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Cluster || model.getType() == SystemTreeItemType.Hosts
                || model.getType() == SystemTreeItemType.Host || model.getType() == SystemTreeItemType.Storage
                || model.getType() == SystemTreeItemType.System);

        storageList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Cluster || model.getType() == SystemTreeItemType.Host
                || model.getType() == SystemTreeItemType.Storages || model.getType() == SystemTreeItemType.Storage
                || model.getType() == SystemTreeItemType.System);

        boolean isDataStorage = false;
        if (model.getType() == SystemTreeItemType.Storage)
        {
            storage_domains storage = (storage_domains) model.getEntity();
            isDataStorage =
                    storage.getstorage_domain_type() == StorageDomainType.Data
                            || storage.getstorage_domain_type() == StorageDomainType.Master;
        }

        vmList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Cluster || model.getType() == SystemTreeItemType.Host
                || isDataStorage || model.getType() == SystemTreeItemType.VMs
                || model.getType() == SystemTreeItemType.System);

        if (poolList != null)
        {
            poolList.setIsAvailable(model.getType() == SystemTreeItemType.System);
        }

        templateList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Cluster || model.getType() == SystemTreeItemType.Host
                || isDataStorage || model.getType() == SystemTreeItemType.Templates
                || model.getType() == SystemTreeItemType.System);

        userList.setIsAvailable(model.getType() == SystemTreeItemType.System);
        /*
         * --- JUICOMMENT_BEGIN monitor.setIsAvailable(model.getType() == SystemTreeItemType.System); JUICOMMENT_END ---
         */
        eventList.setIsAvailable(model.getType() == SystemTreeItemType.DataCenter
                || model.getType() == SystemTreeItemType.Cluster || model.getType() == SystemTreeItemType.Host
                || model.getType() == SystemTreeItemType.Storage || model.getType() == SystemTreeItemType.System);

        reportsList.setIsAvailable(ReportInit.getInstance().isReportsEnabled()
                && (model.getType() == SystemTreeItemType.System
                        || model.getType() == SystemTreeItemType.DataCenter || model.getType() == SystemTreeItemType.Clusters));

        // Select a default item depending on system tree selection.
        ListModel oldSelectedItem = getSelectedItem();

        // Do not Change Tab if the Selection is the Reports
        if (!reportsList.getIsAvailable() || getSelectedItem() != reportsList) {
            switch (model.getType())
            {
            case DataCenter:
                setSelectedItem(dataCenterList);
                break;
            case Clusters:
            case Cluster:
                setSelectedItem(clusterList);
                break;
            case Hosts:
            case Host:
                setSelectedItem(hostList);
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
            default:
                // webadmin: redirect to default tab in case no tab is selected.
                if (getSelectedItem() == null)
                {
                    setSelectedItem(getDefaultItem());
                }
                break;
            }
        } else {
            reportsList.refreshReportModel();
        }

        // Update search string if selected item was not changed. If it is,
        // search string will be updated in OnSelectedItemChanged method.
        if (getSelectedItem() == oldSelectedItem)
        {
            String prefix = "";
            String search = "";
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            SplitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSearchCommand().Execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext)
            {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem((SystemTreeItemModel) getSystemTree().getSelectedItem());
            }
        }
    }

    private void SearchStringChanged()
    {
        getBookmarkList().setSearchString(getEffectiveSearchString());
    }

    private void SearchStringPrefixChanged()
    {
        setHasSearchStringPrefix(!StringHelper.isNullOrEmpty(getSearchStringPrefix()));
        getAutoCompleteModel().setPrefix(getSearchStringPrefix());
    }

    private void ClearSearchString()
    {
        setSearchString(getHasSearchStringPrefix() ? null : getSelectedItem().getDefaultSearchString(), false);
        getSearchCommand().Execute();
    }

    public void About()
    {
        if (getWindow() != null)
        {
            return;
        }

        AboutModel model = new AboutModel();
        setWindow(model);
        model.setShowOnlyVersion(false);
        model.setTitle("About oVirt Engine");
        model.setHashName("about_rhev_manager");

        UICommand tempVar = new UICommand("Cancel", this);
        tempVar.setTitle("Close");
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    public void SignOut()
    {
        // Stop search on all list models.
        for (SearchableListModel listModel : getItems())
        {
            listModel.EnsureAsyncSearchStopped();
        }

        getEventList().EnsureAsyncSearchStopped();
        getAlertList().EnsureAsyncSearchStopped();
        getTaskList().EnsureAsyncSearchStopped();

        if (Frontend.getIsUserLoggedIn())
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setHandleFailure(true);
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue)
                {
                    CommonModel commonModel = (CommonModel) model;
                    commonModel.setLoggedInUser(null);
                    commonModel.getSignedOutEvent().raise(commonModel, EventArgs.Empty);
                }
            };

            Frontend.LogoffAsync(Frontend.getLoggedInUser(), _asyncQuery);
        }
    }

    public void Configure()
    {
        if (getWindow() != null)
        {
            return;
        }

        EntityModel model = new EntityModel();
        setWindow(model);
        model.setTitle("Configure");
        model.setHashName("configure");
        model.setEntity(new Model[] { roleListModel, systemPermissionListModel });

        UICommand tempVar = new UICommand("Cancel", this);
        tempVar.setTitle("Close");
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    public void Cancel()
    {
        setWindow(null);
    }

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
    private SearchableListModel monitor;
    private SearchableListModel volumeList;

    private void InitItems()
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

        if (DataProvider.IsLicenseHasDesktops())
        {
            poolList = new PoolListModel();
            list.add(poolList);
        }

        templateList = new TemplateListModel();
        list.add(templateList);
        userList = new UserListModel();
        list.add(userList);
        /*
         * --- JUICOMMENT_BEGIN monitor = new MonitorModel(); list.add(monitor); JUICOMMENT_END ---
         */
        eventList = new EventListModel();
        list.add(eventList);

        reportsList = new ReportsListModel(ReportInit.getInstance().getReportBaseUrl());
        reportsList.setUser("ovirt");
        reportsList.setPassword("1234");
        list.add(reportsList);

        reportsList.setIsAvailable(false);
        quotaList = new QuotaListModel();
        list.add(quotaList);

        volumeList = new VolumeListModel();
        if (!volumeList.getGlusterModeEnum().equals(GlusterModeEnum.ONLY_OVIRT)) {
            list.add(volumeList);
        }

        setItems(list);

        roleListModel = new RoleListModel();
        systemPermissionListModel = new SystemPermissionListModel();

        // Activate the default list model.
        setSelectedItem(getDefaultItem());
    }

    private SearchableListModel getDefaultItem()
    {
        return vmList;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(SelectedItemsChangedEventDefinition) && sender == getTagList())
        {
            TagListModel_SelectedItemsChanged(sender, args);
        }
        else if (ev.equals(BookmarkListModel.NavigatedEventDefinition) && sender == getBookmarkList())
        {
            BookmarkListModel_Navigated(sender, (BookmarkEventArgs) args);
        }
        else if (ev.equals(SelectedItemChangedEventDefinition) && sender == getSystemTree())
        {
            SystemTree_ItemChanged(sender, args);
        }
    }

    private boolean executingSearch;
    private RoleListModel roleListModel;
    private SystemPermissionListModel systemPermissionListModel;

    @Override
    protected void OnSelectedItemChanging(Object newValue, Object oldValue)
    {
        super.OnSelectedItemChanging(newValue, oldValue);

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

            oldModel.EnsureAsyncSearchStopped();
        }
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();

        if (!executingSearch && getSelectedItem() != null)
        {
            // Split search string as necessary.
            String prefix = "";
            String search = "";
            RefObject<String> tempRef_prefix = new RefObject<String>(prefix);
            RefObject<String> tempRef_search = new RefObject<String>(search);
            SplitSearchString(getSelectedItem().getDefaultSearchString(), tempRef_prefix, tempRef_search);
            prefix = tempRef_prefix.argvalue;
            search = tempRef_search.argvalue;

            setSearchStringPrefix(prefix);
            setSearchString(search);

            getSelectedItem().setSearchString(getEffectiveSearchString());
            getSelectedItem().getSearchCommand().Execute();

            if (getSelectedItem() instanceof ISupportSystemTreeContext)
            {
                ISupportSystemTreeContext treeContext = (ISupportSystemTreeContext) getSelectedItem();
                treeContext.setSystemTreeSelectedItem((SystemTreeItemModel) getSystemTree().getSelectedItem());
            }
        }

        UpdateHasSelectedTags();
    }

    public void Search()
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
            if (a.IsSearchStringMatch(getEffectiveSearchString()))
            {
                model = a;
                break;
            }
        }

        if (model == null)
        {
            return;
        }

        // Transfer a search string to the model.
        model.setSearchString(getEffectiveSearchString());

        // Change active list model as neccesary.
        setSelectedItem(model);

        // Propagate search command to a concrete list model.
        getSelectedItem().getSearchCommand().Execute();

        executingSearch = false;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getSearchCommand())
        {
            Search();
        }
        else if (command == getAboutCommand())
        {
            About();
        }
        else if (command == getSignOutCommand())
        {
            SignOut();
        }
        else if (command == getConfigureCommand())
        {
            Configure();
        }
        else if (command == getClearSearchStringCommand())
        {
            ClearSearchString();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }

    /**
     * Splits a search string into two component, the prefix and a search string itself.
     */
    private void SplitSearchString(String source, RefObject<String> prefix, RefObject<String> search)
    {
        java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>) getTagList().getSelectedItems();
        SystemTreeItemModel model = (SystemTreeItemModel) getSystemTree().getSelectedItem();

        prefix.argvalue = "";

        // Split for tags.
        if (tags != null && tags.size() > 0)
        {
            Regex regex = new Regex("tag\\s*=\\s*(?:[\\w-]+)(?:\\sor\\s)?", RegexOptions.IgnoreCase);

            String[] array = source.split("[:]", -1);
            String entityClause = array[0];
            String searchClause = array[1];

            String tagsClause = "";
            for (TagModel tag : tags)
            {
                tagsClause += ("tag=" + tag.getName().getEntity());
                if (tag != tags.get(tags.size() - 1))
                {
                    tagsClause += " or ";
                }
            }

            prefix.argvalue = StringFormat.format("%1$s: %2$s ", entityClause, tagsClause);

            search.argvalue = regex.replace(searchClause, "").trim();
        }
        // Split for system tree.
        else if (model != null && model.getType() != SystemTreeItemType.System)
        {
            getAutoCompleteModel().setFilter(new String[] { "or", "and" });

            switch (model.getType())
            {
            case DataCenter: {
                if (dataCenterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("DataCenter: name = %1$s", model.getTitle());
                }
                else if (clusterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Cluster: datacenter.name = %1$s", model.getTitle());
                }
                else if (hostList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Host: datacenter = %1$s", model.getTitle());
                }
                else if (storageList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Storage: datacenter = %1$s", model.getTitle());
                }
                else if (vmList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Vms: datacenter = %1$s", model.getTitle());
                }
                else if (templateList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Template: datacenter = %1$s", model.getTitle());
                }
                else if (eventList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Events: event_datacenter = %1$s", model.getTitle());
                }
            }
                break;
            case Clusters: {
                if (clusterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue =
                            StringFormat.format("Cluster: datacenter.name = %1$s", model.getParent().getTitle());
                }
            }
                break;
            case Cluster: {
                if (clusterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Cluster: name = %1$s", model.getTitle());
                }
                else if (hostList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Host: cluster = %1$s", model.getTitle());
                }
                else if (storageList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Storage: cluster.name = %1$s", model.getTitle());
                }
                else if (vmList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Vms: cluster = %1$s", model.getTitle());
                }
                else if (templateList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Template: cluster = %1$s", model.getTitle());
                }
                else if (eventList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Events: cluster = %1$s", model.getTitle());
                }
            }
                break;
            case Hosts: {
                if (hostList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Host: cluster = %1$s", model.getParent().getTitle());
                }
            }
                break;
            case Host: {
                if (hostList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Host: name = %1$s", model.getTitle());
                }
                else if (storageList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Storage: host.name = %1$s", model.getTitle());
                }
                else if (vmList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Vms: Hosts.name = %1$s", model.getTitle());
                }
                else if (templateList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Template: Hosts.name = %1$s", model.getTitle());
                }
                else if (eventList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Events: host.name = %1$s", model.getTitle());
                }
            }
                break;
            case Storages: {
                if (storageList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Storage: datacenter = %1$s", model.getParent().getTitle());
                }
            }
                break;
            case Storage: {
                if (dataCenterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("DataCenter: storage.name = %1$s", model.getTitle());
                }
                else if (clusterList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Cluster: storage.name = %1$s", model.getTitle());
                }
                else if (hostList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Host: storage.name = %1$s", model.getTitle());
                }
                else if (storageList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Storage: name = %1$s", model.getTitle());
                }
                else if (vmList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Vms: storage.name = %1$s", model.getTitle());
                }
                else if (templateList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Templates: storage.name = %1$s", model.getTitle());
                }
                else if (eventList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Events: event_storage = %1$s", model.getTitle());
                }
            }
                break;
            case Templates: {
                if (templateList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Template: datacenter = %1$s", model.getParent().getTitle());
                }
            }
                break;
            case VMs: {
                if (vmList.IsSearchStringMatch(source))
                {
                    prefix.argvalue = StringFormat.format("Vms: cluster = %1$s", model.getParent().getTitle());
                }
            }
                break;
            }

            prefix.argvalue = prefix.argvalue + " ";
            search.argvalue = "";
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
}
