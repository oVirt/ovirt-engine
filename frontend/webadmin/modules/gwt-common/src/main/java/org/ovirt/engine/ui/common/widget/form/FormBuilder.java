package org.ovirt.engine.ui.common.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class FormBuilder {

    private final AbstractFormPanel formPanel;
    private final List<FormItem> items = new ArrayList<>();

    // Maps model property names to corresponding items, used to update
    // form items whenever the given model property changes its value
    private final Map<String, List<FormItem>> propertyNameMap = new HashMap<>();

    private EntityModel<?> model;

    public FormBuilder(AbstractFormPanel formPanel, int numOfColumns, int numOfRows) {
        this.formPanel = formPanel;

        formPanel.addFormDetailView(numOfRows, numOfColumns);
    }

    /**
     * Set the relative width of the column in bootstrap grid columns, acceptable values are between 1 and 12.
     * Make sure your total for all columns does not exceed 12 or your results will be unpredictable.
     * @param columnNum The column you want to set the relative width for.
     * @param widthInColumns The number of grid columns you want your column to occupy.
     */
    public void setRelativeColumnWidth(int columnNum, int widthInGridColumns) {
        if (widthInGridColumns < 1 || widthInGridColumns > 12) {
            throw new IllegalArgumentException("The widthInGridColumns has to be between 1 and 12"); //$NON-NLS-1$
        }
        formPanel.setRelativeColumnWidth(columnNum, widthInGridColumns);
    }

    public void addFormItem(FormItem item) {
        addFormItem(item, 6, 6);
    }

    /**
     * Adds new item to the form panel.
     */
    public void addFormItem(FormItem item, int labelWidth, int valueWidth) {
        // Adopt item
        item.setFormPanel(formPanel);

        // Validate and add item
        if (item.isValid()) {
            formPanel.addFormItem(item, labelWidth, valueWidth);
            items.add(item);

            // Update property name mapping, if necessary
            String isAvailablePropertyName = item.getIsAvailablePropertyName();
            if (isAvailablePropertyName != null) {
                getItemsForPropertyName(isAvailablePropertyName).add(item);
            }
        }
    }

    List<FormItem> getItemsForPropertyName(String propertyName) {
        if (!propertyNameMap.containsKey(propertyName)) {
            propertyNameMap.put(propertyName, new ArrayList<FormItem>());
        }
        return propertyNameMap.get(propertyName);
    }

    /**
     * Updates all items within the form panel.
     */
    public void update(EntityModel<?> model) {
        // Detach property change listener from old model
        if (this.model != null) {
            this.model.getPropertyChangedEvent().removeListener(propertyChangedEventListener);
        }

        // Adopt new model
        this.model = model;

        // Attach property change listener to new model
        this.model.getPropertyChangedEvent().addListener(propertyChangedEventListener);

        // Update all form items
        for (FormItem item : items) {
            item.update();
        }
    }

    private final IEventListener<PropertyChangedEventArgs> propertyChangedEventListener = (ev, sender, args) -> {
        String propertyName = args.propertyName;
        List<FormItem> formItems = propertyNameMap.get(propertyName);
        if (formItems != null) {
            for (FormItem item : formItems) {
                item.update();
            }
        }
    };

}
