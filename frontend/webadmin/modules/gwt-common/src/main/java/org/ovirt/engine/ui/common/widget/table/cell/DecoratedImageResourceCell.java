package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.widget.ImageWithDecorator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class DecoratedImageResourceCell extends AbstractTitlePanelCell<ImageWithDecorator> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"position: relative; left: 0; top: 0;\"><span style=\"position: relative; left: 0px; top: 0px;\">{0}</span><span style=\"position: absolute; left: {2}px; top: {3}px;\">{1}</span></div>")
        SafeHtml doubleImageContainer(SafeHtml imageHtml, SafeHtml decoratorHtml, int left, int top);
    }

    private static final CellTemplate template = GWT.create(CellTemplate.class);

    public DecoratedImageResourceCell() {
        super();
    }

    @Override
    public void render(Context context, ImageWithDecorator value, SafeHtmlBuilder sb) {
        if (value != null) {
            SafeHtml mainImageHtml = SafeHtmlUtils.fromTrustedString("");
            SafeHtml decorateImageHtml = SafeHtmlUtils.fromTrustedString("");
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
                    value.getDecoratorPositionTop()));
        }
    }
}
