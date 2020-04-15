package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.core.searchbackend.SyntaxObject;
import org.ovirt.engine.core.searchbackend.SyntaxObjectType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.RegistrationResult;
import org.ovirt.engine.ui.frontend.communication.RefreshActiveModelEvent;
import org.ovirt.engine.ui.uicommonweb.ProvideTickEvent;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.Scheduler;
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
// TODO once all the children of this class will be refactored to use generics, change from <T> to <T extends Queryable>
public abstract class SearchableListModel<E, T> extends SortedListModel<T> implements HasEntity<E>, GridController {
    public static final String MODEL_CHANGE_RELEVANT_FOR_ACTIONS = "model_change_relevant_for_actions"; // $NON-NLS-1$
    private IEventListener<? super PropertyChangedEventArgs> modelChangeRelevantForActionsListener = (ev, sender, args) -> {
        if (args.propertyName.equals(MODEL_CHANGE_RELEVANT_FOR_ACTIONS)) {
            onModelChangeRelevantForActions();
        }
    };

    private static final int UnknownInteger = -1;
    private static final Logger logger = Logger.getLogger(SearchableListModel.class.getName());

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

    public String getModifiedSearchString() {
        if (tags.isEmpty()) {
            return getSearchString();
        } else {
            return getTaggedSearchString();
        }
    }

    private String getTaggedSearchString() {
        StringBuilder tags = new StringBuilder();
        for (String tag: this.tags) {
            tags.append("tag=" + tag); // $NON-NLS-1$
            if (!this.tags.get(this.tags.size() - 1).equals(tag)) {
                tags.append(" OR "); // $NON-NLS-1$
            }
        }
        if (getDefaultSearchString().equalsIgnoreCase(getSearchString())) {
            //Nothing added, we can append the tags.
            return getSearchString() + tags.toString();
        } else {
            //Have a search string with something already, append with OR.
            return getSearchString() + " OR " + tags.toString(); // $NON-NLS-1$
        }
    }

    public void setSearchString(String value) {
        if (!Objects.equals(searchString, value)) {
            searchString = value;
            pagingSearchString = null;
            currentPageNumber = 1;
            searchStringChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SearchString")); //$NON-NLS-1$
        }
    }

    private List<String> tags = new ArrayList<>();

    public void setTagStrings(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public List<String> getTagStrings() {
        return this.tags;
    }

    private boolean caseSensitiveSearch = false;

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

    public int getTotalItemsCount() {
        return -1;
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
        getPropertyChangedEvent().addListener(modelChangeRelevantForActionsListener);
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
            if (!((Queryable) value).getQueryableId().equals(((Queryable) getEntity()).getQueryableId())) {
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

    protected void entityChanging(E newValue, E oldValue) {
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
        timerChangeHandler = timer.addGridTimerStateChangeEventHandler(event -> {
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
        } else {
            stopRefresh();

            if (getIsQueryFirstTime()) {
                setSelectedItem(null);
                setSelectedItems(null);
            }

            if (!getIsTimerDisabled()) {
                setIsQueryFirstTime(true);
                onPropertyChanged(new PropertyChangedEventArgs(PropertyChangedEventArgs.PROGRESS));
                syncSearch();
                setIsQueryFirstTime(false);
                startGridTimer();
            } else {
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
            Scheduler.get().scheduleDeferred(() -> startRefresh());
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

    /**
     * Inform all buttons (including those added by UI extension) that they should update button availability.
     */
    protected void fireModelChangeRelevantForActionsEvent() {
        getPropertyChangedEvent().raise(this, new PropertyChangedEventArgs(MODEL_CHANGE_RELEVANT_FOR_ACTIONS));
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
            Iterator<T> enumerator = getItems().iterator();
            setIsEmpty(enumerator.hasNext() ? false : true);
        } else {
            setIsEmpty(true);
        }
    }

    private void resetIsEmpty() {
        // Note: Do NOT use IList: 'Items' is not necissarily IList
        // (e.g in Monitor models, the different ListModels' Items are
        // of type 'valueObjectEnumerableList', which is not IList).
        if (getItems() != null) {
            Iterator<T> enumerator = getItems().iterator();
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

    protected void searchFirstPage() {
        searchString = stripPageKeyword(searchString);
        setSearchStringPage(1); // First page
        getSearchCommand().execute();
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

    @Override
    public final boolean hasItemsSorted() {
        return (sortBy != null) || super.hasItemsSorted();
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
        if (item1 instanceof Queryable && item2 instanceof Queryable) {
            return ((Queryable) item1).getQueryableId().equals(((Queryable) item2).getQueryableId());
        }
        return Objects.equals(item1, item2);
    }

    protected void syncSearch(
            QueryType queryType,
            QueryParametersBase queryParametersBase,
            AsyncQuery<QueryReturnValue> asyncCallback) {
        queryParametersBase.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(queryType, queryParametersBase, asyncCallback);

        setIsQueryFirstTime(false);
    }

    protected void syncSearch(QueryType queryType, QueryParametersBase queryParametersBase) {
        syncSearch(queryType, queryParametersBase, new SetItemsAsyncQuery());
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
        } else if (command == getSearchNextPageCommand()) {
            searchNextPage();
        } else if (command == getSearchPreviousPageCommand()) {
            searchPreviousPage();
        } else if (command == getForceRefreshCommand()) {
            forceRefresh();
        }

        if (command != null && command.isAutoRefresh()) {
            getTimer().fastForward();
        }
    }

    protected void onModelChangeRelevantForActions() {
        // no-op by default. Override if needed.
    }

    public static final class PrivateAsyncCallback<E, T> {
        private final SearchableListModel<E, T> model;
        private boolean searchRequested;

        public PrivateAsyncCallback(SearchableListModel<E, T> model) {
            this.model = model;
            AsyncDataProvider.getInstance().getSearchResultsLimit(model.asyncQuery(result -> ApplySearchPageSize(result)));
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

    protected class SetItemsAsyncQuery extends AsyncQuery<QueryReturnValue> {
        public SetItemsAsyncQuery() {
            super(new AsyncCallback<QueryReturnValue>() {
                @Override
                public void onSuccess(QueryReturnValue returnValue) {
                    setItems((Collection<T>) returnValue.getReturnValue());
                }
            });
        }
    }

    protected class SetRawItemsAsyncQuery extends AsyncQuery<List<T>> {
        public SetRawItemsAsyncQuery() {
            super(new AsyncCallback<List<T>>() {
                @Override
                public void onSuccess(List<T> returnValue) {
                    setItems(returnValue);
                }
            });
        }
    }

    protected class SetSortedItemsAsyncQuery extends AsyncQuery<QueryReturnValue> {
        public SetSortedItemsAsyncQuery(final Comparator<? super T> comparator) {
            super(new AsyncCallback<QueryReturnValue>() {
                @Override
                public void onSuccess(QueryReturnValue returnValue) {
                    List<T> items = returnValue.getReturnValue();
                    Collections.sort(items, comparator);
                    setItems(items);
                }
            });
        }
    }

    protected class SetSortedRawItemsAsyncQuery extends AsyncQuery<List<T>> {
        public SetSortedRawItemsAsyncQuery(final Comparator<? super T> comparator) {
            super(new AsyncCallback<List<T>>() {
                @Override
                public void onSuccess(List<T> items) {
                    Collections.sort(items, comparator);
                    setItems(items);
                }
            });
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

    @Override
    protected boolean isSingleSelectionOnly() {
        // Main list models (ones with details) will have multi-selection models.
        return false;
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
                event -> {
                    if (getTimer().isActive() || refreshOnInactiveTimer()) { // Only if we are active should we refresh.
                        if (handleRefreshActiveModel(event)) {
                            syncSearch();
                        }
                        if (event.isDoFastForward()) {
                            // Start the fast refresh.
                            getTimer().fastForward();
                        }
                    }
                }));
    }

    @Override
    public boolean isEntityPresent() {
        return true;
    }

    @Override
    public void setEntityPresent(boolean flag) {
        // not used
    }
}
