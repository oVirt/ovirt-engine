package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Style.Float;

public class LeftAlignedUiCommandButton extends UiCommandButton {
    public LeftAlignedUiCommandButton(String label) {
        super(label);
    }

    @Override
    public void addStyleName(String style) {
        super.addStyleName(style);

        // but always float to left side
        getElement().getStyle().setFloat(Float.LEFT);
    }
}
