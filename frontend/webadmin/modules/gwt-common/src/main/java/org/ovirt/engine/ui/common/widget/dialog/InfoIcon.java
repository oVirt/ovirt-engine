package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;

public class InfoIcon extends TooltippedIcon {

    public static interface InfoIconStyle extends CssResource {
        String infoIconColor();
    }

    public static interface InfoIconResources extends ClientBundle {
        @ClientBundle.Source("org/ovirt/engine/ui/common/css/InfoIcon.css")
        InfoIconStyle iconStyle();
    }

    private static final InfoIconResources RESOURCES = GWT.create(InfoIconResources.class);

    private final InfoIconStyle style;

    @UiConstructor
    public InfoIcon(SafeHtml text) {
        super(text, new Anchor());
        // We know getTooltipWidget() is an Anchor
        ((Anchor) getTooltipWidget()).setIcon(IconType.INFO_CIRCLE);
        style = RESOURCES.iconStyle();
        style.ensureInjected();
        getTooltipWidget().addStyleName(style.infoIconColor());
    }
}
