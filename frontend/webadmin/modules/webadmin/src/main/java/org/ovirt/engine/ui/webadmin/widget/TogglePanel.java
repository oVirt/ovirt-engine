package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * A panel that can be toggled (checked or unchecked), providing visual feedback.
 */
public class TogglePanel extends HorizontalPanel {

    private final ModelBoundCheckBox checkBox;
    private boolean checked;

    public TogglePanel(Model model) {
        super();
        checkBox = new ModelBoundCheckBox(model);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                GWT.log("Checkbox value changed to " + event.getValue());
                setChecked(event.getValue());
            }
        });

        setWidth("100%");
        setHeight("100%");
        setVerticalAlignment(ALIGN_MIDDLE);
        setChecked(false);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        updateToggleStyle();
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    void updateToggleStyle() {
        GWT.log("Updating Style to " + checked);
        getElement().getStyle().setBackgroundColor(checked ? "gray" : "#F3F7FB");
    }

    protected ModelBoundCheckBox getCheckBox() {
        return checkBox;
    }

}
