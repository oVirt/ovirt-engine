package org.ovirt.engine.ui.common.widget.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Context menu widget providing UI for {@link ColumnController} interface.
 * <p>
 * By design, the API is column-based (instead of index-based) so that
 * menu item operations map directly to GWT {@link Column} objects.
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnContextMenu<T> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, ColumnContextMenu> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel container;

    private final ColumnController<T> controller;
    private final Map<Column<T, ?>, ColumnContextMenuItem<T>> items = new HashMap<>();

    public ColumnContextMenu(ColumnController<T> controller) {
        this.controller = controller;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    /**
     * Add new item to the context menu.
     */
    public void addItem(Column<T, ?> column) {
        if (containsItem(column)) {
            return;
        }

        ColumnContextMenuItem<T> newItem = new ColumnContextMenuItem<>(controller, column);

        items.put(column, newItem);
        container.add(newItem);
    }

    /**
     * Remove item from the context menu.
     */
    public void removeItem(Column<T, ?> column) {
        ColumnContextMenuItem<T> removedItem = items.remove(column);

        if (removedItem != null) {
            container.remove(removedItem);
        }
    }

    /**
     * Check if the context menu contains given item.
     */
    public boolean containsItem(Column<T, ?> column) {
        return items.containsKey(column);
    }

    /**
     * Update context menu items according to the {@link ColumnController}.
     */
    public void update() {
        List<Column<T, ?>> columns = new ArrayList<>(items.keySet());
        Map<Integer, ColumnContextMenuItem<T>> itemsToRender = new TreeMap<>();

        // Collect valid items for rendering, remove invalid items
        for (Column<T, ?> column : columns) {
            int columnIndex = controller.getColumnIndex(column);

            if (columnIndex >= 0) {
                ColumnContextMenuItem<T> item = items.get(column);
                itemsToRender.put(columnIndex, item);
            } else {
                removeItem(column);
            }
        }

        // Render items
        container.clear();
        for (ColumnContextMenuItem<T> item : itemsToRender.values()) {
            container.add(item);
            item.update();
        }
    }

}
