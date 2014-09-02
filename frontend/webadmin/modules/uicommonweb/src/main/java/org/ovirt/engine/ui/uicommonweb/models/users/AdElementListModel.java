package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class AdElementListModel extends SearchableListModel
{

    private EntityModel searchInProgress;

    public EntityModel getSearchInProgress() {
        return searchInProgress;
    }

    public void setSearchInProgress(EntityModel searchInProgress) {
        this.searchInProgress = searchInProgress;
    }

    private Iterable privateExcludeItems;

    private HashMap<String, List<String>> namespacesMap = new HashMap<String, List<String>>();

    public Iterable getExcludeItems()
    {
        return privateExcludeItems;
    }

    public void setExcludeItems(Iterable value)
    {
        privateExcludeItems = value;
    }

    private ListModel privateProfile;

    public ListModel getProfile()
    {
        return privateProfile;
    }

    private void setProfile(ListModel value)
    {
        privateProfile = value;
    }

    private ListModel<String> privateNamespace;

    public void setNamespace(ListModel<String> value) {
        privateNamespace = value;
    }

    public ListModel<String> getNamespace() {
        return privateNamespace;
    }

    private ListModel privateRole;

    public ListModel getRole()
    {
        return privateRole;
    }

    private void setRole(ListModel value)
    {
        privateRole = value;
    }

    private EntityModel privateSelectAll;

    public EntityModel getSelectAll()
    {
        return privateSelectAll;
    }

    public void setSelectAll(EntityModel value)
    {
        privateSelectAll = value;
    }

    // This is required for the webadmin.
    private EntityModel privateIsRoleListHiddenModel;

    public EntityModel getIsRoleListHiddenModel()
    {
        return privateIsRoleListHiddenModel;
    }

    private void setIsRoleListHiddenModel(EntityModel value)
    {
        privateIsRoleListHiddenModel = value;
    }

    private EntityModel privateIsEveryoneSelectionHidden;

    public EntityModel getIsEveryoneSelectionHidden()
    {
        return privateIsEveryoneSelectionHidden;
    }

    private void setIsEveryoneSelectionHidden(EntityModel value)
    {
        privateIsEveryoneSelectionHidden = value;
    }

    private boolean isRoleListHidden;

    public boolean getIsRoleListHidden()
    {
        return isEveryoneSelected;
    }

    public void setIsRoleListHidden(boolean value)
    {
        if (isRoleListHidden != value)
        {
            isRoleListHidden = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsRoleListHidden")); //$NON-NLS-1$
        }
        if (getIsRoleListHiddenModel() != null)
        {
            getIsRoleListHiddenModel().setEntity(value);
        }
    }

    private boolean isEveryoneSelected;

    private List<ProfileEntry> profileEntries;

    public boolean getIsEveryoneSelected()
    {
        return isEveryoneSelected;
    }

    public void setIsEveryoneSelected(boolean value)
    {
        if (isEveryoneSelected != value)
        {
            isEveryoneSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsEveryoneSelected")); //$NON-NLS-1$
        }
    }

    public AdElementListModel()
    {
        setRole(new ListModel());
        setProfile(new ListModel());
        setNamespace(new ListModel());

        setSelectAll(new EntityModel());
        getSelectAll().setEntity(false);
        getSelectAll().getEntityChangedEvent().addListener(this);

        setIsRoleListHiddenModel(new EntityModel());
        getIsRoleListHiddenModel().setEntity(false);

        setIsEveryoneSelectionHidden(new EntityModel());
        getIsEveryoneSelectionHidden().setEntity(false);

        setSearchInProgress(new EntityModel());
        getSearchInProgress().setEntity(false);

        setIsTimerDisabled(true);


        AsyncDataProvider.getAAAProfilesEntriesList(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object result) {
                setProfileEntries((List<ProfileEntry>) result);
                AsyncDataProvider.getAAANamespaces(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object result) {
                        namespacesMap = (HashMap<String, List<String>>) result;
                        populateProfiles(getProfileEntries());
                    }
                }));
            }

        }));

        AsyncDataProvider.getRoleList(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object result) {
                populateRoles((List<Role>) result);

            }
        }));
    }

    protected void populateProfiles(List<ProfileEntry> profiles) {
        getProfile().setItems(profiles);
        getProfile().setSelectedItem(Linq.firstOrDefault(getProfile().getItems()));
    }

    public void populateNamespaces() {
        if (namespacesMap != null) {
            getNamespace().setItems(getAuthzNamespaces());
            getNamespace().setSelectedItem(Linq.firstOrDefault(getNamespace().getItems()));

        }
    }

    protected void setProfileEntries(List<ProfileEntry> value) {
        profileEntries = value;
    }

    protected List<ProfileEntry> getProfileEntries() {
        return profileEntries;

    }

    protected void populateRoles(List<Role> roles){
        Role selectedRole = null;
        List<Role> rolesToPopulate = new ArrayList<Role>();

        for (Role role : roles) {

            if (role.getId() != null) {
             // ignore CONSUME_QUOTA_ROLE in UI
                if (!role.getId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                    rolesToPopulate.add(role);
                }
                //select engine user if it exists
                if (role.getId().equals(ApplicationGuids.engineUser.asGuid())) {
                    selectedRole = role;
                }
            }
        }

        Collections.sort(rolesToPopulate, new Linq.RoleNameComparer());

        getRole().setItems(rolesToPopulate);
        if (selectedRole != null) {
            getRole().setSelectedItem(selectedRole);
        } else if (rolesToPopulate.size() > 0){
            //if engine user does not exist, pick the first on the list
            getRole().setSelectedItem(rolesToPopulate.get(0));
        }
    }

    @Override
    protected void syncSearch()
    {
        // allow only a single user lookup at a time
        if ((Boolean) getSearchInProgress().getEntity()) {
            return;
        }
        getSearchInProgress().setEntity(true);

        super.syncSearch();
        // var exclude = ExcludeItems != null ? ExcludeItems.Cast<DbUser>() : new List<DbUser>();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                AdElementListModel adElementListModel = (AdElementListModel) model;
                VdcQueryReturnValue queryReturnValue = (VdcQueryReturnValue) ReturnValue;
                if (handleQueryError(queryReturnValue, adElementListModel)) {
                    return;
                }

                setusers(new ArrayList<EntityModel>());
                addUsersToModel(queryReturnValue, getExcludeUsers());
                onUserAndAdGroupsLoaded(adElementListModel);
            }
        };

        findUsers("allnames=" //$NON-NLS-1$
                + (StringHelper.isNullOrEmpty(getSearchString()) ? "*" : getSearchString()), //$NON-NLS-1$
                _asyncQuery);

        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                AdElementListModel adElementListModel = (AdElementListModel) model;
                VdcQueryReturnValue queryReturnValue = (VdcQueryReturnValue) ReturnValue;
                if (handleQueryError(queryReturnValue, adElementListModel)) {
                    return;
                }

                HashSet<String> excludeUsers = new HashSet<String>();
                if (adElementListModel.getExcludeItems() != null)
                {
                    for (Object item : adElementListModel.getExcludeItems())
                    {
                        DbUser a = (DbUser) item;

                        excludeUsers.add(a.getExternalId());
                    }
                }
                adElementListModel.setgroups(new ArrayList<EntityModel>());
                addGroupsToModel(queryReturnValue, excludeUsers);
                onUserAndAdGroupsLoaded(adElementListModel);
            }
        };

        findGroups("name=" + (StringHelper.isNullOrEmpty(getSearchString()) ? "*" : getSearchString()), //$NON-NLS-1$ //$NON-NLS-2$
                _asyncQuery);
    }

    protected void addUsersToModel(VdcQueryReturnValue returnValue, Set<String> excludeUsers) {
        for (IVdcQueryable item : (List<IVdcQueryable>) returnValue.getReturnValue()) {
            DirectoryUser a = (DirectoryUser) item;
            if (!excludeUsers.contains(a.getId())) {
                EntityModel tempVar2 = new EntityModel();
                tempVar2.setEntity(new DbUser(a));
                getusers().add(tempVar2);
            }
        }
    }

    protected void addGroupsToModel(VdcQueryReturnValue returnValue, Set<String> excludeUsers) {
        for (IVdcQueryable item : (Collection<IVdcQueryable>) returnValue.getReturnValue())
        {
            DirectoryGroup a = (DirectoryGroup) item;
            if (!excludeUsers.contains(a.getId()))
            {
                // XXX: This should use DbGroup and not DbUser.
                DbUser tempVar3 = new DbUser();
                tempVar3.setExternalId(a.getId());
                tempVar3.setFirstName(a.getName());
                tempVar3.setLastName(""); //$NON-NLS-1$
                tempVar3.setLoginName(""); //$NON-NLS-1$
                tempVar3.setDomain(a.getDirectoryName());
                tempVar3.setNamespace(a.getNamespace());
                DbUser user = tempVar3;

                EntityModel tempVar4 = new EntityModel();
                tempVar4.setEntity(user);
                getgroups().add(tempVar4);
            }
        }
    }
    protected Set<String> getExcludeUsers() {
        Set<String> excludeUsers = new HashSet<String>();
        if (getExcludeItems() != null) {
            for (Object item : getExcludeItems()) {
                DbUser a = (DbUser) item;
                excludeUsers.add(a.getExternalId());
            }
        }
        return excludeUsers;
    }

    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.getInstance()
                .runQuery(VdcQueryType.Search,
                        new SearchParameters("ADGROUP@" + ((ProfileEntry) getProfile().getSelectedItem()).getAuthz() + ":" + getNamespace().getSelectedItem() + ": " + searchString, SearchType.DirectoryGroup), query); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.getInstance()
                .runQuery(VdcQueryType.Search,
                        new SearchParameters("ADUSER@" + ((ProfileEntry) getProfile().getSelectedItem()).getAuthz() + ":" + getNamespace().getSelectedItem() + ": " + searchString, SearchType.DirectoryUser), query); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected void onUserAndAdGroupsLoaded(AdElementListModel adElementListModel)
    {
        if (adElementListModel.getusers() != null && adElementListModel.getgroups() != null)
        {
            getSearchInProgress().setEntity(false);

            ArrayList<EntityModel> items = new ArrayList<EntityModel>();
            items.addAll(getusers());
            items.addAll(getgroups());
            adElementListModel.getSelectAll().setEntity(false);
            adElementListModel.setItems(items);
            setusers(null);
            setgroups(getusers());

            setIsEmpty(items.isEmpty());
        }
    }

    private ArrayList<EntityModel> privateusers;

    public ArrayList<EntityModel> getusers()
    {
        return privateusers;
    }

    public void setusers(ArrayList<EntityModel> value)
    {
        privateusers = value;
    }

    private ArrayList<EntityModel> privategroups;

    public ArrayList<EntityModel> getgroups()
    {
        return privategroups;
    }

    public void setgroups(ArrayList<EntityModel> value)
    {
        privategroups = value;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (sender == getSelectAll())
        {
            if (getItems() == null)
            {
                return;
            }
            boolean selectAll = (Boolean) getSelectAll().getEntity();
            EntityModel entityModel;
            for (Object item : getItems())
            {
                entityModel = (EntityModel) item;
                entityModel.setIsSelected(selectAll);
            }
        }
    }

    public boolean availableNamespaces() {
        return getAuthzNamespaces() != null && !getAuthzNamespaces().isEmpty();
    }

    @Override
    protected String getListName() {
        return "AdElementListModel"; //$NON-NLS-1$
    }

    /**
     * Handle error message in case of a query failure
     * @param returnValue query return value
     * @param model the model being currently displayed
     * @return true if a query failure has occurred
     */
    private boolean handleQueryError(VdcQueryReturnValue returnValue, AdElementListModel model) {
        model.setMessage(null);
        if (!returnValue.getSucceeded()) {
            model.setMessage(Frontend.getInstance().getAppErrorsTranslator()
                    .translateErrorTextSingle(returnValue.getExceptionString()));
            getSearchInProgress().setEntity(false);

            return true;
        }

        return false;
    }

    private List<String> getAuthzNamespaces() {
        ProfileEntry profileEntry = (ProfileEntry) getProfile().getSelectedItem();
        return profileEntry != null ? namespacesMap.get(profileEntry.getAuthz()) : Collections.<String> emptyList();
    }

}
