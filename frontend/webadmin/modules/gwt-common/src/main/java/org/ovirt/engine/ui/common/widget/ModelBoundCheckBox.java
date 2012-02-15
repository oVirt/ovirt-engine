package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Check box widget bound to UiCommon model "IsSelected" property.
 */
public class ModelBoundCheckBox extends CheckBox {

    private final Model model;

    public ModelBoundCheckBox(Model model) {
        super();
        this.model = model;
        registerModelPropertyChangeListener();
        registerChangeHandler();
    }

    @Override
    public void setValue(Boolean value) {
        super.setValue(value);
        model.setIsSelected(value);
    }

    private void registerChangeHandler() {
        addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                model.setIsSelected(event.getValue());
            }
        });
    }

    private void registerModelPropertyChangeListener() {
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isSelected = model.getIsSelected();
                ModelBoundCheckBox.super.setValue(isSelected, true);
            }
        });
    }

}
