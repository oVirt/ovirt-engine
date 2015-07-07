package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Check box widget bound to UiCommon model "IsSelected" property.
 */
public class ModelBoundCheckBox extends CheckBox {

    private final Model model;

    public ModelBoundCheckBox(Model model) {
        this(model, false);
    }

    /**
     * When the readOnly is true, the checkbox only reads the value from model, but does not modify the model itself.
     */
    public ModelBoundCheckBox(Model model, boolean readOnly) {
        super();
        this.model = model;
        registerModelPropertyChangeListener();
        if (!readOnly) {
            registerChangeHandler();
        }
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
        model.getPropertyChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                boolean isSelected = model.getIsSelected();
                ModelBoundCheckBox.super.setValue(isSelected, true);
            }
        });
    }

}
