package org.ovirt.engine.ui.common.widget.table.column;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.TooltipPanel;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * ImageResourceCell that supports setting a style and displaying a tooltip in a
 * {@code TooltipPanel}.
 *
 */
public class StyledImageResourceCell extends TitlePanelCell<ImageResource> implements HasStyleClass {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{0}\" class=\"{1}\">{2}</div>")
        SafeHtml imageContainerWithStyleClass(String style, String styleClass, SafeHtml imageHtml);
    }

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$
    private String styleClass = ""; //$NON-NLS-1$

    private static CellTemplate template;

    private final TooltipPanel tooltipPanel = new TooltipPanel();

    private final Set<String> consumedEvents;

    public StyledImageResourceCell() {
        super();

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
        consumedEvents = new HashSet<String>();
        consumedEvents.add(BrowserEvents.MOUSEOVER);
        consumedEvents.add(BrowserEvents.MOUSEOUT);
    }

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
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

    /**
     * Set the text for the tool-tip that will show when this cell is hovered over.
     * @param title The text to show in the tool-tip.
     */
    @Override
    public void setTitle(String title) {
        tooltipPanel.setText(title);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, ImageResource value,
            NativeEvent event, ValueUpdater<ImageResource> valueUpdater) {
        tooltipPanel.handleNativeBrowserEvent(parent.getOffsetParent(), event);
    }
}
