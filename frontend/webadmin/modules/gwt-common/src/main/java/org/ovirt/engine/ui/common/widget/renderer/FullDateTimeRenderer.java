package org.ovirt.engine.ui.common.widget.renderer;

import java.util.Date;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Standard renderer for Date values in oVirt.
 * TODO: rename OvirtDateTimeRenderer
 */
public class FullDateTimeRenderer extends AbstractRenderer<Date> {

    private static final String japaneseLocale = "ja"; //$NON-NLS-1$

    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    private DateTimeFormat formatter;

    /**
     * Create a new FullDateTimeRenderer with a default pattern of 'yyyy-MMM-dddd HH:mm'.
     * (Uses only 'MM' for month is locale is set to Japanese.)
     */
    public FullDateTimeRenderer() {
        this(true, false);
    }

    /**
     * Create a new FullDateTimeRenderer with a default pattern of 'yyyy-MMM-dddd'.
     * (Uses only 'MM' for month is locale is set to Japanese.) Pass 'true' for includeTime if
     * you want to include the hours and minutes in the date ('yyyy-MMM-dddd HH:mm').
     * Pass 'true' for includeSeconds ('yyyy-MMM-dddd HH:mm:ss') if you want the seconds in the
     * date as well.
     */
    public FullDateTimeRenderer(boolean includeTime, boolean includeSeconds) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("yyyy-MM"); //$NON-NLS-1$
        if (!LocaleInfo.getCurrentLocale().getLocaleName().startsWith(japaneseLocale)) {
            pattern.append("M"); // add another M for non-Japanese //$NON-NLS-1$
        }
        pattern.append("-dd"); //$NON-NLS-1$
        if (includeTime) {
            pattern.append(", HH:mm"); //$NON-NLS-1$
            if (includeSeconds) {
                pattern.append(":ss"); //$NON-NLS-1$
            }
        }
        formatter = DateTimeFormat.getFormat(pattern.toString());
    }

    @Override
    public String render(Date object) {
        if(object == null){
            return constants.notAvailableLabel();
        }
        return formatter.format(object);
    }

}
