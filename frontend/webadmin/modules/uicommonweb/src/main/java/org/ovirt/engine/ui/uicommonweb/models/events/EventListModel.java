package org.ovirt.engine.ui.uicommonweb.models.events;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class EventListModel extends SearchableListModel
{
	private ITimer timer;


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

		timer = (ITimer)TypeResolver.getInstance().Resolve(ITimer.class);
		timer.setInterval(getConfigurator().getPollingTimerInterval());
		timer.getTickEvent().addListener(this);
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
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			EventListModel eventListModel = (EventListModel)model;
			java.util.ArrayList<AuditLog> list = (java.util.ArrayList<AuditLog>)((VdcQueryReturnValue)ReturnValue).getReturnValue();
			eventListModel.UpdateItems(list);
		}};

		SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.AuditLog);
		tempVar.setMaxCount(getSearchPageSize());
		tempVar.setSearchFrom(getLastEvent() != null ? getLastEvent().getaudit_log_id() : 0);
		tempVar.setRefresh(false);
		SearchParameters searchParameters = tempVar;

		  Frontend.RunQuery(VdcQueryType.Search, searchParameters, _asyncQuery);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		//base.eventRaised(ev, sender, args);

		if (ev.equals(ProvideTickEvent.Definition))
		{
			getRefreshCommand().Execute();
		}
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

		java.util.List<AuditLog> list = (java.util.List<AuditLog>)getItems();

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

}