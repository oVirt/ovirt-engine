package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.GridTimer;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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
            OnPropertyChanged(new PropertyChangedEventArgs("LastEvent")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsAdvancedView")); //$NON-NLS-1$
        }
    }

    private boolean requestingData;

    public boolean isRequestingData() {
        return requestingData;
    }

    public EventListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().eventsTitle());
        setHashName("events"); //$NON-NLS-1$

        setRefreshCommand(new UICommand("Refresh", this)); //$NON-NLS-1$

        setDefaultSearchString("Events:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.AUDIT_OBJ_NAME, SearchObjects.AUDIT_PLU_OBJ_NAME });

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
        return searchString.trim().toLowerCase().startsWith("event"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        requestingData = true;
        setItems(new ObservableCollection<AuditLog>());
        setLastEvent(null);

        timer.start();
    }

    @Override
    public void Search() {
        super.Search();

        // Force refresh of the event list when the event tab is shown
        // without waiting to the timer. This is invoked only the first
        // time the Events tab is shown - than the timer takes care of this.
        forceRefresh();
    }

    protected void forceRefresh() {
        getRefreshCommand().Execute();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    protected void refreshModel()
    {
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                EventListModel eventListModel = (EventListModel) model;
                ArrayList<AuditLog> list = (ArrayList<AuditLog>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                requestingData = false;
                for (AuditLog auditLog : list) {
                    // in case the corr_id is created in client,
                    // remove unnecessary data (leave only the corr_id).
                    if (auditLog.getCorrelationId() != null
                        && auditLog.getCorrelationId().startsWith(TaskListModel._WEBADMIN_)) {
                        auditLog.setCorrelationId(auditLog.getCorrelationId().split("_")[2]); //$NON-NLS-1$
                    }
                }
                eventListModel.UpdateItems(list);
            }
        });

        SearchParameters params = new SearchParameters(getSearchString(), SearchType.AuditLog);
        params.setMaxCount(getSearchPageSize());
        params.setSearchFrom(getLastEvent() != null ? getLastEvent().getaudit_log_id() : 0);
        params.setRefresh(false);

        Frontend.RunQuery(VdcQueryType.Search, params, query);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getRefreshCommand())
        {
            refreshModel();
            UpdatePagingAvailability();
        }
    }

    @Override
    public void EnsureAsyncSearchStopped()
    {
        super.EnsureAsyncSearchStopped();

        timer.stop();
    }

    private void UpdateItems(ArrayList<AuditLog> source)
    {
        if (getItems() == null)
        {
            return;
        }

        List<AuditLog> list = (List<AuditLog>) getItems();

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

        // If there are no data for this entity, the LastEvent has to be fired in order
        // to stop the progress animation (SearchableTabModelProvider).
        if (Linq.FirstOrDefault(list) == null) {
            OnPropertyChanged(new PropertyChangedEventArgs("LastEvent")); //$NON-NLS-1$
        }
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
        return "EventListModel"; //$NON-NLS-1$
    }

}
