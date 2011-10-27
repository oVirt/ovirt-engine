package org.ovirt.engine.ui.userportal.client.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.client.events.SelectedItemChangedEventArgs;

public abstract class GridController<T> implements IEventListener {
	
	private HashMap<Object,GridElement<T>> gridElements = new HashMap<Object,GridElement<T>>();

	protected GridElement<T> selectedElement;
	
	private SearchableListModel model;
	
	private GridTimer searchTimer;

	private Event selectionChangedEvent = new Event("SelectionChanged", GridController.class);
	private Event itemsChangedEvent = new Event("RefreshSelectedItemDetails", GridController.class);
	private SelectedItemChangedEventArgs eventArgs = new SelectedItemChangedEventArgs(null);

	// Event that is raised upon changed selection status (Initial item was selected / Selected item was de-selected)
	private Event selectionStatusChangedEvent = new Event("SelectionStatusChanged", GridController.class);
	public static PropertyChangedEventArgs ITEM_SELECTED_EVENT_ARGS = new PropertyChangedEventArgs("ItemSelected");
	public static PropertyChangedEventArgs NO_ITEM_SELECTED_EVENT_ARGS = new PropertyChangedEventArgs("NoItemSelected");
	
	private static String GRID_REFRESH_TIMER_NAME = "GRID_REFRESH_TIMER_";	
	private static int MIN_REFRESH_INTERVAL_IN_MILLIS = 2000;
	private static int MAX_REFRESH_INTERVAL_IN_MILLIS = 8000;
	private static int[] REPETITIONS_PER_INTERVAL = new int[] { 3, 30, 3 };
	private static int INTERVAL_MULTIPLIER = 2;
	
	private int repetitionCounter = 0;		
	private String gridName = this.getClass().getName();
	private boolean isRapidTimerRunning = false;
	private boolean selectDefaultValue = false;
	
	// Grid's refresh rate (in seconds)
	// Each controller holds this value for supporting different refresh rates for every grid
	// [not needed now, but might be requested]
	private int refreshRate = GridRefreshManager.getInstance().getGlobalRefreshRate();
		
    protected void handleResults(Iterable<T> items) {
		int position = 0;
		
		ArrayList<Object> existingVMs = new ArrayList<Object>();
		for (T item : items) {
			Object itemId = getId(item);
			
			// Add the item to the list of existing items to check for removed ones later
			existingVMs.add(itemId);

			// Set the selected item again after each search since the search initializes it
			if(selectedElement != null)
				if (itemId.equals(selectedElement.getItemId())) {
					model.setSelectedItem(getSelectedItem());
					model.setSelectedItems(Arrays.asList(getSelectedItem()));
				}

			// If the item doesn't exist in the grid, add it to the grid and create the component
			if (!gridElements.keySet().contains(itemId)) {
				gridElements.put(itemId, addItem(item, position));
			}
			else { // The item exist, check for updates in the values
				gridElements.get(itemId).updateValues(item);
			}
			position++;
		}
		
		// Compare the list of items in the grid to the items returned from the search to check if items were removed
		ArrayList<Object> elementsToRemove = new ArrayList<Object>();
		for (Object id : gridElements.keySet()) {
			if (!existingVMs.contains(id)) {
				elementsToRemove.add(id);
				// If the removed item was the selected item, deselect it
				if (selectedElement != null && selectedElement.equals(gridElements.get(id)))
					select(null);
			}
		}		
	
		for (Object id : elementsToRemove) {
			GWT.log("Removing item with id : " + id);
			removeItem(gridElements.get(id));
			gridElements.remove(id);
		}
		
		if (selectDefaultValue && selectedElement == null && !gridElements.isEmpty()) {
			select(gridElements.get(getId(items.iterator().next())));
		}
	}
	
	public void select(GridElement<T> elementToSelect) {
		if (selectedElement != null) {
			// Abort if the item to select is already select
			if (selectedElement.equals(elementToSelect))
				return;
			
			selectedElement.deselect();
		}
		else {
			// Initial item selected, raise selection status changed event
			selectionStatusChangedEvent.raise(this, ITEM_SELECTED_EVENT_ARGS);
		}
		
		selectedElement = elementToSelect;

		if (selectedElement != null) {
			selectedElement.select();
		}
		else {
			// Selection changed to null, raise selection status changed event
			selectionStatusChangedEvent.raise(this, NO_ITEM_SELECTED_EVENT_ARGS);
		}

		T item = selectedElement == null ? null : getItemById(selectedElement.getItemId());
		
		model.setSelectedItem(item);
		ArrayList<T> items = new ArrayList<T>();
		items.add(item);
		model.setSelectedItems(items);
		eventArgs.selectedItem = item;
			
		selectionChangedEvent.raise(this, eventArgs);
	}
	
	private T getItemById(Object id) {
		if (id == null)
			return null;

		for (T item : (List<T>)model.getItems()) {
			if (getId(item).equals(id)) {
				return item;
			}
		}
		return null;
	}
	
	long d;

	public void search() {
		model.Search();
	}

	public void repeatSearch(int intervalInMillis) {
		repeatSearch(intervalInMillis, intervalInMillis, 1);
	}	
	
	@SuppressWarnings("unchecked")
    final public void repeatSearch(int intervalInMillis, final int maxIntervalInMillis, final int intervalMultiplier) {	    
	    if (searchTimer != null) {
	        searchTimer.cancel();	        
	    }
	    
	    if (intervalInMillis == MIN_REFRESH_INTERVAL_IN_MILLIS) {
	        repetitionCounter = 0;	        
	    }
	    
        searchTimer = (GridTimer)UserPortalTimerFactory.factoryTimer(GRID_REFRESH_TIMER_NAME + model.getClass().getName(), new GridTimer() {
            @Override
            public void run() {
                GWT.log("Invoking grid timer for the grid " + gridName + ", intervalInMillis:" + intervalInMillis + ", has repetitions: " + hasRepetitions + (hasRepetitions ? ", remaining repetitions: " + repetitions : ""));
                
                // Handle timer with repetitions
                if (hasRepetitions) {
                    repetitions--;
                    if (repetitions < 1) {                            
                        // Cancel current grid timer
                        cancel();                        
                        
                        // Schedule another repetition with a higher interval                            
                        repeatSearch(intervalInMillis * intervalMultiplier, maxIntervalInMillis, intervalMultiplier);                                                         
                    }                        
                }
                search();
            }
        });   
        
	    // Set timer's repetitions (if needed)
        int repetitionsPerInterval = REPETITIONS_PER_INTERVAL[repetitionCounter];
	    if (repetitionsPerInterval > 0) {
            searchTimer.repetitions = repetitionsPerInterval;            
            searchTimer.hasRepetitions = true;
            
            if (repetitionCounter == REPETITIONS_PER_INTERVAL.length - 1) repetitionCounter = 0;
            else repetitionCounter++;
        }
        else {
            searchTimer.hasRepetitions = false;
        }
        
        // Set timer's interval
        searchTimer.intervalInMillis = intervalInMillis;
        
        // Start timer (if needed)
        if (intervalInMillis <= maxIntervalInMillis) {   
            searchTimer.scheduleRepeating(intervalInMillis);
            if (intervalMultiplier > 1) isRapidTimerRunning = true;
        }
        else if (GridRefreshManager.getInstance().isSubscribed(this)) {
            // If this controller is subscribed to refresh manager,
            // start repeat search again with its defined refresh rate
            isRapidTimerRunning = false;
            GridRefreshManager.getInstance().refreshGrids();            
        }
	}
	
	public void stopRepeatedSearch() {
		if (searchTimer != null)
			searchTimer.cancel();
	}
	
	public boolean isRapidTimerRunning() {
	    return isRapidTimerRunning;
	}

	// Should be called whenever an action that has an impact on the grid was performed
	public void gridChangePerformed() {
		GridRefreshManager.getInstance().suspendRefresh();
	    repeatSearch(MIN_REFRESH_INTERVAL_IN_MILLIS, MAX_REFRESH_INTERVAL_IN_MILLIS, INTERVAL_MULTIPLIER);
	}
	
	public void setModel(SearchableListModel model) {
		this.model = model;
		model.getItemsChangedEvent().addListener(this);
	}

    public void eventRaised(Event ev, Object sender, EventArgs args) {
    	handleResults(model.getItems());
    	itemsChangedEvent.raise(this, EventArgs.Empty);
    }

    public T getSelectedItem() {
    	if (selectedElement != null) {
    		return getItemById(selectedElement.getItemId());
    	}
    	return null;
    }

    public GridElement<T> getSelectedItemLayout() {
    	if (selectedElement != null) {
    		return gridElements.get(selectedElement.getItemId());
    	}
    	return null;
    }

	public Event getSelectionChangedEvent() {
		return selectionChangedEvent;
	}
	
	public Event getItemsChangedEvent() {
		return itemsChangedEvent;
	}

	public Event getSelectionStatusChangedEvent() {
		return selectionStatusChangedEvent;
	}
	
	public void setSelectDefaultValue(boolean selectDefaultValue) {
		this.selectDefaultValue = selectDefaultValue;
	}
	
	public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }
	
	public abstract Object getId(T item);
		
	public abstract GridElement<T> addItem(T item, int position);
	
	public abstract void removeItem(GridElement<T> itemView);

	abstract class GridTimer extends Timer {		
		public int intervalInMillis;
		public int repetitions;
        public boolean hasRepetitions;
	}
}
