package org.ovirt.engine.ui.common.widget.renderer;

import java.util.Date;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Date values using {@code FullDateTimeFormat}.
 */
public class FullDateTimeRenderer extends AbstractRenderer<Date> {

    private static final String japaneseLocale = "ja"; //$NON-NLS-1$
    private static final DateTimeFormat formatJp = DateTimeFormat.getFormat("yyyy-MM-dd, HH:mm"); //$NON-NLS-1$
    private static final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm"); //$NON-NLS-1$

    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public String render(Date object) {
        if(object == null){
            return constants.notAvailableLabel();
        }
        return getLocalizedDateTimeFormat().format(object);
    }

    public static DateTimeFormat getLocalizedDateTimeFormat() {
        if (LocaleInfo.getCurrentLocale().getLocaleName().startsWith(japaneseLocale)) {
            return formatJp;
        }
        return format;
    }

}
