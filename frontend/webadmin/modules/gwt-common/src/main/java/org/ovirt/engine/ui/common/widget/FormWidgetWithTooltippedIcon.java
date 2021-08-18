package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.TooltippedIcon;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class FormWidgetWithTooltippedIcon extends WidgetWithTooltippedIcon {

    public FormWidgetWithTooltippedIcon(Widget contentWidget, Class<? extends TooltippedIcon> iconType) {
        super(addStyle(contentWidget), iconType, SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    private static Widget addStyle(Widget contentWidget) {
        contentWidget.getElement().getStyle().setWidth(80, Unit.PCT);
        contentWidget.getElement().getStyle().setPaddingRight(5, Unit.PX);
        return contentWidget;
    }
}
