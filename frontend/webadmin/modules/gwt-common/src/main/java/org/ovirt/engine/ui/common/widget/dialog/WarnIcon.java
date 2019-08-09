package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class WarnIcon extends TooltippedIcon {

    @UiConstructor
    public WarnIcon() {
        this(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    public WarnIcon(String text) {
        super(SafeHtmlUtils.fromString(text), createIcon());
    }

    public WarnIcon(SafeHtml text) {
        super(text, createIcon());
    }

    private static Widget createIcon() {
        FlowPanel panel = new FlowPanel(SpanElement.TAG);
        panel.addStyleName(PatternflyConstants.PFICON);
        panel.addStyleName(PatternflyConstants.PFICON_WARNING_TRIANGLE_O);
        panel.getElement().getStyle().setMarginLeft(5, Unit.PX);
        return panel;
    }
}
