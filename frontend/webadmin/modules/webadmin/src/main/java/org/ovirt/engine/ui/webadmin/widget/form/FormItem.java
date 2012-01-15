package org.ovirt.engine.ui.webadmin.widget.form;

import org.ovirt.engine.ui.webadmin.widget.label.TextBoxLabel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class FormItem extends Composite {

    private String name;
    private Widget value;
    private int row;
    private int column;
    private String isVisiblePropertyName;

    public FormItem(String name, Widget value, int row, int column) {
        this.name = name;
        this.value = value;
        this.row = row;
        this.column = column;
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
        TextBoxLabel textBoxLabel = new TextBoxLabel(value.toString());
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

}
