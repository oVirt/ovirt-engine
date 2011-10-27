package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class EmptyValueRenderer<T> extends AbstractRenderer<T> {

    private static String unAvailablePropertyLabel;

    public EmptyValueRenderer() {
        unAvailablePropertyLabel =
                ClientGinjectorProvider.instance().getApplicationConstants().unAvailablePropertyLabel();
    }

    @Override
    public String render(T value) {
        return value != null && !value.equals("") ? value.toString() : unAvailablePropertyLabel;
    }
}
