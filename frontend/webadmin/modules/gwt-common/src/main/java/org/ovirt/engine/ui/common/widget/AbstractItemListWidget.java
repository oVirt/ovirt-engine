package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
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
 * @param <M> the model backing this widget
 * @param <T> the type of values contained in the backing model
 */
public abstract class AbstractItemListWidget<M extends ListModel<T>, T> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractItemListWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel itemListPanel;

    @UiField
    Label itemListLabel;

    @UiField
    UnorderedList itemList;

    private M model;

    public void init(M model) {
        this.model = model;
        itemList.addStyleName(Styles.LIST_INLINE);
        createItems();
    }

    protected abstract void createItems();

    public void refreshItems() {
        itemListPanel.clear();
        createItems();
    }

    public M getModel() {
        return model;
    }
}
