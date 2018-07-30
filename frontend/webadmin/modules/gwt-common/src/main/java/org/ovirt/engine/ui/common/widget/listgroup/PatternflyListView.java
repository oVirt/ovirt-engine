package org.ovirt.engine.ui.common.widget.listgroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasDataMinimalDelegate;
import org.ovirt.engine.ui.uicommonweb.models.OvirtSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;

public class PatternflyListView<E, T, M extends SearchableListModel<E, T>> extends ListGroup
    implements ClickHandler {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private List<PatternflyListViewItem<T>> currentState = new ArrayList<>();
    private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    private HasData<T> hasDataDelegate = new HasDataMinimalDelegate<T>() {
        @Override
        public int getRowCount() {
            return currentState.size();
        }

        @Override
        public Iterable<T> getVisibleItems() {
            return model.getItems();
        }
    };

    private M model;
    private PatternflyListViewItemCreator<T> creator;
    private Set<Integer> selectedIndexes = new HashSet<>();
    private HandlerRegistration selectionChangedHandler;

    public PatternflyListView() {
        addStyleName(PatternflyConstants.PF_LIST_VIEW);
        addStyleName(PatternflyConstants.PF_LIST_VIEW_VIEW);
    }

    public void setModel(M model) {
        this.model = model;
        // Remove the handler from the previous model's selection model.
        if (selectionChangedHandler != null) {
            selectionChangedHandler.removeHandler();
        }
        selectionChangedHandler = getSelectionModel().addSelectionChangeHandler(e -> processSelectionChanged());
        getSelectionModel().setDataDisplay(this.hasDataDelegate);
        getSelectionModel().setMultiSelectEnabled(true);

        getModel().getItemsChangedEvent().addListener((ev, sender, args) -> {
            // Update the selection model to match the information from the previous selected model.
            getSelectionModel().clear();
            List<T> items = getModel().getItemsAsList();
            List<Integer> itemsToSelect = selectedIndexes.stream().filter(selectedIndex ->
                selectedIndex > -1 && selectedIndex < items.size()
            ).collect(Collectors.toList());
            itemsToSelect.forEach(index -> getSelectionModel().setSelected(items.get(index), true));
            updateInfoPanel();
        });
    }

    private OvirtSelectionModel<T> getSelectionModel() {
        return getModel().getSelectionModel();
    }

    public M getModel() {
        return model;
    }

    public PatternflyListViewItemCreator<T> getCreator() {
        return creator;
    }

    public void setCreator(PatternflyListViewItemCreator<T> creator) {
        this.creator = creator;
    }

    private void clearClickHandlers() {
        for (HandlerRegistration registration: handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
    }

    private void restoreState(PatternflyListViewItem<T> oldState, PatternflyListViewItem<T> newItem) {
        newItem.restoreStateFromViewItem(oldState);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() instanceof ListGroupItem) {
            PatternflyListViewItem<T> clickedItem = null;
            for (PatternflyListViewItem<T> item : currentState) {
                if (item.asListGroupItem() == event.getSource()) {
                    clickedItem = item;
                    break;
                }
            }
            if (clickedItem != null && !getSelectionModel().isSelected(clickedItem.getEntity())) {
                if (!event.isControlKeyDown() && !event.isShiftKeyDown()) {
                    // A simple click.
                    getSelectionModel().clear();
                    getSelectionModel().setSelected(clickedItem.getEntity(), true);
                } else if (event.isControlKeyDown()) {
                    // A control click
                    getSelectionModel().setSelected(clickedItem.getEntity(),
                            !getSelectionModel().isSelected(clickedItem.getEntity()));
                }
            }
        }
    }

    private void processSelectionChanged() {
        List<T> selectedItems = getSelectionModel().getSelectedObjects();
        selectedIndexes.clear();
        model.getItems().forEach(item -> {
            if (selectedItems.contains(item)) {
                selectedIndexes.add(model.getItemsAsList().indexOf(item));
            }
        });
        updateInfoPanel();
    }

    private void updateInfoPanel() {
        if (getModel().getItems() instanceof List) {
            clearClickHandlers();
            clear();
            int i = 0;
            List<PatternflyListViewItem<T>> newCurrentState = new ArrayList<>();
            Set<Integer> selectedItemsIndexes = new HashSet<>();
            for(T item: getModel().getItems()) {
                if (item == null) {
                    continue;
                }

                PatternflyListViewItem<T> newItem = creator.createListViewItem(item);
                handlerRegistrations.add(newItem.addClickHandler(this));
                if (i < currentState.size()) {
                    restoreState(currentState.get(i), newItem);
                    if (selectedIndexes.contains(i)) {
                        newItem.asListGroupItem().addStyleName(Styles.ACTIVE);
                        selectedItemsIndexes.add(i);
                    }
                }
                newCurrentState.add(newItem);
                add(newItem.asListGroupItem());
                i++;
            }
            selectedIndexes = selectedItemsIndexes;
            currentState.clear();
            currentState = newCurrentState;
            if (getWidgetCount() == 0) {
                // No items found.
                ListGroupItem noItems = new ListGroupItem();
                noItems.addStyleName(Styles.LIST_GROUP_ITEM_HEADING);
                noItems.setText(constants.noItemsToDisplay());
                add(noItems);
            }
        }
    }

    public void expandAll() {
        for (PatternflyListViewItem<T> listViewItem: currentState) {
            listViewItem.toggleExpanded(true);
        }
    }

    public void collapseAll() {
        for (PatternflyListViewItem<T> listViewItem: currentState) {
            listViewItem.toggleExpanded(false);
        }
    }
}
