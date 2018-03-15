package org.ovirt.engine.ui.common.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a form panel that renders name/value items organized in columns.
 */
public abstract class AbstractFormPanel extends Composite implements HasElementId {
    interface Style extends CssResource {
        String formPanelLabel();
        String formPanelValue();
    }

    //There can be a max of 12 columns
    private static final int BOOTSTRAP_GRID_SIZE = 12;
    private static final String COL_PREFIX = "xs_"; //$NON-NLS-1$

    /**
     * Stores the label width and item width so we can look them up based on the {@code FormItem}.
     * Index 0 is the label, index 1 is the item width.
     */
    private Map<FormItem, List<Integer>> formSizeMap = new HashMap<>();

    protected String elementId = DOM.createUniqueId();

    @UiField
    public Container container;

    @UiField
    public Style style;

    // Used with form item auto placement feature
    private final Map<Integer, Integer> nextAvailableRowForColumn = new HashMap<>();

    public int getNextAvailableRow(int column) {
        if(!nextAvailableRowForColumn.containsKey(column)) {
            nextAvailableRowForColumn.put(column, 0);
        }
        return nextAvailableRowForColumn.get(column);
    }

    void incNextAvailableRow(int column) {
        int curRow = getNextAvailableRow(column);
        nextAvailableRowForColumn.put(column, curRow + 1);
    }

    /**
     * Adds new detail view (column) to the form panel.
     */
    public void addFormDetailView(int numOfRows, int numOfColumns) {
        container.clear();
        for (int i = 0; i < numOfRows; i++) {
            Row row = createRow(numOfColumns);
            container.add(row);
        }
    }

    public void setRelativeColumnWidth(int columnNum, int widthInGridColumns) {
        for(int i = 0; i < container.getWidgetCount(); i++) {
            Row row = (Row) container.getWidget(i);
            Column column = (Column) row.getWidget(columnNum);
            column.setSize(COL_PREFIX + widthInGridColumns);
        }
    }

    private Row createRow(int numOfColumns) {
        Row row = new Row();
        //Evenly distribute by default.
        String columnSize = COL_PREFIX + (BOOTSTRAP_GRID_SIZE / numOfColumns);
        for (int i = 0; i < numOfColumns; i++) {
            Column column = new Column(columnSize);
            row.add(column);
        }
        return row;
    }

    public void addFormItem(FormItem item) {
        addFormItem(item, 6, 6);
    }

    /**
     * Adds new item to the form panel.
     */
    public void addFormItem(FormItem item, int labelWidth, int valueWidth) {
        updateItemSizes(item, labelWidth, valueWidth);
        // Create item label
        Label itemLabel = new Label(item.getName());
        itemLabel.getElement().setId(ElementIdUtils.createFormGridElementId(elementId, item.getColumn(),
                item.getRow(), "_label")); //$NON-NLS-1$
        itemLabel.setStyleName(style.formPanelLabel());

        Row itemRow = new Row();
        Column labelColumn = new Column(COL_PREFIX + labelWidth);
        labelColumn.add(itemLabel);
        itemRow.add(labelColumn);
        Column itemColumn = findColumn(item.getRow(), item.getColumn());
        if (itemColumn != null) {
            itemColumn.add(itemRow);
        }

        // Update the item
        updateFormItem(item, valueWidth);

        // Update auto placement data
        incNextAvailableRow(item.getColumn());
    }

    private void updateItemSizes(FormItem item, int labelWidth, int valueWidth) {
        List<Integer> sizesList = formSizeMap.get(item);
        if (sizesList == null) {
            sizesList = new ArrayList<>();
            formSizeMap.put(item, sizesList);
        }
        sizesList.clear();
        sizesList.add(labelWidth);
        sizesList.add(valueWidth);
    }

    private Column findColumn(int row, int column) {
        Column result = null;
        IsWidget rowWidget = container.getWidget(row);
        if (rowWidget instanceof Row) {
            IsWidget columnWidget = ((Row) rowWidget).getWidget(column);
            if (columnWidget instanceof Column) {
                result = (Column) columnWidget;
            }
        }
        return result;
    }

    public void updateFormItem(FormItem item) {
        updateFormItem(item, null);
    }

    /**
     * Updates the value and visibility of the given item.
     */
    public void updateFormItem(FormItem item, Integer labelWidth) {
        Widget valueWidget = item.resolveValueWidget();
        valueWidget.getElement().setId(
                ElementIdUtils.createFormGridElementId(elementId, item.getColumn(), item.getRow(), "_value")); //$NON-NLS-1$
        valueWidget.setStyleName(style.formPanelValue());
        boolean visible = item.getIsAvailable();

        Column widgetColumn = findColumn(item.getRow(), item.getColumn());
        if (widgetColumn != null) {
            IsWidget itemCell = widgetColumn.getWidget(0);
            if (itemCell instanceof Row) {
                Row itemCellRow = (Row)itemCell;
                // Update item visibility
                itemCellRow.setVisible(visible);
                if(itemCellRow.getWidgetCount() > 1) {
                    itemCellRow.remove(1); //Clear out old value.
                }
                Column valueColumn = new Column(COL_PREFIX + determineRealLabelWidth(item, labelWidth));
                valueColumn.add(valueWidget);
                itemCellRow.add(valueColumn);
            }
        }
    }

    /**
     * Determine the real width the label should be.
     * @param item The item to determine the width for.
     * @param labelWidth If not null store the label width and return the value. If null look up the value.
     * @return The actual width the label should be, even if not supplied.
     */
    int determineRealLabelWidth(FormItem item, Integer labelWidth) {
        List<Integer> sizesList = formSizeMap.get(item);
        if (labelWidth != null) {
            sizesList.set(1, labelWidth);
        }
        return sizesList.get(1);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
