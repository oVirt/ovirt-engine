package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.widget.ImageWithDecorator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A Cell used to render an {@link ImageWithDecorator}.
 */
public class DecoratedImageResourceCell extends AbstractCell<ImageWithDecorator> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div data-status=\"{5}\" id=\"{4}\" style=\"position: relative; left: 0; top: 0;\"><span style=\"position: relative; left: 0px; top: 0px;\">{0}</span><span style=\"position: absolute; left: {2}px; top: {3}px;\">{1}</span></div>")
        SafeHtml doubleImageContainer(SafeHtml imageHtml, SafeHtml decoratorHtml, int left, int top, String id, String status);
    }

    private static final CellTemplate template = GWT.create(CellTemplate.class);

    @Override
    public void render(Context context, ImageWithDecorator value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            SafeHtml mainImageHtml = SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
            SafeHtml decorateImageHtml = SafeHtmlUtils.fromTrustedString(""); //$NON-NLS-1$
            if (value.getImage() != null) {
                mainImageHtml =
                        SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value.getImage()).getHTML());
            }
            if (value.getDecorator() != null) {
                decorateImageHtml =
                        SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value.getDecorator()).getHTML());
            }
            sb.append(template.doubleImageContainer(mainImageHtml,
                    decorateImageHtml,
                    value.getDecoratorPositionLeft(),
                    value.getDecoratorPositionTop(),
                    id,
                    value.getStatus()));
        }
    }
}
