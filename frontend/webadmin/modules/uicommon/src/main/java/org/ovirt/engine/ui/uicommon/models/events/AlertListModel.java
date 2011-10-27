package org.ovirt.engine.ui.uicommon.models.events;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class AlertListModel extends SearchableListModel
{
	private ObservableCollection<IVdcQueryable> items;


	//		public event EventHandler NewAlert = delegate { };

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



	public java.util.List getItems()
	{
		return (java.util.List)super.getItems();
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

		IProvideCollectionChangedEvent notifier = (IProvideCollectionChangedEvent)((items instanceof IProvideCollectionChangedEvent) ? items : null);
		if (notifier != null)
		{
			notifier.getCollectionChangedEvent().addListener(this);
		}
	}

	@Override
	public void EnsureAsyncSearchStopped()
	{
		super.EnsureAsyncSearchStopped();

		if (getAsyncResult() != null && !getAsyncResult().getId().equals(Guid.Empty))
		{
			IProvideCollectionChangedEvent notifier = (IProvideCollectionChangedEvent)((items instanceof IProvideCollectionChangedEvent) ? items : null);
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
			items_CollectionChanged(sender, (NotifyCollectionChangedEventArgs)args);
		}
	}

	private void items_CollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
	{
		super.ItemsCollectionChanged(sender, e);

		if (e.Action == NotifyCollectionChangedAction.Remove)
		{
			java.util.ArrayList<AuditLog> items = Linq.<AuditLog>Cast(getItems());

			//var itemsToRemove =
			//	e.OldItems
			//	.Cast<AuditLog>()
			//	.Select(a => items.FirstOrDefault(b => b.audit_log_id == a.audit_log_id))
			//	.ToList();
			java.util.ArrayList<AuditLog> itemsToRemove = new java.util.ArrayList<AuditLog>();
			for (Object item : e.OldItems)
			{
				AuditLog a = (AuditLog)item;
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

			//itemsToRemove.Each(a => Items.Remove(a));
			for (AuditLog a : itemsToRemove)
			{
				getItems().remove(a);
			}
		}

		if (e.Action == NotifyCollectionChangedAction.Add)
		{
			for (Object item : e.NewItems)
			{
				AuditLog a = (AuditLog)item;
				if (getItems().size() == 100)
				{
					getItems().remove(getItems().size() - 1);
				}

				//var lastItem = Items.Count > 0 ? Items[Items.Count - 1] as AuditLog : null;
				AuditLog lastItem = getItems().size() > 0 ? (AuditLog)getItems().get(getItems().size() - 1) : null;
				if (lastItem != null && lastItem.getaudit_log_id() < a.getaudit_log_id())
				{
					getItems().add(0, a);
					//NewAlert(this, EventArgs.Empty);
					getNewAlertEvent().raise(this, EventArgs.Empty);
				}
				else
				{
					getItems().add(a);
				}
			}

			setLastAlert(getItems().size() > 0 ? (AuditLog)getItems().get(0) : null);
		}

		UpdateTitle();
	}

	private void UpdateTitle()
	{
		setTitle(StringFormat.format("%1$s Alerts", (getItems() == null || getItems().isEmpty()) ? "No" : String.valueOf(getItems().size())));
		setHasAlerts(getItems() != null && getItems().size() > 0);
	}
}