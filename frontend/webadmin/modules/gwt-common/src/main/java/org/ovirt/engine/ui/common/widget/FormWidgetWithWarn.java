package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

public class FormWidgetWithWarn extends WidgetWithWarn {

    public FormWidgetWithWarn(Widget contentWidget) {
        super(addStyle(contentWidget), SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    private static Widget addStyle(Widget contentWidget) {
        contentWidget.getElement().getStyle().setWidth(90, Unit.PCT);
        contentWidget.getElement().getStyle().setPaddingRight(5, Unit.PX);
        return contentWidget;
    }
}
