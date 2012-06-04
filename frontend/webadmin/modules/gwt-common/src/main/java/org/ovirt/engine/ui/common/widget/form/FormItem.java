package org.ovirt.engine.ui.common.widget.form;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class FormItem extends Composite {

    private String name;
    private Widget value;
    private int row;
    private int column;
    private String isVisiblePropertyName;
    private boolean isAvailable;

    public FormItem(String name, Widget value, int row, int column) {
        this.name = name;
        this.value = value;
        this.row = row;
        this.column = column;
        this.isAvailable = true;
    }

    public FormItem(String name, Widget value, int row, int column, String isVisiblePropertyName) {
        this(name, value, row, column);

        // Save the property name for updating visibility when item is set asynchronously
        this.isVisiblePropertyName = isVisiblePropertyName;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Widget getValue() {
        return value;
    }

    public void setValue(Widget value) {
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getIsVisiblePropertyName() {
        return isVisiblePropertyName;
    }

    public void setIsVisiblePropertyName(String isVisiblePropertyName) {
        this.isVisiblePropertyName = isVisiblePropertyName;
    }

    public void setIsAvailable(boolean isAvailable)
    {
        this.isAvailable = isAvailable;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }
}
