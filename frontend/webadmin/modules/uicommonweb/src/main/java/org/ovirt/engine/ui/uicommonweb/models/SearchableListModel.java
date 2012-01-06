package org.ovirt.engine.ui.uicommonweb.models;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.RegistrationResult;
import org.ovirt.engine.ui.uicommonweb.ProvideTickEvent;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.IteratorUtils;

import com.google.gwt.core.client.GWT;

/**
 Represents a list model with ability to fetch items both sync and async.
*/
@SuppressWarnings("unused")
public abstract class SearchableListModel extends ListModel implements GridController
{
    private static final int UnknownInteger = -1;
    private static Logger logger = Logger.getLogger(SearchableListModel.class.getName());
	private static final String PAGE_STRING_REGEX = "[\\s]+page[\\s]+[1-9]+[0-9]*[\\s]*$";
	private static final String PAGE_NUMBER_REGEX = "[1-9]+[0-9]*$";

	private UICommand privateSearchCommand;
	public UICommand getSearchCommand()
	{
		return privateSearchCommand;
	}
	private void setSearchCommand(UICommand value)
	{
		privateSearchCommand = value;
	}
	private UICommand privateSearchNextPageCommand;
	public UICommand getSearchNextPageCommand()
	{
		return privateSearchNextPageCommand;
	}
	private void setSearchNextPageCommand(UICommand value)
	{
		privateSearchNextPageCommand = value;
	}
	private UICommand privateSearchPreviousPageCommand;
	public UICommand getSearchPreviousPageCommand()
	{
		return privateSearchPreviousPageCommand;
	}
	private void setSearchPreviousPageCommand(UICommand value)
	{
		privateSearchPreviousPageCommand = value;
	}
	private UICommand privateForceRefreshCommand;
	public UICommand getForceRefreshCommand()
	{
		return privateForceRefreshCommand;
	}
	private void setForceRefreshCommand(UICommand value)
	{
		privateForceRefreshCommand = value;
	}



	private boolean privateIsQueryFirstTime;
	public boolean getIsQueryFirstTime()
	{
		return privateIsQueryFirstTime;
	}
	public void setIsQueryFirstTime(boolean value)
	{
		privateIsQueryFirstTime = value;
	}
	private boolean privateIsTimerDisabled;
	public boolean getIsTimerDisabled()
	{
		return privateIsTimerDisabled;
	}
	public void setIsTimerDisabled(boolean value)
	{
		privateIsTimerDisabled = value;
	}

	// Update IsAsync wisely! Set it once after initializing the SearchableListModel object.
	private boolean privateIsAsync;
	public boolean getIsAsync()
	{
		return privateIsAsync;
	}
	public void setIsAsync(boolean value)
	{
		privateIsAsync = value;
	}
	private String privateDefaultSearchString;
	public String getDefaultSearchString()
	{
		return privateDefaultSearchString;
	}
	public void setDefaultSearchString(String value)
	{
		privateDefaultSearchString = value;
	}
	private int privateSearchPageSize;
	public int getSearchPageSize()
	{
		return privateSearchPageSize;
	}
	public void setSearchPageSize(int value)
	{
		privateSearchPageSize = value;
	}

	private RegistrationResult asyncResult;
	public RegistrationResult getAsyncResult()
	{
		return asyncResult;
	}
	public void setAsyncResult(RegistrationResult value)
	{
		if (asyncResult != value)
		{
			AsyncResultChanging(value, asyncResult);
			asyncResult = value;
			OnPropertyChanged(new PropertyChangedEventArgs("AsyncResult"));
		}
	}

	private String searchString;
	public String getSearchString()
	{
		return searchString;
	}
	public void setSearchString(String value)
	{
		if (!StringHelper.stringsEqual(searchString, value))
		{
			searchString = value;
			SearchStringChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("SearchString"));
		}
	}

	public int getSearchPageNumber()
	{
		if (StringHelper.isNullOrEmpty(getSearchString()))
		{
			return 1;
		}

			// try getting the end of SearchString in the form of "page <n>"
		String pageStringRegex = PAGE_STRING_REGEX;

		Match match = Regex.Match(getSearchString(), pageStringRegex, RegexOptions.IgnoreCase);
		if (match.Success())
		{
				// retrieve the page number itself:
			String pageString = match.getValue(); // == "page <n>"
			String pageNumberRegex = PAGE_NUMBER_REGEX;
			match = Regex.Match(pageString, pageNumberRegex);
			if (match.Success())
			{
				int retValue = 0;
				RefObject<Integer> tempRef_retValue = new RefObject<Integer>(retValue);
				boolean tempVar = IntegerCompat.TryParse(match.getValue(), tempRef_retValue);
					retValue = tempRef_retValue.argvalue;
				if (tempVar)
				{
					return retValue;
				}
			}
		}

		return 1;
	}

	public int getNextSearchPageNumber()
	{
		return getSearchPageNumber() + 1;
	}

	public int getPreviousSearchPageNumber()
	{
		return getSearchPageNumber() == 1 ? 1 : getSearchPageNumber() - 1;
	}


	private PrivateAsyncCallback asyncCallback;

	protected SearchableListModel()
	{
		//Configure this instance.
		getConfigurator().Configure(this);

		setSearchCommand(new UICommand("Search", this));
		setSearchNextPageCommand(new UICommand("SearchNextPage", this));
		setSearchPreviousPageCommand(new UICommand("SearchPreviousPage", this));
		setForceRefreshCommand(new UICommand("ForceRefresh", this));
		setSearchPageSize(UnknownInteger);
		asyncCallback = new PrivateAsyncCallback(this);


		// Most of SearchableListModels will not have paging. The ones that
		// should have paging will set it explicitly in their constructors.
		getSearchNextPageCommand().setIsAvailable(false);
		getSearchPreviousPageCommand().setIsAvailable(false);
	}

	/**
	 Returns value indicating whether the specified search string is
	 matching this list model.
	*/
	public boolean IsSearchStringMatch(String searchString)
	{
		return true;
	}

	private GridTimer privatetimer;

    private boolean rapidTimerRunning;

	public GridTimer gettimer()
	{
		return privatetimer;
	}
	public void settimer(GridTimer value)
	{
		privatetimer = value;
	}
	public GridTimer getTimer()
	{
		if (gettimer() == null)
		{
			settimer(new GridTimer(getListName()) {

                @Override
                public void execute() {
                    logger.info(SearchableListModel.this.getClass().getName() + ": Executing search");
                    if (getIsAsync())
                    {
                        AsyncSearch();
                    } else {
                        SyncSearch();
                    }
                }

			});
			gettimer().setRefreshRate(getConfigurator().getPollingTimerInterval());
		}
		return gettimer();
	}

	@Override
	public void setSelectedItem(Object value) {
	    setIsQueryFirstTime(true);
	    super.setSelectedItem(value);
	    setIsQueryFirstTime(false);
	}

    @Override
    public void setEntity(Object value) {
        if(getEntity() == null){
            super.setEntity(value);
            return;
        }
        //Equals doesn't always has the same outcome as checking the ids of the elements.
        if(getEntity() instanceof IVdcQueryable) {
            if(value != null){
                IVdcQueryable ivdcq_value = (IVdcQueryable)value;
                IVdcQueryable ivdcq_entity = (IVdcQueryable)getEntity();
                if(!ivdcq_value.getQueryableId().equals(ivdcq_entity.getQueryableId())){
                    super.setEntity(value);
                    return;
                }
            }
        }
        if(!getEntity().equals(value)){
            super.setEntity(value);
            return;
        }

        setEntity(value, false);
    }

	protected abstract String getListName() ;

    protected void SearchStringChanged()
	{
	}

	public void Search()
	{
		//Defer search if there max result limit was not yet retrieved.
		if (getSearchPageSize() == UnknownInteger)
		{
			asyncCallback.RequestSearch();
		}
		else
		{
			EnsureAsyncSearchStopped();

			if (getIsQueryFirstTime())
			{
				setSelectedItem(null);
				setSelectedItems(null);
			}

			if (getIsAsync())
			{
				AsyncSearch();
			}
			else
			{
				if (getIsTimerDisabled() == false)
				{
					setIsQueryFirstTime(true);
					SyncSearch();
					setIsQueryFirstTime(false);
					getTimer().start();
				}
				else
				{
					SyncSearch();
				}
			}
		}
	}

	public void ForceRefresh()
	{
		getTimer().stop();
		setIsQueryFirstTime(true);
		SyncSearch();

		if (!getIsTimerDisabled())
		{
			getTimer().start();
		}
	}

	private void AsyncResultChanging(RegistrationResult newValue, RegistrationResult oldValue)
	{
		if (oldValue != null)
		{
			oldValue.getRetrievedEvent().removeListener(this);
		}

		if (newValue != null)
		{
			newValue.getRetrievedEvent().addListener(this);
		}
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(RegistrationResult.RetrievedEventDefinition))
		{
			AsyncResult_Retrieved();
		}
		if (ev.equals(ProvideTickEvent.Definition))
		{
			SyncSearch();
		}
	}

	private void AsyncResult_Retrieved()
	{
		//Update IsEmpty flag.

		// Note: Do NOT use IList. 'Items' is not necissarily IList
		// (e.g in Monitor models, the different ListModels' Items are
		// of type 'valueObjectEnumerableList', which is not IList).
		if (getItems() != null)
		{
			java.util.Iterator enumerator = getItems().iterator();
			setIsEmpty(enumerator.hasNext() ? false : true);
		}
		else
		{
			setIsEmpty(true);
		}
	}

	private void ResetIsEmpty()
	{
		// Note: Do NOT use IList: 'Items' is not necissarily IList
		// (e.g in Monitor models, the different ListModels' Items are
		// of type 'valueObjectEnumerableList', which is not IList).
		if (getItems() != null)
		{
			java.util.Iterator enumerator = getItems().iterator();
			if (enumerator.hasNext())
			{
				setIsEmpty(false);
			}
		}
	}

	@Override
	protected void ItemsChanged()
	{
		super.ItemsChanged();

		ResetIsEmpty();
		UpdatePagingAvailability();
	}

	@Override
	protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
	{
		super.ItemsCollectionChanged(sender, e);

		ResetIsEmpty();
		UpdatePagingAvailability();
	}

	protected void UpdatePagingAvailability()
	{
		getSearchNextPageCommand().setIsExecutionAllowed(getSearchNextPageCommand().getIsAvailable() && getNextSearchPageAllowed());
		getSearchPreviousPageCommand().setIsExecutionAllowed(getSearchPreviousPageCommand().getIsAvailable() && getPreviousSearchPageAllowed());
	}

	private void SetSearchStringPage(int newSearchPageNumber)
	{
		if (Regex.IsMatch(getSearchString(), PAGE_STRING_REGEX, RegexOptions.IgnoreCase))
		{
			setSearchString(Regex.replace(getSearchString(), PAGE_STRING_REGEX, StringFormat.format(" page %1$s", newSearchPageNumber)));
		}
		else
		{
			setSearchString(StringFormat.format("%1$s page %2$s", getSearchString(), newSearchPageNumber));
		}
	}

	protected void SearchNextPage()
	{
		SetSearchStringPage(getNextSearchPageNumber());
		getSearchCommand().Execute();
	}

	protected void SearchPreviousPage()
	{
		SetSearchStringPage(getPreviousSearchPageNumber());
		getSearchCommand().Execute();
	}

	protected boolean getNextSearchPageAllowed()
	{
		if (!getSearchNextPageCommand().getIsAvailable() || getItems() == null || IteratorUtils.moveNext(getItems().iterator()) == false)
		{
			return false;
		}

		boolean retValue = true;

			// ** TODO: Inefficient performance-wise! If 'Items' was ICollection or IList
			// ** it would be better, since we could simply check its 'Count' property.

		int pageSize = getSearchPageSize();

		if (pageSize > 0)
		{
			java.util.Iterator e = getItems().iterator();
			int itemsCountInCurrentPage = 0;
			while (IteratorUtils.moveNext(e))
			{
				itemsCountInCurrentPage++;
			}

			if (itemsCountInCurrentPage < pageSize)
			{
					// current page contains results quantity smaller than
					// the pageSize -> there is no next page:
				retValue = false;
			}
		}

		return retValue;
	}

	protected boolean getPreviousSearchPageAllowed()
	{
		return getSearchPreviousPageCommand().getIsAvailable() && getSearchPageNumber() > 1;
	}

	/**
	 Override this method to take care on sync fetching.
	*/
	protected void SyncSearch()
	{
	}

	@Override
	public Iterable getItems()
	{
		return items;
	}
	@Override
	public void setItems(Iterable value)
	{
		if (items != value)
		{
			IVdcQueryable lastSelectedItem = (IVdcQueryable)getSelectedItem();
			java.util.ArrayList<IVdcQueryable> lastSelectedItems = new java.util.ArrayList<IVdcQueryable>();

			if (getSelectedItems() != null)
			{
				if (getSelectedItems() instanceof java.util.ArrayList)
				{
					for (Object item : getSelectedItems())
					{
						lastSelectedItems.add((IVdcQueryable) item);
					}
				}
				else
				{
					java.util.Iterator iterator = getSelectedItems().iterator();
					while (iterator.hasNext())
					{
						lastSelectedItems.add((IVdcQueryable)iterator.next());
					}
				}
			}

			ItemsChanging(value, items);
			items = value;
			UpdatePagingAvailability();
			getItemsChangedEvent().raise(this, EventArgs.Empty);
			OnPropertyChanged(new PropertyChangedEventArgs("Items"));

			selectedItem = null;

			if (getSelectedItems() != null)
			{
				getSelectedItems().clear();
			}

			if (lastSelectedItem != null && value != null)
			{
				IVdcQueryable newSelectedItem = null;
				java.util.ArrayList<IVdcQueryable> newItems = new java.util.ArrayList<IVdcQueryable>();

				if (value instanceof java.util.ArrayList)
				{
					for (Object item : value)
					{
						newItems.add((IVdcQueryable)item);
					}
				}
				else
				{
					java.util.Iterator iterator = value.iterator();
					while (iterator.hasNext())
					{
					    newItems.add((IVdcQueryable)iterator.next());
					}
				}

				if (newItems != null)
				{
					for (IVdcQueryable newItem : newItems)
					 {
							// Search for selected item
						if (newItem.getQueryableId().equals(lastSelectedItem.getQueryableId()))
						{
							newSelectedItem = newItem;
						}
						else
						{
								// Search for selected items
							for (IVdcQueryable item : lastSelectedItems)
							{
								if (newItem.getQueryableId().equals(item.getQueryableId()))
								{
									selectedItems.add(newItem);
								}
							}
						}
					}
				}
				if (newSelectedItem != null)
				{
					selectedItem = newSelectedItem;

					if (selectedItems != null)
					{
						selectedItems.add(newSelectedItem);
					}
				}
			 }
			OnSelectedItemChanged();
		}
	}

	public void SyncSearch(VdcQueryType vdcQueryType, VdcQueryParametersBase vdcQueryParametersBase)
	{
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			SearchableListModel searchableListModel = (SearchableListModel)model;
			searchableListModel.setItems((Iterable)((VdcQueryReturnValue)ReturnValue).getReturnValue());
		}};

		vdcQueryParametersBase.setRefresh(getIsQueryFirstTime());

		Frontend.RunQuery(vdcQueryType, vdcQueryParametersBase, _asyncQuery);

		setIsQueryFirstTime(false);
	}

	/**
	 Override this method to take care on async fetching.
	*/
	protected void AsyncSearch()
	{
	}

	public void EnsureAsyncSearchStopped()
	{
		getTimer().stop();
		if (getAsyncResult() != null && !getAsyncResult().getId().equals(Guid.Empty))
		{
			Frontend.UnregisterQuery(getAsyncResult().getId());
			setAsyncResult(null);
		}
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getSearchCommand())
		{
			Search();
		}
		else if (command == getSearchNextPageCommand())
		{
			SearchNextPage();
		}
		else if (command == getSearchPreviousPageCommand())
		{
			SearchPreviousPage();
		}
		else if (command == getForceRefreshCommand())
		{
			ForceRefresh();
		}

		if(command.isAutoRefresh()) {
		    getTimer().fastForward();
		}
	}


	public final static class PrivateAsyncCallback
	{
		private SearchableListModel model;
		private boolean searchRequested;


		public PrivateAsyncCallback(SearchableListModel model)
		{
			this.model = model;
			AsyncQuery _asyncQuery1 = new AsyncQuery();
			_asyncQuery1.setModel(this);
			_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
											{
												PrivateAsyncCallback privateAsyncCallback1 = (PrivateAsyncCallback)model1;
												privateAsyncCallback1.ApplySearchPageSize((Integer)result1);
			}};
			AsyncDataProvider.GetSearchResultsLimit(_asyncQuery1);
		}

		public void RequestSearch()
		{
			searchRequested = true;
		}

		private void ApplySearchPageSize(int value)
		{
			model.setSearchPageSize(value);

			//If there search was requested before max result limit was retrieved, do it now.
			if (searchRequested && model.getIsTimerDisabled())
			{
				model.getSearchCommand().Execute();
			}

			//Sure paging functionality.
			model.UpdatePagingAvailability();
		}
	}

    // ////////////////////////////
    // GridController methods
    // ///////////////////////////

    @Override
    public void setRefreshRate(int currentRefreshRate) {
        getTimer().setRefreshRate(currentRefreshRate);
    }

    @Override
    public int getRefreshRate() {
        return getTimer().getRefreshRate();
    }

    @Override
    public void toBackground() {
        // move to slow
        logger.info("toBackground(): pausing timer");
        getTimer().pause();
    }

    @Override
    public void toForground() {
        // move to normal
        logger.info("toForground(): resuming timer");
        getTimer().resume();
    }

    @Override
    public String getId() {
        return getListName();
    }
}
