package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipWidth;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class TooltippedIcon extends FocusPanel {

    private Image image;
    private final WidgetTooltip tooltip;
    private boolean useItalic = true;

    public TooltippedIcon(SafeHtml text, final ImageResource mouseOutImage, final ImageResource mouseInImage) {
        super();

        image = new Image(mouseOutImage);

        tooltip = new WidgetTooltip(image);
        setText(text);
        setWidget(tooltip);

        addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                image.setUrl(mouseOutImage.getSafeUri());
            }
        });

        addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                image.setUrl(mouseInImage.getSafeUri());
            }
        });
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

}
