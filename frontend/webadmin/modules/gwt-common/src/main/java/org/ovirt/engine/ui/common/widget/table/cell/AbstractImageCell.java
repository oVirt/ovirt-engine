package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.widget.table.HasStyleClass;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class AbstractImageCell<T> extends AbstractCell<T> implements HasStyleClass {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div id=\"{0}\" style=\"{1}\" class=\"{2}\">{3}</div>")
        SafeHtml imageContainerWithStyleClass(String id, SafeStyles style, String styleClass, SafeHtml imageHtml);
    }

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$
    private String styleClass = ""; //$NON-NLS-1$

    private CellTemplate template = GWT.create(CellTemplate.class);

    protected abstract SafeHtml getRenderedImage(T value);

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            final SafeHtml renderedImage = getRenderedImage(value);
            sb.append(template.imageContainerWithStyleClass(
                    id,
                    SafeStylesUtils.fromTrustedString(style),
                    styleClass,
                    renderedImage));
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
