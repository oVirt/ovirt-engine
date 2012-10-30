package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.Date;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Date values using {@code GeneralDateTimeFormat}.
 */
public class GeneralDateTimeRenderer extends AbstractRenderer<Date> {
    private static final CommonApplicationConstants CONSTANTS = GWT.create(CommonApplicationConstants.class);

    @Override
    public String render(Date object) {
        if (object == null) {
            return CONSTANTS.notAvailableLabel();
        }
        return FullDateTimeRenderer.getLocalizedDateTimeFormat().format(object);
    }

}
