package org.ovirt.engine.ui.common.widget.form;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;

import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a name/value item rendered within an {@link AbstractFormPanel}.
 */
public class FormItem {

    /**
     * If {@link #showDefaultValue} returns {@code true}, show the default value, otherwise show the actual value.
     */
    public interface DefaultValueCondition {

        boolean showDefaultValue();

    }

    private AbstractFormPanel formPanel;

    private final int row;
    private final int column;

    private final String isAvailablePropertyName;
    private boolean isAvailable = true;

    private String name;
    private Widget valueWidget;

    private TextBoxLabel defaultValueLabel;
    private DefaultValueCondition defaultValueCondition;

    public FormItem(int row, int column) {
        this("", new TextBoxLabel(), row, column); //$NON-NLS-1$
    }

    public FormItem(String name, Widget valueWidget, int row, int column) {
        this(name, valueWidget, row, column, null);
    }

    public FormItem(String name, Widget valueWidget, int row, int column, String isAvailablePropertyName) {
        this.row = row;
        this.column = column;
        this.isAvailablePropertyName = isAvailablePropertyName;
        this.valueWidget = valueWidget;

        // Add trailing ':' to the item name, if necessary
        if (name != null && !name.trim().isEmpty() && !name.endsWith(":")) { //$NON-NLS-1$
            this.name = name + ":"; //$NON-NLS-1$
        } else {
            this.name = name;
        }
    }

    public FormItem withDefaultValue(String defaultValue, DefaultValueCondition defaultValueCondition) {
        this.defaultValueLabel = new TextBoxLabel(defaultValue);
        this.defaultValueCondition = defaultValueCondition;
        return this;
    }

    public void setFormPanel(AbstractFormPanel formPanel) {
        this.formPanel = formPanel;
    }

    public void update() {
        assert formPanel != null : "FormItem must be adopted by FormPanel"; //$NON-NLS-1$
        formPanel.updateFormItem(this);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getIsAvailablePropertyName() {
        return isAvailablePropertyName;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
        update();
    }

    public String getName() {
        return name;
    }

    public Widget getValueWidget() {
        return valueWidget;
    }

    /**
     * Resolves the appropriate value widget for this form item based on {@link DefaultValueCondition} (if any).
     */
    public Widget resolveValueWidget() {
        boolean showDefaultValue = defaultValueCondition != null && defaultValueCondition.showDefaultValue();
        return showDefaultValue ? defaultValueLabel : valueWidget;
    }

}
