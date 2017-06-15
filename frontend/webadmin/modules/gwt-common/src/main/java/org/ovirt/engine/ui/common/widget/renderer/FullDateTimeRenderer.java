package org.ovirt.engine.ui.common.widget.renderer;

import java.util.Date;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Standard renderer for Date values in oVirt.
 * TODO: rename OvirtDateTimeRenderer
 */
public class FullDateTimeRenderer extends AbstractRenderer<Date> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private DateTimeFormat formatPattern = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

    /**
     * Create a new FullDateTimeRenderer with a default pattern of 'yyyy-MMM-dddd HH:mm'.
     * (Uses only 'MM' for month is locale is set to Japanese.)
     */
    public FullDateTimeRenderer() {
        this(true);
    }

    /**
     * Create a new FullDateTimeRenderer with a default pattern of 'yyyy-MMM-dddd'.
     * (Uses only 'MM' for month is locale is set to Japanese.) Pass 'true' for includeTime if
     * you want to include the hours and minutes in the date ('yyyy-MMM-dddd HH:mm').
     * Pass 'true' for includeSeconds ('yyyy-MMM-dddd HH:mm:ss') if you want the seconds in the
     * date as well.
     */
    public FullDateTimeRenderer(boolean includeTime) {
        DateTimeFormat newFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
        if (includeTime) {
            newFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
        }
        formatPattern = newFormat;
    }

    @Override
    public String render(Date object) {
        if(object == null){
            return constants.notAvailableLabel();
        }
        return formatPattern.format(object);
    }

}
