package org.ovirt.engine.ui.common.widget;

import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * This model-backed widget may be used to display a list of items horizontally.
 *
 * @param <T> the type of values contained in the backing model
 */
public abstract class AbstractItemListWidget<T extends Nameable> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractItemListWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel itemListPanel;

    @UiField
    Label itemListLabel;

    @UiField
    UnorderedList itemList;

    private ListModel<T> model;

    public AbstractItemListWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void init(ListModel<T> model) {
        this.model = model;
        itemList.addStyleName(Styles.LIST_INLINE);
        refreshItems();
    }

    protected abstract String noItemsText();

    public void refreshItems() {
        itemListPanel.clear();
        createItems();
    }

    public ListModel<T> getModel() {
        return model;
    }

    private void createItems() {
        List<T> selectedItems = getModel().getSelectedItems();
        boolean noItemsSelected = selectedItems == null || selectedItems.isEmpty();

        if (noItemsSelected) {
            Span span = new Span();
            span.setText(noItemsText());
            itemListPanel.add(span);
            return;
        }

        itemList.clear();
        Collections.sort(selectedItems, new NameableComparator());

        selectedItems.forEach(label -> {
            ItemListItem labelListItem = new ItemListItem();
            labelListItem.init(label.getName());
            labelListItem.getDeactivationAnchor().addClickHandler(event -> {
                getModel().getSelectedItems().remove(label);
                refreshItems();
            });
            itemList.add(labelListItem);
        });

        itemListPanel.add(itemList);
    }
}
