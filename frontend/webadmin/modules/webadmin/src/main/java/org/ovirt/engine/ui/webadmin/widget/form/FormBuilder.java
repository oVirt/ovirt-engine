package org.ovirt.engine.ui.webadmin.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class FormBuilder {

    // A panel in which the builder assemble FormItems
    AbstractFormPanel formPanel;

    // A list of available FormItems
    List<FormItem> formItems = new ArrayList<FormItem>();

    // A map between property name and a FormItem
    HashMap<String, FormItem> propertyNamesMap = new HashMap<String, FormItem>();

    @SuppressWarnings("rawtypes")
    SimpleBeanEditorDriver driver;
    EntityModel model;

    public FormBuilder(AbstractFormPanel formPanel, int numOfColumns, int numOfRows) {
        this.formPanel = formPanel;

        // Add columns of form items formatted bt the specified dimensions
        for (int column = 0; column < numOfColumns; column++) {
            formPanel.addFormDetailView(numOfRows);
        }
    }

    public void addFormItem(FormItem item) {
        // Add the specified item to the list
        formItems.add(item);

        // Add the item to the map if needed
        if (item.getIsVisiblePropertyName() != null) {
            propertyNamesMap.put(item.getIsVisiblePropertyName(), item);
        }
    }

    @SuppressWarnings("rawtypes")
    public void showForm(EntityModel model, SimpleBeanEditorDriver driver) {
        this.model = model;
        this.driver = driver;

        // Clear the form panel
        formPanel.clear();

        // Add each FormItem to the form panel iff the item should be visible
        for (FormItem item : formItems) {
            if (item.isVisible()) {
                formPanel.addFormItem(item);
            }
        }

        model.getPropertyChangedEvent().removeListener(propertyChangedEventListener);
        model.getPropertyChangedEvent().addListener(propertyChangedEventListener);
    }

    private final IEventListener propertyChangedEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {

            String propertyName = ((PropertyChangedEventArgs) args).PropertyName;
            if (propertyNamesMap.containsKey(propertyName)) {
                FormItem item = propertyNamesMap.get(propertyName);

                // Update the item that is correlated to the property name:
                // remove the old item and add the new one iff should be visible
                formPanel.removeFormItem(item);
                if (item.isVisible()) {
                    formPanel.addFormItem(item);
                }
            }

        }
    };

    public void setColumnsWidth(String... columnsWidth) {
        formPanel.setColumnsWidth(columnsWidth);
    }

}
