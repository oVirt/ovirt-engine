package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.widget.table.HasStyleClass;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Cell that renders an ImageResource. Supports setting a style / class. Supports tooltips.
 * TODO: this will replace ImageResourceCell in a follow-up patch.
 */
public class ImageResourceCell2 extends AbstractTooltipCell<ImageResource> implements HasStyleClass {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div id=\"{0}\" style=\"{1}\" class=\"{2}\">{3}</div>")
        SafeHtml imageContainerWithStyleClass(String id, String style, String styleClass, SafeHtml imageHtml);
    }

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$
    private String styleClass = ""; //$NON-NLS-1$

    private CellTemplate template = GWT.create(CellTemplate.class);

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            sb.append(template.imageContainerWithStyleClass(
                    id,
                    style,
                    styleClass,
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value).getHTML())));
        }
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass == null ? "" : styleClass; //$NON-NLS-1$
    }

}
