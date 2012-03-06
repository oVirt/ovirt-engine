package org.ovirt.engine.ui.common.widget.renderer;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Date values using {@code FullDateTimeFormat}.
 */
public class FullDateTimeRenderer extends AbstractRenderer<Date> {

    private static final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm:ss");

    @Override
    public String render(Date object) {
        if(object == null){
            return "";
        }
        return format.format(object);
    }

}
