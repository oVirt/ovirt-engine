package org.ovirt.engine.ui.common.widget.table.cell;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public class IconTypeCell extends AbstractImageCell<IconType> {

    interface IconTypeTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"fa {0} inline-icon\" style=\"{1}\"/>")
        SafeHtml icon(String className, SafeStyles styles);
    }

    private static final IconTypeTemplate TEMPLATE = GWT.create(IconTypeTemplate.class);

    private String color = "#FFF"; //$NON-NLS-1$

    @Override
    protected SafeHtml getRenderedImage(IconType value) {
        return getRenderedImage(value, color); // $NON-NLS-1$
    }

    protected SafeHtml getRenderedImage(IconType value, String color) {
        SafeStyles styles = new SafeStylesBuilder().trustedColor(color).toSafeStyles();
        return TEMPLATE.icon(value.getCssName(), styles);
    }

    public void setColor(SafeHtml color) {
        this.color = color.asString();
    }

}
