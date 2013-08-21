package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
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

    public Iterable getExcludeItems()
    {
        return privateExcludeItems;
    }

    public void setExcludeItems(Iterable value)
    {
        privateExcludeItems = value;
    }

    private ListModel privateDomain;

    public ListModel getDomain()
    {
        return privateDomain;
    }

    private void setDomain(ListModel value)
    {
        privateDomain = value;
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
        setDomain(new ListModel());

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


        AsyncDataProvider.getDomainList(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object result) {
                populateDomains((List<String>) result);
            }
        }), false);


        AsyncDataProvider.getRoleList(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object result) {
                populateRoles((List<Role>) result);

            }
        }));
    }

    protected void populateDomains(List<String> domains){
        getDomain().setItems(domains);
        getDomain().setSelectedItem(Linq.firstOrDefault(domains));
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

                HashSet<Guid> excludeUsers = new HashSet<Guid>();
                if (adElementListModel.getExcludeItems() != null)
                {
                    for (Object item : adElementListModel.getExcludeItems())
                    {
                        DbUser a = (DbUser) item;

                        excludeUsers.add(a.getId());
                    }
                }
                adElementListModel.setgroups(new ArrayList<EntityModel>());
                for (IVdcQueryable item : (ArrayList<IVdcQueryable>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    LdapGroup a = (LdapGroup) item;
                    if (!excludeUsers.contains(a.getid()))
                    {
                        DbUser tempVar3 = new DbUser();
                        tempVar3.setId(a.getid());
                        tempVar3.setFirstName(a.getname());
                        tempVar3.setLastName(""); //$NON-NLS-1$
                        tempVar3.setLoginName(""); //$NON-NLS-1$
                        tempVar3.setDomain(a.getdomain());
                        DbUser user = tempVar3;

                        EntityModel tempVar4 = new EntityModel();
                        tempVar4.setEntity(user);
                        adElementListModel.getgroups().add(tempVar4);
                    }
                }

                onUserAndAdGroupsLoaded(adElementListModel);
            }
        };

        findGroups("name=" + (StringHelper.isNullOrEmpty(getSearchString()) ? "*" : getSearchString()), //$NON-NLS-1$ //$NON-NLS-2$
                _asyncQuery);
    }

    protected void addUsersToModel(VdcQueryReturnValue returnValue, Set<Guid> excludeUsers) {
        for (IVdcQueryable item : (ArrayList<IVdcQueryable>) returnValue.getReturnValue()) {
            LdapUser a = (LdapUser) item;
            if (!excludeUsers.contains(a.getUserId())) {
                EntityModel tempVar2 = new EntityModel();
                tempVar2.setEntity(new DbUser(a));
                getusers().add(tempVar2);
            }
        }
    }

    protected Set<Guid> getExcludeUsers() {
        Set<Guid> excludeUsers = new HashSet<Guid>();
        if (getExcludeItems() != null) {
            for (Object item : getExcludeItems()) {
                DbUser a = (DbUser) item;
                excludeUsers.add(a.getId());
            }
        }
        return excludeUsers;
    }

    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("ADGROUP@" + getDomain().getSelectedItem() + ": " + searchString, SearchType.AdGroup), query); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("ADUSER@" + getDomain().getSelectedItem() + ": " + searchString, SearchType.AdUser), query); //$NON-NLS-1$ //$NON-NLS-2$
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
            model.setMessage(Frontend.getAppErrorsTranslator()
                    .TranslateErrorTextSingle(returnValue.getExceptionString()));
            getSearchInProgress().setEntity(false);

            return true;
        }

        return false;
    }

}
