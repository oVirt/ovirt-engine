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
 * ImageResourceCell that supports setting a style and displaying a tooltip in a DecoratedPopupPanel.
 */
public class ImageResourceCell extends AbstractTitlePanelCell<ImageResource> implements HasStyleClass {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{0}\" class=\"{1}\">{2}</div>")
        SafeHtml imageContainerWithStyleClass(String style, String styleClass, SafeHtml imageHtml);
    }

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$
    private String styleClass = ""; //$NON-NLS-1$

    private static CellTemplate template;

    public ImageResourceCell() {
        super();

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
    }

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb, String id) {
        // this class is removed in a follow-up patch. that is why i'm ignoring the id here.
        if (value != null) {
            sb.append(template.imageContainerWithStyleClass(
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
