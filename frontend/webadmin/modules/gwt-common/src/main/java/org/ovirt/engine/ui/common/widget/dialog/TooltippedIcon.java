package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipConfig.Width;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
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
        setTooltipText(text.asString());
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
        setTooltipText(text.asString());
        tooltip.reconfigure();
    }

    public void setTooltipPlacement(Placement placement) {
        tooltip.setPlacement(placement);
    }

    private void setTooltipText(String text) {
        tooltip.setText(useItalic ? wrapItalic(text) : text);
    }

    /**
     * Return the tooltip text, wrapped in italic if there wasn't already italic detected.
     */
    private String wrapItalic(String text) {
        if (text == null || text.isEmpty() || text.contains("<i>")) { //$NON-NLS-1$
            return text;
        }
        return text = "<i>" + text + "</i>"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setTooltipMaxWidth(Width width) {
        tooltip.setMaxWidth(width);
    }

    public boolean isUseItalic() {
        return useItalic;
    }

    public void setUseItalic(boolean useItalic) {
        this.useItalic = useItalic;
    }

}
