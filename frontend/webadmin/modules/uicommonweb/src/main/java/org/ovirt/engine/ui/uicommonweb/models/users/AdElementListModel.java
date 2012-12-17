package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.HashSet;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
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

public class AdElementListModel extends SearchableListModel
{

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
            OnPropertyChanged(new PropertyChangedEventArgs("IsRoleListHidden")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsEveryoneSelected")); //$NON-NLS-1$
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

        setIsTimerDisabled(true);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                AdElementListModel adElementListModel = (AdElementListModel) model;
                ArrayList<String> domains = (ArrayList<String>) result;
                adElementListModel.getDomain().setItems(domains);
                adElementListModel.getDomain().setSelectedItem(Linq.FirstOrDefault(domains));
                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.setModel(adElementListModel);
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model1, Object result1)
                    {
                        AdElementListModel adElementListModel1 = (AdElementListModel) model1;
                        Role roleValue = null;
                        boolean first = true;
                        ArrayList<Role> roles = (ArrayList<Role>) result1;
                        ArrayList<Role> newRoles = new ArrayList<Role>();
                        for (Role r : roles) {
                            // ignore CONSUME_QUOTA_ROLE in UI
                            if (!r.getId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                                newRoles.add(r);
                            }
                        }
                        for (Role r : roles)
                        {
                            if (first)
                            {
                                roleValue = r;
                                first = false;
                            }
                            if (r.getId() != null && r.getId().equals(ApplicationGuids.engineUser.asGuid())) //$NON-NLS-1$
                            {
                                roleValue = r;
                                break;
                            }
                        }

                        adElementListModel1.getRole().setItems(newRoles);
                        adElementListModel1.getRole().setSelectedItem(roleValue);

                    }
                };
                AsyncDataProvider.GetRoleList(_asyncQuery1);
            }
        };
        AsyncDataProvider.GetDomainList(_asyncQuery, false);
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();
        // var exclude = ExcludeItems != null ? ExcludeItems.Cast<DbUser>() : new List<DbUser>();

        String domain = (String) getDomain().getSelectedItem();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                AdElementListModel adElementListModel = (AdElementListModel) model;
                HashSet<Guid> excludeUsers = new HashSet<Guid>();
                if (getExcludeItems() != null)
                {
                    for (Object item : getExcludeItems())
                    {
                        DbUser a = (DbUser) item;

                        excludeUsers.add(a.getuser_id());
                    }
                }
                setusers(new ArrayList<EntityModel>());
                for (IVdcQueryable item : (ArrayList<IVdcQueryable>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    LdapUser a = (LdapUser) item;
                    if (!excludeUsers.contains(a.getUserId()))
                    {
                        DbUser tempVar = new DbUser();
                        tempVar.setuser_id(a.getUserId());
                        tempVar.setIsGroup(false);
                        tempVar.setname(a.getName());
                        tempVar.setsurname(a.getSurName());
                        tempVar.setusername(a.getUserName());
                        tempVar.setdomain(a.getDomainControler());
                        DbUser user = tempVar;

                        EntityModel tempVar2 = new EntityModel();
                        tempVar2.setEntity(user);
                        getusers().add(tempVar2);
                    }
                }
                OnUserAndAdGroupsLoaded(adElementListModel);
            }
        };

        findUsers("allnames=" //$NON-NLS-1$
                + (StringHelper.isNullOrEmpty(getSearchString()) ? "*" : getSearchString()), //$NON-NLS-1$
                _asyncQuery);

        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                AdElementListModel adElementListModel = (AdElementListModel) model;
                HashSet<Guid> excludeUsers = new HashSet<Guid>();
                if (adElementListModel.getExcludeItems() != null)
                {
                    for (Object item : adElementListModel.getExcludeItems())
                    {
                        DbUser a = (DbUser) item;

                        excludeUsers.add(a.getuser_id());
                    }
                }
                adElementListModel.setgroups(new ArrayList<EntityModel>());
                for (IVdcQueryable item : (ArrayList<IVdcQueryable>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    LdapGroup a = (LdapGroup) item;
                    if (!excludeUsers.contains(a.getid()))
                    {
                        DbUser tempVar3 = new DbUser();
                        tempVar3.setuser_id(a.getid());
                        tempVar3.setIsGroup(true);
                        tempVar3.setname(a.getname());
                        tempVar3.setsurname(""); //$NON-NLS-1$
                        tempVar3.setusername(""); //$NON-NLS-1$
                        tempVar3.setdomain(a.getdomain());
                        DbUser user = tempVar3;

                        EntityModel tempVar4 = new EntityModel();
                        tempVar4.setEntity(user);
                        adElementListModel.getgroups().add(tempVar4);
                    }
                }

                OnUserAndAdGroupsLoaded(adElementListModel);
            }
        };

        findGroups("name=" + (StringHelper.isNullOrEmpty(getSearchString()) ? "*" : getSearchString()), //$NON-NLS-1$ //$NON-NLS-2$
                _asyncQuery);
    }

    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("ADGROUP@" + getDomain().getSelectedItem() + ": " + searchString, SearchType.AdGroup), query); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("ADUSER@" + getDomain().getSelectedItem() + ": " + searchString, SearchType.AdUser), query); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void OnUserAndAdGroupsLoaded(AdElementListModel adElementListModel)
    {
        if (adElementListModel.getusers() != null && adElementListModel.getgroups() != null)
        {
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

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
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
}
