package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * A panel that can be toggled (checked or unchecked), providing visual feedback.
 */
public class TogglePanel extends HorizontalPanel {

    private final ModelBoundCheckBox checkBox;
    private boolean checked;

    public TogglePanel(final Model model) {
        super();

        checkBox = new ModelBoundCheckBox(model, false);

        setWidth("100%"); //$NON-NLS-1$
        setHeight("100%"); //$NON-NLS-1$
        setVerticalAlignment(ALIGN_MIDDLE);
        setChecked(false);

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsSelected".equals(propName)) { //$NON-NLS-1$
                setChecked(model.getIsSelected());
            }
        });
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
        getElement().getStyle().setBackgroundColor(checked ? "#C3D0E0" : "#F3F7FB"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected ModelBoundCheckBox getCheckBox() {
        return checkBox;
    }

}
