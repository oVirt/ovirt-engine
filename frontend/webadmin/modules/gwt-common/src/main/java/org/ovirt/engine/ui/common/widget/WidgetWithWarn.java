package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.WarnIcon;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class WidgetWithWarn extends WidgetWithTooltippedIcon {

    public WidgetWithWarn(Widget contentWidget) {
        this(contentWidget, SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    public WidgetWithWarn(Widget contentWidget, String warnText) {
        this(contentWidget, SafeHtmlUtils.fromString(warnText));
    }

    public WidgetWithWarn(Widget contentWidget, SafeHtml warnText) {
        super(contentWidget, WarnIcon.class, warnText);
    }
}
