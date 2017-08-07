package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiConstructor;

public class InfoIcon extends TooltippedIcon {

    @UiConstructor
    public InfoIcon() {
        this(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    public InfoIcon(SafeHtml text) {
        super(text, new Anchor());
        // We know getTooltipWidget() is an Anchor
        ((Anchor) getTooltipWidget()).setIcon(IconType.INFO_CIRCLE);
    }
}
