package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Date values using {@code GeneralDateTimeFormat}.
 */
public class GeneralDateTimeRenderer extends AbstractRenderer<Date> {

    private static final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm");

    @Override
    public String render(Date object) {
        return format.format(object);
    }

}
