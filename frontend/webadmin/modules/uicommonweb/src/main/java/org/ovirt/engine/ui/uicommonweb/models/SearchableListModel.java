package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.core.searchbackend.SyntaxObject;
import org.ovirt.engine.core.searchbackend.SyntaxObjectType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.RegistrationResult;
import org.ovirt.engine.ui.frontend.communication.RefreshActiveModelEvent;
import org.ovirt.engine.ui.frontend.communication.RefreshActiveModelEvent.RefreshActiveModelHandler;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.ProvideTickEvent;
import org.ovirt.engine.ui.uicommonweb.ReportCommand;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.GridTimerStateChangeEvent.GridTimerStateChangeEventHandler;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Represents a list model with ability to fetch items both sync and async.
 *
 * This list model has also an entity.
 * This entity is useful to represent hierarchical parent of the list items.
 * For example {@link org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel} has an entity of type
 * Cluster and a list of items of type VM.
 *
 * @param <E> The type of the entity.
 * @param <T> The type of list items.
 */
// TODO once all the children of this class will be refactored to use generics, change from <T> to <T extends IVdcQueryable>
public abstract class SearchableListModel<E, T> extends SortedListModel<T> implements HasEntity<E>, GridController {
    private static final int UnknownInteger = -1;
    private static final Logger logger = Logger.getLogger(SearchableListModel.class.getName());
    private static final String PAGE_STRING_REGEX = "[\\s]+page[\\s]+[1-9]+[0-9]*[\\s]*$"; //$NON-NLS-1$
    private static final String PAGE_NUMBER_REGEX = "[1-9]+[0-9]*$"; //$NON-NLS-1$

    private UICommand privateSearchCommand;
    private HandlerRegistration timerChangeHandler;

    public UICommand getSearchCommand() {
        return privateSearchCommand;
    }

    private void setSearchCommand(UICommand value) {
        privateSearchCommand = value;
    }

    private UICommand privateSearchNextPageCommand;

    public UICommand getSearchNextPageCommand() {
        return privateSearchNextPageCommand;
    }

    private void setSearchNextPageCommand(UICommand value) {
        privateSearchNextPageCommand = value;
    }

    private UICommand privateSearchPreviousPageCommand;

    public UICommand getSearchPreviousPageCommand() {
        return privateSearchPreviousPageCommand;
    }

    private void setSearchPreviousPageCommand(UICommand value) {
        privateSearchPreviousPageCommand = value;
    }

    private UICommand privateForceRefreshCommand;

    public UICommand getForceRefreshCommand() {
        return privateForceRefreshCommand;
    }

    private void setForceRefreshCommand(UICommand value) {
        privateForceRefreshCommand = value;
    }

    private final List<ReportCommand> openReportCommands = new LinkedList<>();

    public ReportCommand addOpenReportCommand(String idParamName, boolean isMultiple, String uriId) {
        return addOpenReportCommand(new ReportCommand("OpenReport", idParamName, isMultiple, uriId, this)); //$NON-NLS-1$
    }

    private ReportCommand addOpenReportCommand(ReportCommand reportCommand) {
        if (openReportCommands.add(reportCommand)) {
            List<IVdcQueryable> items =
                    getSelectedItems() != null ? Linq.<IVdcQueryable> cast(getSelectedItems())
                            : new ArrayList<IVdcQueryable>();
            updateReportCommandAvailability(reportCommand, items);

            return reportCommand;
        } else {
            return null;
        }
    }

    private boolean privateIsQueryFirstTime;

    public boolean getIsQueryFirstTime() {
        return privateIsQueryFirstTime;
    }

    public void setIsQueryFirstTime(boolean value) {
        privateIsQueryFirstTime = value;
    }

    private boolean privateIsTimerDisabled;

    public boolean getIsTimerDisabled() {
        return privateIsTimerDisabled;
    }

    public void setIsTimerDisabled(boolean value) {
        privateIsTimerDisabled = value;
    }

    private String privateDefaultSearchString;

    public String getDefaultSearchString() {
        return privateDefaultSearchString;
    }

    public void setDefaultSearchString(String value) {
        privateDefaultSearchString = value;
    }

    private String[] searchObjects;

    public String[] getSearchObjects() {
        return searchObjects;
    }

    public void setSearchObjects(String[] value) {
        searchObjects = value;
    }

    private int privateSearchPageSize;

    public int getSearchPageSize() {
        return privateSearchPageSize;
    }

    public void setSearchPageSize(int value) {
        privateSearchPageSize = value;
    }

    private String searchString;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String value) {
        if (!Objects.equals(searchString, value)) {
            searchString = value;
            pagingSearchString = null;
            searchStringChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchString")); //$NON-NLS-1$
        }
    }

    private boolean caseSensitiveSearch = true;

    public boolean isCaseSensitiveSearch() {
        return caseSensitiveSearch;
    }

    public void setCaseSensitiveSearch(boolean value) {
        caseSensitiveSearch = value;
    }

    private String pagingSearchString;

    public int getSearchPageNumber() {
        return this.currentPageNumber;
    }

    public String getItemsCountString() {
        if (getItems() == null) {
            return ""; //$NON-NLS-1$
        }
        int fromItemCount = getSearchPageSize() * (getSearchPageNumber() - 1) + 1;
        int toItemCount = (fromItemCount - 1) + ((Collection) getItems()).size();

        if (toItemCount == 0 || fromItemCount > toItemCount) {
            return ""; //$NON-NLS-1$
        }

        return fromItemCount + "-" + toItemCount; //$NON-NLS-1$
    }

    public int getNextSearchPageNumber() {
        return getSearchPageNumber() + 1;
    }

    public int getPreviousSearchPageNumber() {
        return getSearchPageNumber() == 1 ? 1 : getSearchPageNumber() - 1;
    }

    private final PrivateAsyncCallback<E, T> asyncCallback;

    private final EntityModel<E> entityModel;

    protected SearchableListModel() {
        setSearchCommand(new UICommand("Search", this)); //$NON-NLS-1$
        setSearchNextPageCommand(new UICommand("SearchNextPage", this)); //$NON-NLS-1$
        setSearchPreviousPageCommand(new UICommand("SearchPreviousPage", this)); //$NON-NLS-1$
        setForceRefreshCommand(new UICommand("ForceRefresh", this)); //$NON-NLS-1$
        setSearchPageSize(UnknownInteger);
        asyncCallback = new PrivateAsyncCallback<>(this);

        updateActionAvailability();

        // Most of SearchableListModels will not have paging. The ones that
        // should have paging will set it explicitly in their constructors.
        getSearchNextPageCommand().setIsAvailable(false);
        getSearchPreviousPageCommand().setIsAvailable(false);

        entityModel = new EntityModel<E>() {
            @Override
            protected void onEntityChanged() {
                super.onEntityChanged();
                SearchableListModel.this.onEntityChanged();
            }

            @Override
            protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
                super.entityPropertyChanged(sender, e);
                SearchableListModel.this.entityPropertyChanged(sender, e);
            }

            @Override
            protected void entityChanging(E newValue, E oldValue) {
                super.entityChanging(newValue, oldValue);
                SearchableListModel.this.entityChanging(newValue, oldValue);
            }
        };
    }

    @Override
    public Event<EventArgs> getEntityChangedEvent() {
        return entityModel.getEntityChangedEvent();
    }

    @Override
    public E getEntity() {
        return entityModel.getEntity();
    }

    @Override
    public void setEntity(E value) {
        if (getEntity() == null) {
            entityModel.setEntity(value);
            return;
        }
        // Equals doesn't always has the same outcome as checking the ids of the elements.
        if (value != null) {
            if (!((IVdcQueryable) value).getQueryableId().equals(((IVdcQueryable) getEntity()).getQueryableId())) {
                entityModel.setEntity(value);
                return;
            }
        }

        if (!getEntity().equals(value)) {
            entityModel.setEntity(value);
            return;
        }

        setEntity(value, false);
    }

    protected void setEntity(E value, boolean fireEvents) {
        entityModel.setEntity(value, fireEvents);
    }

    protected void onEntityChanged() {
    }

    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
    }

    protected void entityChanging(E oldValue, E newValue) {
    }

    /**
     * Returns value indicating whether the specified search string is matching this list model.
     */
    public boolean isSearchStringMatch(String searchString) {
        return true;
    }

    /**
     * Grid refresh timer associated with this list model.
     */
    private GridTimer timer;
    private int currentPageNumber = 1; //Default to 1

    /**
     * Setter for the grid timer.
     * @param value The new {@code GridTimer}.
     */
    private void setTimer(final GridTimer value) {
        timer = value;
    }

    @Override
    public GridTimer getTimer() {
        if (timer == null && getEventBus() != null) {
            // The timer doesn't exist yet, and we have an event bus, create the timer and pass in the bus.
            setTimer(new GridTimer(getListName(), getEventBus()) {

                @Override
                public void execute() {
                    // Execute the code, sub classes can override this method to get their own code run.
                    doGridTimerExecute();
                }

            });
            //Always add a change handler, so we can properly synchronize the interval on all GridTimers.
            replaceTimerChangeHandler();
        }
        return timer;
    }

    /**
     * Sub classes can override this method if they need to do something different when the timer
     * expires.
     */
    protected void doGridTimerExecute() {
        logger.fine(SearchableListModel.this.getClass().getName() + ": Executing search"); //$NON-NLS-1$
        syncSearch();
    }

    /**
     * Add a {@code ValueChangeHandler} to the timer associated with this {@code SearchableListModel}.
     * The handler is used to update the refresh rate based on changes of other timers. So if another timer changes
     * from lets say 5 seconds to 30 seconds interval. It will fire a {@code ValueChangeEvent} which this timer
     * receives.
     *
     * If this timer is currently active (active tab/always active). It will stop this timer, change the interval,
     * and start the timer again. If it is inactive, it will just update the interval so that the interval is correct
     * for when the timer does become active (changing main tabs).
     */
    private void addTimerChangeHandler() {
        timerChangeHandler = timer.addGridTimerStateChangeEventHandler(new GridTimerStateChangeEventHandler() {

            @Override
            public void onGridTimerStateChange(GridTimerStateChangeEvent event) {
                int newInterval = event.getRefreshRate();
                if (timer.isActive()) {
                    //Immediately adjust timer and restart if it was active.
                    if (newInterval != timer.getRefreshRate()) {
                        timer.stop();
                        timer.setRefreshRate(newInterval, false);
                        timer.start();
                    }
                } else {
                    //Update the timer interval for inactive timers, so they are correct when they become active
                    timer.setRefreshRate(newInterval, false);
                }
            }
        });
    }

    protected void replaceTimerChangeHandler() {
        if (timerChangeHandler != null) {
            removeTimerChangeHandler();
        }
        addTimerChangeHandler();
    }

    @Override
    public void refresh() {
        getForceRefreshCommand().execute();
    }

    @Override
    public void setSelectedItem(T value) {
        setIsQueryFirstTime(true);
        super.setSelectedItem(value);
        setIsQueryFirstTime(false);
    }

    protected abstract String getListName();

    protected void searchStringChanged() {
    }

    public void search() {
        // Defer search if there max result limit was not yet retrieved.
        if (getSearchPageSize() == UnknownInteger) {
            asyncCallback.requestSearch();
        }
        else {
            stopRefresh();

            if (getIsQueryFirstTime()) {
                setSelectedItem(null);
                setSelectedItems(null);
            }

            if (getIsTimerDisabled() == false) {
                setIsQueryFirstTime(true);
                onPropertyChanged(new PropertyChangedEventArgs(PropertyChangedEventArgs.PROGRESS));
                syncSearch();
                setIsQueryFirstTime(false);
                startGridTimer();
            }
            else {
                syncSearch();
            }
        }
    }

    protected void startGridTimer() {
        if (getTimer() != null) {
            //Timer can be null if the event bus hasn't been set yet (model hasn't been fully initialized)
            startRefresh();
        } else {
            //Defer the start of the timer until after the event bus has been added to this model. Then we
            //can pass the event bus to the timer and the timer can become active.
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    startRefresh();
                }
            });
        }
    }

    private void startRefresh() {
        if (getTimer() != null) {
            getTimer().start();
        }
    }

    public void forceRefresh() {
        stopRefresh();
        setIsQueryFirstTime(true);
        syncSearch();

        if (!getIsTimerDisabled()) {
            startRefresh();
        }
    }

    protected void setReportModelResourceId(ReportModel reportModel, String idParamName, boolean isMultiple) {

    }

    protected void openReport() {
        setWidgetModel(createReportModel());
    }

    protected ReportModel createReportModel() {
        ReportCommand reportCommand = (ReportCommand) getLastExecutedCommand();
        ReportModel reportModel = new ReportModel(ReportInit.getInstance().getReportRightClickUrl(),
                ReportInit.getInstance().getSsoToken());

        reportModel.setReportUnit(reportCommand.getUriValue());

        if (reportCommand.getIdParamName() != null) {
            for (T item : getSelectedItems()) {
                if (((ReportCommand) getLastExecutedCommand()).isMultiple) {
                    reportModel.addResourceId(reportCommand.getIdParamName(), ((BusinessEntity<?>) item).getId()
                            .toString());
                } else {
                    reportModel.setResourceId(reportCommand.getIdParamName(), ((BusinessEntity<?>) item).getId()
                            .toString());
                }
            }
        }

        boolean firstItem = true;
        String dcId = ""; //$NON-NLS-1$
        for (T item : getSelectedItems()) {
            if (item instanceof HasStoragePool) {
                if (firstItem) {
                    dcId = ((HasStoragePool<?>) item).getStoragePoolId().toString();
                    firstItem = false;
                } else if (!((HasStoragePool<?>) item).getStoragePoolId().toString().equals(dcId)) {
                    reportModel.setDifferntDcError(true);
                    continue;
                }
            }
        }

        if (!dcId.equals("")) { //$NON-NLS-1$
            reportModel.setDataCenterID(dcId);
        }

        return reportModel;
    }

    private String dateStr(Date date) {
        return date.getYear() + "-" + date.getMonth() + "-" + date.getDate(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void asyncResultChanging(RegistrationResult newValue, RegistrationResult oldValue) {
        if (oldValue != null) {
            oldValue.getRetrievedEvent().removeListener(this);
        }

        if (newValue != null) {
            newValue.getRetrievedEvent().addListener(this);
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
        entityModel.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(RegistrationResult.RetrievedEventDefinition)) {
            asyncResult_Retrieved();
        }
        if (ev.matchesDefinition(ProvideTickEvent.definition)) {
            syncSearch();
        }
    }

    private void asyncResult_Retrieved() {
        // Update IsEmpty flag.

        // Note: Do NOT use IList. 'Items' is not necissarily IList
        // (e.g in Monitor models, the different ListModels' Items are
        // of type 'valueObjectEnumerableList', which is not IList).
        if (getItems() != null) {
            Iterator enumerator = getItems().iterator();
            setIsEmpty(enumerator.hasNext() ? false : true);
        }
        else {
            setIsEmpty(true);
        }
    }

    private void resetIsEmpty() {
        // Note: Do NOT use IList: 'Items' is not necissarily IList
        // (e.g in Monitor models, the different ListModels' Items are
        // of type 'valueObjectEnumerableList', which is not IList).
        if (getItems() != null) {
            Iterator enumerator = getItems().iterator();
            if (enumerator.hasNext()) {
                setIsEmpty(false);
            }
        }
    }

    @Override
    protected void itemsChanged() {
        super.itemsChanged();

        resetIsEmpty();
        updatePagingAvailability();
    }

    @Override
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs<T> e) {
        super.itemsCollectionChanged(sender, e);

        resetIsEmpty();
        updatePagingAvailability();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        /*
        NOTICE, that there's lot of methods overriding this one, looking 'just like' this one, which seems wrong.
        But notice that all those 'updateActionAvailability()' are private, and does not extends each other.
        Making #updateActionAvailability() protected seems not very straightforward, therefore it's not fixed.
        * */
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateReportCommandAvailability(ReportCommand reportCommand, List<?> selectedItems) {
        reportCommand.setIsExecutionAllowed((!reportCommand.isMultiple() && (selectedItems.size() == 1))
                || (reportCommand.isMultiple() && (selectedItems.size() > 1)));
    }

    private void updateActionAvailability() {
        List<?> items =
                getSelectedItems() != null ? getSelectedItems()
                        : Collections.emptyList();

        for (ReportCommand reportCommand : openReportCommands) {
            updateReportCommandAvailability(reportCommand, items);
        }
    }

    protected void updatePagingAvailability() {
        getSearchNextPageCommand().setIsExecutionAllowed(getSearchNextPageCommand().getIsAvailable()
                && getNextSearchPageAllowed());
        getSearchPreviousPageCommand().setIsExecutionAllowed(getSearchPreviousPageCommand().getIsAvailable()
                && getPreviousSearchPageAllowed());
    }

    private void setSearchStringPage(int newSearchPageNumber) {
       this.pagingSearchString = " page " + newSearchPageNumber; //$NON-NLS-1$
       this.currentPageNumber = newSearchPageNumber;
    }

    protected void searchNextPage() {
        searchString = stripPageKeyword(searchString);
        setSearchStringPage(getNextSearchPageNumber());
        getSearchCommand().execute();
    }

    protected void searchPreviousPage() {
        searchString = stripPageKeyword(searchString);
        setSearchStringPage(getPreviousSearchPageNumber());
        getSearchCommand().execute();
    }

    private String stripPageKeyword(String str) {
        int index = str.indexOf("page"); //$NON-NLS-1$
        if (index == -1) {
            return str;
        }
        return str.substring(0, index);
    }

    protected boolean getNextSearchPageAllowed() {
        if (!getSearchNextPageCommand().getIsAvailable() || getItems() == null
                || !getItems().iterator().hasNext()) {
            return false;
        }

        boolean retValue = true;

        int pageSize = getSearchPageSize();

        if (pageSize > 0) {
            if (getItems().size() < pageSize) {
                // current page contains results quantity smaller than
                // the pageSize -> there is no next page:
                retValue = false;
            }
        }

        return retValue;
    }

    protected boolean getPreviousSearchPageAllowed() {
        return getSearchPreviousPageCommand().getIsAvailable() && getSearchPageNumber() > 1;
    }

    /**
     * Override this method to take care on sync fetching.
     * <p>
     * If server-side sorting via the search query is supported by this model:
     * <ul>
     * <li>override {@link #supportsServerSideSorting} to return {@code true}</li>
     * <li>make sure {@code syncSearch} implementation uses {@link #applySortOptions}</li>
     * </ul>
     */
    protected void syncSearch() {
    }

    private String sortBy;
    private boolean sortAscending;

    /**
     * Updates current server-side sort options, performing {@link #refresh} if necessary.
     *
     * @param sortBy
     *            Field to sort by via the search query or {@code null} for undefined sort.
     * @param sortAscending
     *            Sort direction, effective only when {@code sortBy} is not {@code null}.
     */
    public void updateSortOptions(String sortBy, boolean sortAscending) {
        boolean shouldRefresh = !Objects.equals(this.sortBy, sortBy)
                || this.sortAscending != sortAscending;

        this.sortBy = sortBy;
        this.sortAscending = sortAscending;

        if (shouldRefresh) {
            searchString = stripPageKeyword(searchString);
            setSearchStringPage(1);
            refresh();
        }
    }

    /**
     * Clears current server-side sort options.
     */
    public void clearSortOptions() {
        this.sortBy = null;
        this.sortAscending = false;
    }

    /**
     * Returns the given search string with current server-side sort options applied.
     *
     * @param searchString
     *            Search string to update with current server-side sort options.
     */
    protected String applySortOptions(String searchString) {
        String result = searchString;

        if (sortBy != null) {
            result += " " + SyntaxChecker.SORTBY + " " + sortBy //$NON-NLS-1$ //$NON-NLS-2$
                    + " " + (sortAscending ? SyntaxChecker.SORTDIR_ASC : SyntaxChecker.SORTDIR_DESC); //$NON-NLS-1$
        }
        if (result != null && pagingSearchString != null) {
            result += " " + pagingSearchString; //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Returns {@code true} if this model's {@link #syncSearch} implementation supports server-side sorting.
     */
    public boolean supportsServerSideSorting() {
        return false;
    }

    /**
     * Returns {@code true} if this model's {@linkplain #getSearchString search string}
     * allows the use of server-side sorting.
     * <p>
     * This method returns {@code false} if:
     * <ul>
     * <li>search string contains syntax error(s)
     * <li>search string contains {@code SORTBY} syntax object
     * </ul>
     * Otherwise, this method returns {@code true}.
     */
    public boolean isSearchValidForServerSideSorting() {
        ISyntaxChecker syntaxChecker = getConfigurator().getSyntaxChecker();
        if (syntaxChecker == null) {
            return true;
        }

        String search = getSearchString();
        SyntaxContainer syntaxResult = syntaxChecker.analyzeSyntaxState(search, true);

        if (syntaxResult.getError() != SyntaxError.NO_ERROR) {
            return false;
        }

        for (SyntaxObject syntaxObject : syntaxResult) {
            if (syntaxObject.getType() == SyntaxObjectType.SORTBY) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setComparator(Comparator<? super T> comparator, boolean sortAscending) {
        super.setComparator(comparator, sortAscending);

        Collection<T> items = getItems();
        if (items != null) {
            Collection<T> maybeSortedItems = (comparator != null) ? sortItems(items) : new ArrayList<>(items);
            setItems(maybeSortedItems);
        }
    }

    @Override
    public void setItems(Collection<T> value) {
        if (items != value) {
            T lastSelectedItem = getSelectedItem();
            List<T> lastSelectedItems = new ArrayList<>();

            if (getSelectedItems() != null) {
                for (T item : getSelectedItems()) {
                    lastSelectedItems.add(item);
                }
            }

            if (comparator == null || ((value instanceof SortedSet)
                    && Objects.equals(((SortedSet<?>) value).comparator(), comparator))) {
                itemsChanging(value, items);
                items = value;
            } else {
                Collection<T> sortedItems = sortItems(value);
                itemsChanging(sortedItems, items);
                items = sortedItems;
            }

            updatePagingAvailability();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$

            selectedItem = null;

            if (getSelectedItems() != null) {
                getSelectedItems().clear();
            }

            if (lastSelectedItem != null && items != null) {
                T newSelectedItem = null;
                List<T> newItems = new ArrayList<>();

                for (T item : items) {
                    newItems.add(item);
                }

                if (newItems != null) {
                    newSelectedItem = determineSelectedItems(newItems, lastSelectedItem, lastSelectedItems);
                }
                if (newSelectedItem != null) {
                    selectedItem = newSelectedItem;

                    if (selectedItems != null) {
                        selectedItems.add(newSelectedItem);
                    }
                }
            }
            onSelectedItemChanged();
        }
    }

    protected T determineSelectedItems(List<T> newItems, T lastSelectedItem, List<T> lastSelectedItems) {
        T newSelectedItem = null;
        for (T newItem : newItems) {
            // Search for selected item
            if (itemsEqual(newItem, lastSelectedItem)) {
                newSelectedItem = newItem;
            } else {
                // Search for selected items
                for (T item : lastSelectedItems) {
                    if (itemsEqual(newItem, item)) {
                        selectedItems.add(newItem);
                    }
                }
            }
        }
        return newSelectedItem;
    }

    private static <T> boolean itemsEqual(T item1, T item2) {
        if (item1 instanceof IVdcQueryable && item2 instanceof IVdcQueryable) {
            return ((IVdcQueryable) item1).getQueryableId().equals(((IVdcQueryable) item2).getQueryableId());
        }
        return Objects.equals(item1, item2);
    }

    public void syncSearch(VdcQueryType vdcQueryType, VdcQueryParametersBase vdcQueryParametersBase) {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                setItems((Collection<T>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        vdcQueryParametersBase.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(vdcQueryType, vdcQueryParametersBase, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    public void stopRefresh() {
        if (getTimer() != null) {
            //Timer can be null if the event bus hasn't been set yet. If the timer is null we can't stop it.
            getTimer().stop();
        }
    }

    protected void removeTimerChangeHandler() {
        if (timerChangeHandler != null) {
            timerChangeHandler.removeHandler();
            timerChangeHandler = null;
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getSearchCommand()) {
            search();
        }
        else if (command == getSearchNextPageCommand()) {
            searchNextPage();
        }
        else if (command == getSearchPreviousPageCommand()) {
            searchPreviousPage();
        }
        else if (command == getForceRefreshCommand()) {
            forceRefresh();
        } else if (command instanceof ReportCommand) {
            openReport();
        }

        if (command != null && command.isAutoRefresh()) {
            getTimer().fastForward();
        }
    }

    public static final class PrivateAsyncCallback<E, T> {
        private final SearchableListModel<E, T> model;
        private boolean searchRequested;

        public PrivateAsyncCallback(SearchableListModel<E, T> model) {
            this.model = model;
            AsyncQuery _asyncQuery1 = new AsyncQuery();
            _asyncQuery1.setModel(this);
            _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model1, Object result1) {
                    ApplySearchPageSize((Integer) result1);
                }
            };
            AsyncDataProvider.getInstance().getSearchResultsLimit(_asyncQuery1);
        }

        public void requestSearch() {
            searchRequested = true;
            model.setItems(new ArrayList<T>());
            model.getSelectedItemChangedEvent().raise(this, new EventArgs());
            model.getSelectedItemsChangedEvent().raise(this, new EventArgs());
        }

        private void ApplySearchPageSize(int value) {
            model.setSearchPageSize(value);

            // If there search was requested before max result limit was retrieved, do it now.
            if (searchRequested && !model.getTimer().isActive()) {
                model.getSearchCommand().execute();
            }

            // Sure paging functionality.
            model.updatePagingAvailability();
        }
    }

    /**
     * Sub classes that have an edit command will override this method.
     *
     * @return An edit {@code UICommand}
     */
    public UICommand getEditCommand() {
        // Returning null will result in no action. I can't make this
        // method abstract like I want as not all sub classes will
        // implement the edit command.
        return null;
    }

    /**
     * Get the double click command, in most cases this will be 'edit'. If sub
     * classes want a different default command they can override this method
     * and return the command they want.
     *
     * If a user double clicks in a grid or tree, this default command is
     * invoked.
     * @return The default {@code UICommand}
     */
    public UICommand getDoubleClickCommand() {
        return getEditCommand();
    }

    // ////////////////////////////
    // GridController methods
    // ///////////////////////////

    @Override
    public String getId() {
        return getListName();
    }

    protected boolean handleRefreshActiveModel(RefreshActiveModelEvent event) {
        return true;
    }

    protected boolean refreshOnInactiveTimer() {
        return false;
    }

    @Override
    protected void registerHandlers() {
        // Register to listen for operation complete events.
        registerHandler(getEventBus().addHandler(RefreshActiveModelEvent.getType(),
                new RefreshActiveModelHandler() {
            @Override
            public void onRefreshActiveModel(RefreshActiveModelEvent event) {
                if (getTimer().isActive() || refreshOnInactiveTimer()) { // Only if we are active should we refresh.
                    if (handleRefreshActiveModel(event)) {
                        syncSearch();
                    }
                    if (event.isDoFastForward()) {
                        // Start the fast refresh.
                        getTimer().fastForward();
                    }
                }
            }
        }));
    }
}
