package org.ovirt.engine.ui.common.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class FormBuilder {

    private final AbstractFormPanel formPanel;
    private final List<FormItem> items = new ArrayList<FormItem>();

    // Maps model property names to corresponding items, used to update
    // form items whenever the given model property changes its value
    private final Map<String, List<FormItem>> propertyNameMap = new HashMap<String, List<FormItem>>();

    private EntityModel model;

    public FormBuilder(AbstractFormPanel formPanel, int numOfColumns, int numOfRows) {
        this.formPanel = formPanel;

        // Add columns to the form panel
        for (int i = 0; i < numOfColumns; i++) {
            formPanel.addFormDetailView(numOfRows);
        }
    }

    /**
     * Adds new item to the form panel.
     */
    public FormItem addFormItem(FormItem item) {
        formPanel.addFormItem(item);
        items.add(item);

        // Update property name mapping, if necessary
        String isAvailablePropertyName = item.getIsAvailablePropertyName();
        if (isAvailablePropertyName != null) {
            getItemsForPropertyName(isAvailablePropertyName).add(item);
        }

        return item;
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
    public void update(EntityModel model) {
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

    private final IEventListener propertyChangedEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            String propertyName = ((PropertyChangedEventArgs) args).PropertyName;
            List<FormItem> formItems = propertyNameMap.get(propertyName);
            if (formItems != null) {
                for (FormItem item : formItems) {
                    item.update();
                }
            }
        }
    };

}
