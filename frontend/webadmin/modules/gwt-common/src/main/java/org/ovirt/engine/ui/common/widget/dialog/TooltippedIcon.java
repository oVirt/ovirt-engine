package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class TooltippedIcon extends FocusPanel {

    private Image image;
    private final WidgetTooltip tooltip;
    private boolean useItalic = true;

    public TooltippedIcon(SafeHtml text, final ImageResource mouseOutImage, final ImageResource mouseInImage) {
        super();

        image = new Image(mouseOutImage);
        tooltip = createTooltip(image);
        setText(text);

        addMouseOutHandler(event -> image.setUrl(mouseOutImage.getSafeUri()));

        addMouseOverHandler(event -> image.setUrl(mouseInImage.getSafeUri()));
    }

    public TooltippedIcon(SafeHtml text, Widget icon) {
        super();
        tooltip = createTooltip(icon);
        setText(text);
    }

    protected Widget getTooltipWidget() {
        return tooltip.getWidget();
    }

    private WidgetTooltip createTooltip(Widget widget) {
        WidgetTooltip tooltip = new WidgetTooltip(widget);
        setWidget(tooltip);
        return tooltip;
    }

    public void setText(SafeHtml text) {
        // "text" can actually contain HTML markup.
        tooltip.setHtml(useItalic ? wrapItalic(text) : text);
    }

    public void setTooltipPlacement(Placement placement) {
        tooltip.setPlacement(placement);
    }

    /**
     * Return the tooltip text, wrapped in italic if there wasn't already italic detected.
     */
    private SafeHtml wrapItalic(SafeHtml text) {
        if (text.asString().startsWith("<i>")) { //$NON-NLS-1$
            // already wrapped in italic
            return text;
        }
        return new SafeHtmlBuilder()
                .appendHtmlConstant("<i>") //$NON-NLS-1$
                .append(text)
                .appendHtmlConstant("</i>") //$NON-NLS-1$
                .toSafeHtml();
    }

    public void setTooltipMaxWidth(TooltipWidth width) {
        tooltip.setMaxWidth(width);
    }

    public void setTooltip(String tooltipText) {
        tooltip.setText(tooltipText);
    }

}
