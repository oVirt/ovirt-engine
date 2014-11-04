package org.ovirt.engine.ui.common.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a form panel that renders name/value items organized in columns.
 */
public abstract class AbstractFormPanel extends Composite implements HasElementId {

    protected String elementId = DOM.createUniqueId();

    @UiField
    public HorizontalPanel contentPanel;

    // List of detail views, each one representing a column of form items
    private final List<Grid> detailViews = new ArrayList<Grid>();

    // Used with form item auto placement feature
    private Map<Integer, Integer> nextAvailableRowForColumn = new HashMap<Integer, Integer>();

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
    public void addFormDetailView(int numOfRows) {
        Grid view = new Grid(numOfRows, 2);
        view.setStyleName("formPanel_detailView"); //$NON-NLS-1$
        view.getColumnFormatter().setStyleName(0, "formPanel_detailViewNameColumn"); //$NON-NLS-1$
        view.getColumnFormatter().setStyleName(1, "formPanel_detailViewValueColumn"); //$NON-NLS-1$

        detailViews.add(view);
        contentPanel.add(view);
    }

    /**
     * Adds new item to the form panel.
     */
    public void addFormItem(FormItem item) {
        // Create item label
        Label itemLabel = new Label(item.getName());
        itemLabel.setStyleName("formPanel_detailViewItemName"); //$NON-NLS-1$

        // Add item label
        Grid view = getDetailView(item.getColumn());
        view.setWidget(item.getRow(), 0, itemLabel);

        // Update the item
        updateFormItem(item);

        // Update auto placement data
        incNextAvailableRow(item.getColumn());
    }

    /**
     * Updates the value and visibility of the given item.
     */
    public void updateFormItem(FormItem item) {
        Widget valueWidget = item.resolveValueWidget();
        boolean visible = item.getIsAvailable();

        // Update item value
        valueWidget.setStyleName("formPanel_detailViewItemValue"); //$NON-NLS-1$
        Grid view = getDetailView(item.getColumn());
        view.setWidget(item.getRow(), 1, valueWidget);

        // Update item visibility
        view.getWidget(item.getRow(), 0).setVisible(visible);
        view.getWidget(item.getRow(), 1).setVisible(visible);

        // set item ids
        view.getWidget(item.getRow(), 0).getElement().setId(
                ElementIdUtils.createFormGridElementId(elementId, item.getColumn(), item.getRow(), "_label")); //$NON-NLS-1$
        view.getWidget(item.getRow(), 1).getElement().setId(
                ElementIdUtils.createFormGridElementId(elementId, item.getColumn(), item.getRow(), "_value")); //$NON-NLS-1$
    }

    Grid getDetailView(int column) {
        return detailViews.get(column);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
