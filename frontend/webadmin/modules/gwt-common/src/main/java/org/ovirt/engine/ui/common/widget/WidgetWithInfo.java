package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class WidgetWithInfo extends WidgetWithTooltippedIcon {

    public WidgetWithInfo(Widget contentWidget) {
        this(contentWidget, SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    public WidgetWithInfo(Widget contentWidget, String infoText) {
        this(contentWidget, SafeHtmlUtils.fromString(infoText));
    }

    public WidgetWithInfo(Widget contentWidget, SafeHtml infoText) {
        super(contentWidget, InfoIcon.class, infoText);
    }
}
