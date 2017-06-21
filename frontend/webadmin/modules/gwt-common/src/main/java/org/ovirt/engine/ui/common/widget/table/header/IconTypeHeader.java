package org.ovirt.engine.ui.common.widget.table.header;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public class IconTypeHeader extends SafeHtmlHeader {

    interface IconTypeTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"fa {0} inline-icon\" style=\"{1};width:100%;\"/>")
        SafeHtml icon(String className, SafeStyles styles);
    }

    private static final String DEFAULT_COLOR = "#FFF"; //$NON-NLS-1$

    private static final IconTypeTemplate TEMPLATE = GWT.create(IconTypeTemplate.class);

    public IconTypeHeader(IconType icon, SafeHtml tooltip) {
        this(icon, DEFAULT_COLOR, tooltip);
    }

    public IconTypeHeader(IconType icon, String color, SafeHtml tooltip) {
        super(getRenderedImage(icon, color), tooltip);
    }

    protected static SafeHtml getRenderedImage(IconType value, String color) {
        SafeStylesBuilder builder = new SafeStylesBuilder();
        builder.trustedColor(color);
        builder.textAlign(TextAlign.CENTER);
        builder.verticalAlign(VerticalAlign.MIDDLE);
        SafeStyles styles = builder.toSafeStyles();
        return TEMPLATE.icon(value.getCssName(), styles);
    }
}
