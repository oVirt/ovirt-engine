package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.common.widget.ModelBoundCheckBox;
import org.ovirt.engine.ui.uicommonweb.models.Model;

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
        getElement().getStyle().setBackgroundColor(checked ? "#C3D0E0" : "#F3F7FB");
    }

    protected ModelBoundCheckBox getCheckBox() {
        return checkBox;
    }

}
