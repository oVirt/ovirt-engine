package org.ovirt.engine.ui.common.widget.listgroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

public class PatternflyListView<E, T, M extends SearchableListModel<E, T>> extends ListGroup implements ClickHandler {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private List<PatternflyListViewItem<T>> currentState;
    private List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    private M model;
    private PatternflyListViewItemCreator<T> creator;
    private OrderedMultiSelectionModel<T> selectionModel;
    private int selectedIndex = -1;

    public PatternflyListView() {
        addStyleName(PatternflyConstants.PF_LIST_VIEW);
        addStyleName(PatternflyConstants.PF_LIST_VIEW_VIEW);
    }

    public void setModel(M model) {
        this.model = model;
        // Add selection listener
        getModel().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateInfoPanel());

        getModel().getItemsChangedEvent().addListener((ev, sender, args) -> updateInfoPanel());
    }

    public void setSelectionModel(OrderedMultiSelectionModel<T> selectionModel) {
        this.selectionModel = selectionModel;
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
        String[] styles = oldState.getStyleName().split(" "); // $NON-NLS-1$
        if (Arrays.asList(styles).contains(Styles.ACTIVE)) {
            newItem.addStyleName(Styles.ACTIVE);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeCurrentDisplayState() {
        if (this.currentState == null) {
            currentState = new ArrayList<>();
        }
        if (getWidgetCount() > 0) {
            for (int i = 0; i < getWidgetCount(); i++) {
                IsWidget widget = getWidget(i);
                if (widget instanceof PatternflyListViewItem) {
                    if (i < currentState.size()) {
                        currentState.set(i, (PatternflyListViewItem<T>)widget);
                    } else {
                        currentState.add((PatternflyListViewItem<T>)widget);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() instanceof PatternflyListViewItem) {
            PatternflyListViewItem<T> item = (PatternflyListViewItem<T>)event.getSource();
            for (int i = 0; i < getWidgetCount(); i++) {
                IsWidget widget = getWidget(i);
                widget.asWidget().removeStyleName(Styles.ACTIVE);
            }
            if (!event.isControlKeyDown()) {
                item.addStyleName(Styles.ACTIVE);
                selectionModel.setSelected(item.getEntity(), true);
                selectedIndex = selectionModel.getLastSelectedRow();
                getModel().setSelectedItem(item.getEntity());
            } else {
                selectionModel.setSelected(item.getEntity(), false);
                selectedIndex = -1;
                getModel().setSelectedItem(null);
            }
        }
    }

    private void updateInfoPanel() {
        if (getModel().getItems() instanceof List) {
            storeCurrentDisplayState();
            clearClickHandlers();
            selectionModel.clear();
            clear();
            int i = 0;
            for(T item: getModel().getItems()) {
                PatternflyListViewItem<T> newItem = creator.createListViewItem(item);
                handlerRegistrations.add(newItem.addClickHandler(this));
                if (i < currentState.size()) {
                    restoreState(currentState.get(i), newItem);
                    if (i == selectedIndex) {
                        newItem.addStyleName(Styles.ACTIVE);
                    }
                }
                add(newItem);
                i++;
            }
            if (getWidgetCount() == 0) {
                // No items found.
                ListGroupItem noItems = new ListGroupItem();
                noItems.addStyleName(Styles.LIST_GROUP_ITEM_HEADING);
                noItems.setText(constants.noItemsToDisplay());
                add(noItems);
            }
            restoreSelection(selectedIndex);
        }
    }

    private void restoreSelection(int index) {
        if (getModel().getItems() instanceof List && index >= 0 && index < getModel().getItems().size()) {
            selectionModel.setSelected(((List<T>)getModel().getItems()).get(index), true);
        } else {
            getModel().setSelectedItem(null);
            getModel().setSelectedItems(null);
        }
    }
}
