package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class EventListModel extends SearchableListModel
{
    private final GridTimer timer;

    private UICommand privateRefreshCommand;

    public UICommand getRefreshCommand()
    {
        return privateRefreshCommand;
    }

    private void setRefreshCommand(UICommand value)
    {
        privateRefreshCommand = value;
    }

    private AuditLog lastEvent;

    public AuditLog getLastEvent()
    {
        return lastEvent;
    }

    private void setLastEvent(AuditLog value)
    {
        if (lastEvent != value)
        {
            lastEvent = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LastEvent"));
        }
    }

    private boolean isAdvancedView;

    public boolean getIsAdvancedView()
    {
        return isAdvancedView;
    }

    public void setIsAdvancedView(boolean value)
    {
        if (isAdvancedView != value)
        {
            isAdvancedView = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsAdvancedView"));
        }
    }

    public EventListModel()
    {
        setTitle("Events");

        setRefreshCommand(new UICommand("Refresh", this));

        setDefaultSearchString("Events:");
        setSearchString(getDefaultSearchString());

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        setIsTimerDisabled(true);

        timer = new GridTimer(getListName()) {

            @Override
            public void execute() {
                getRefreshCommand().Execute();
            }
        };

        timer.setRefreshRate(getConfigurator().getPollingTimerInterval());
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("event");
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        setItems(new ObservableCollection<AuditLog>());
        setLastEvent(null);
        timer.start();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    private void Refresh()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                EventListModel eventListModel = (EventListModel) model;
                java.util.ArrayList<AuditLog> list =
                        (java.util.ArrayList<AuditLog>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                eventListModel.UpdateItems(list);
            }
        };

        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.AuditLog);
        tempVar.setMaxCount(getSearchPageSize());
        tempVar.setSearchFrom(getLastEvent() != null ? getLastEvent().getaudit_log_id() : 0);
        tempVar.setRefresh(false);
        SearchParameters searchParameters = tempVar;

        Frontend.RunQuery(VdcQueryType.Search, searchParameters, _asyncQuery);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getRefreshCommand())
        {
            Refresh();
            UpdatePagingAvailability();
        }
    }

    @Override
    public void EnsureAsyncSearchStopped()
    {
        super.EnsureAsyncSearchStopped();

        timer.stop();
    }

    private void UpdateItems(java.util.ArrayList<AuditLog> source)
    {
        if (getItems() == null)
        {
            return;
        }

        java.util.List<AuditLog> list = (java.util.List<AuditLog>) getItems();

        Collections.sort(source, new Linq.AuditLogComparer());

        for (AuditLog item : source)
        {
            if (list.size() == getSearchPageSize())
            {
                list.remove(list.size() - 1);
            }

            list.add(0, item);
        }
        getItemsChangedEvent().raise(this, EventArgs.Empty);
        setLastEvent(Linq.FirstOrDefault(list));
    }

    private boolean entitiesChanged = true;

    @Override
    protected void EntityChanging(Object newValue, Object oldValue) {
        super.EntityChanging(newValue, oldValue);
        entitiesChanged = calculateEntitiesChanged(newValue, oldValue);
    }

    /**
     * Returns true if and only if the two entities: <li>are not null <li>implement the IVdcQueryable.getQueryableId()
     * method <li>the old.getQueryableId().equals(new.getQueryableId())
     *
     */
    private boolean calculateEntitiesChanged(Object newValue, Object oldValue) {
        if (newValue == null || oldValue == null) {
            return true;
        }

        if (!(newValue instanceof IVdcQueryable && oldValue instanceof IVdcQueryable)) {
            return true;
        }

        Object oldValueQueriable = null;
        Object newValueQueriable = null;
        try {
            oldValueQueriable = ((IVdcQueryable) oldValue).getQueryableId();
            newValueQueriable = ((IVdcQueryable) newValue).getQueryableId();
        } catch (NotImplementedException e) {
            return true;
        }

        if (oldValueQueriable == null || newValueQueriable == null) {
            return true;
        }

        return !oldValueQueriable.equals(newValueQueriable);
    }

    /**
     * Runs the onEntityContentChanged() only when the calculateEntitiesChanged(new, old) returns true
     *
     */
    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();

        if (entitiesChanged) {
            onEntityContentChanged();
        }
    }

    /**
     * Called when the OnEntityChanged() ensures, that the new entity is different than the old one (based on the
     * IVdcQueryable). Override it in child classes to refresh your model.
     */
    protected void onEntityContentChanged() {
    }

    @Override
    protected String getListName() {
        return "EventListModel";
    }

}
