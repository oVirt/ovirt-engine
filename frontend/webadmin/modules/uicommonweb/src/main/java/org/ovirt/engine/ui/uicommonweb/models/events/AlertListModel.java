package org.ovirt.engine.ui.uicommonweb.models.events;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.IProvideCollectionChangedEvent;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.NotifyCollectionChangedAction;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.ProvideCollectionChangedEvent;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class AlertListModel extends SearchableListModel
{
    private ObservableCollection<IVdcQueryable> items;

    // public event EventHandler NewAlert = delegate { };

    public static EventDefinition NewAlertEventDefinition;
    private Event privateNewAlertEvent;

    public Event getNewAlertEvent()
    {
        return privateNewAlertEvent;
    }

    private void setNewAlertEvent(Event value)
    {
        privateNewAlertEvent = value;
    }

    @Override
    public java.util.List getItems()
    {
        return (java.util.List) super.getItems();
    }

    public void setItems(java.util.List value)
    {
        super.setItems(value);
    }

    private boolean hasAlerts;

    public boolean getHasAlerts()
    {
        return hasAlerts;
    }

    private void setHasAlerts(boolean value)
    {
        if (hasAlerts != value)
        {
            hasAlerts = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasAlerts"));
        }
    }

    private AuditLog lastAlert;

    public AuditLog getLastAlert()
    {
        return lastAlert;
    }

    private void setLastAlert(AuditLog value)
    {
        if (lastAlert != value)
        {
            lastAlert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LastAlert"));
        }
    }

    static
    {
        NewAlertEventDefinition = new EventDefinition("NewAlert", AlertListModel.class);
    }

    public AlertListModel()
    {
        setNewAlertEvent(new Event(NewAlertEventDefinition));

        setIsTimerDisabled(false);

        setDefaultSearchString("Events: severity=alert");
        setSearchString(getDefaultSearchString());

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        UpdateTitle();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setItems(new ObservableCollection<IVdcQueryable>());

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.AuditLog, getSearchPageSize()));
        items = getAsyncResult().getData();

        IProvideCollectionChangedEvent notifier = ((items instanceof IProvideCollectionChangedEvent) ? items : null);
        if (notifier != null)
        {
            notifier.getCollectionChangedEvent().addListener(this);
        }
    }

    @Override
    protected void SyncSearch()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                AlertListModel alertListModel = (AlertListModel) model;
                java.util.ArrayList<AuditLog> list =
                        (java.util.ArrayList<AuditLog>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                alertListModel.setItems(list);
            }
        };

        SearchParameters tempVar = new SearchParameters("Events: severity=alert", SearchType.AuditLog);
        tempVar.setMaxCount(getSearchPageSize());
        tempVar.setRefresh(false);
        SearchParameters searchParameters = tempVar;

        Frontend.RunQuery(VdcQueryType.Search, searchParameters, _asyncQuery);
    }

    @Override
    public void EnsureAsyncSearchStopped()
    {
        super.EnsureAsyncSearchStopped();

        if (getAsyncResult() != null && !getAsyncResult().getId().equals(NGuid.Empty))
        {
            IProvideCollectionChangedEvent notifier =
                    ((items instanceof IProvideCollectionChangedEvent) ? items : null);
            if (notifier != null)
            {
                notifier.getCollectionChangedEvent().removeListener(this);
            }
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ProvideCollectionChangedEvent.Definition))
        {
            items_CollectionChanged(sender, (NotifyCollectionChangedEventArgs) args);
        }
    }

    private void items_CollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.ItemsCollectionChanged(sender, e);

        if (e.Action == NotifyCollectionChangedAction.Remove)
        {
            java.util.ArrayList<AuditLog> items = Linq.<AuditLog> Cast(getItems());

            // var itemsToRemove =
            // e.OldItems
            // .Cast<AuditLog>()
            // .Select(a => items.FirstOrDefault(b => b.audit_log_id == a.audit_log_id))
            // .ToList();
            java.util.ArrayList<AuditLog> itemsToRemove = new java.util.ArrayList<AuditLog>();
            for (Object item : e.OldItems)
            {
                AuditLog a = (AuditLog) item;
                long i = 0;
                for (AuditLog b : items)
                {
                    if (b.getaudit_log_id() == a.getaudit_log_id())
                    {
                        i = b.getaudit_log_id();
                        break;
                    }
                }

                if (a.getaudit_log_id() == i)
                {
                    itemsToRemove.add(a);
                }
            }

            // itemsToRemove.Each(a => Items.Remove(a));
            for (AuditLog a : itemsToRemove)
            {
                getItems().remove(a);
            }
        }

        if (e.Action == NotifyCollectionChangedAction.Add)
        {
            for (Object item : e.NewItems)
            {
                AuditLog a = (AuditLog) item;
                if (getItems().size() == 100)
                {
                    getItems().remove(getItems().size() - 1);
                }

                // var lastItem = Items.Count > 0 ? Items[Items.Count - 1] as AuditLog : null;
                AuditLog lastItem = getItems().size() > 0 ? (AuditLog) getItems().get(getItems().size() - 1) : null;
                if (lastItem != null && lastItem.getaudit_log_id() < a.getaudit_log_id())
                {
                    getItems().add(0, a);
                    // NewAlert(this, EventArgs.Empty);
                    getNewAlertEvent().raise(this, EventArgs.Empty);
                }
                else
                {
                    getItems().add(a);
                }
            }

            setLastAlert(getItems().size() > 0 ? (AuditLog) getItems().get(0) : null);
        }

        UpdateTitle();
    }

    private void UpdateTitle()
    {
        setTitle(StringFormat.format("%1$s Alerts",
                (getItems() == null || getItems().isEmpty()) ? "No" : String.valueOf(getItems().size())));
        setHasAlerts(getItems() != null && getItems().size() > 0);
    }

    @Override
    protected String getListName() {
        return "AlertListModel";
    }
}
