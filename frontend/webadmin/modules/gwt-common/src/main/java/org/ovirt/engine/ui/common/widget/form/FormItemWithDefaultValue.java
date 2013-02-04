package org.ovirt.engine.ui.common.widget.form;

import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;

public class FormItemWithDefaultValue extends FormItem {
    private TextBoxLabel defaultValue;
    private Condition condition;

    /**
     *
     * @param name
     *            - name of the field
     * @param value
     *            - value of the field if condition if true
     * @param row
     *            - row in the form
     * @param column
     *            - column in the form
     * @param defaultValue
     *            - default value (used if condition if false)
     * @param condition
     *            - condition
     */
    public FormItemWithDefaultValue(String name,
            Widget value,
            int row,
            int column,
            String defaultValue,
            Condition condition) {
        super(name, value, row, column);
        this.defaultValue = new TextBoxLabel(defaultValue);
        this.condition = condition;
    }

    @Override
    public Widget getValue() {
        return condition.isTrue() ? super.getValue() : defaultValue;
    }

    public interface Condition {
        boolean isTrue();
    }
}
