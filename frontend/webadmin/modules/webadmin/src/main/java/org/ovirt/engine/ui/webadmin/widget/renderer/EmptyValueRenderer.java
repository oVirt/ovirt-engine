package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class EmptyValueRenderer<T> extends AbstractRenderer<T> {

    private String unAvailablePropertyLabel = "";

    boolean showUnAvailableLabel;

    public EmptyValueRenderer() {
    }

    public EmptyValueRenderer(boolean showUnAvailableLabel) {
        if (showUnAvailableLabel) {
            unAvailablePropertyLabel =
                    ClientGinjectorProvider.instance().getApplicationConstants().unAvailablePropertyLabel();
        }
    }

    @Override
    public String render(T value) {
        return value != null && !value.equals("") ? value.toString() : unAvailablePropertyLabel;
    }
}
