package org.ovirt.engine.ui.common.widget.table.column;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.ElementAwareDecoratedPopupPanel;

import com.google.gwt.cell.client.ImageResourceCell;
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
import com.google.gwt.user.client.ui.Label;

/**
 * ImageResourceCell that supports setting a style and displaying a tooltip in a
 * DecoratedPopupPanel.
 *
 */
public class StyledImageResourceCell extends ImageResourceCell {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{0}\">{1}</div>")
        SafeHtml imageContainer(String style, SafeHtml imageHtml);
    }

    private String style = "line-height: 100%; text-align: center; vertical-align: middle;"; //$NON-NLS-1$
    private String title = ""; //$NON-NLS-1$

    private static CellTemplate template;

    private ElementAwareDecoratedPopupPanel titlePanel = new ElementAwareDecoratedPopupPanel();

    private Set<String> consumedEvents;

    public StyledImageResourceCell() {
        super();

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }

        consumedEvents = new HashSet<String>();
        consumedEvents.add("mouseover"); //$NON-NLS-1$
        consumedEvents.add("mouseout"); //$NON-NLS-1$
    }

    @Override
    public Set<String> getConsumedEvents() {
        return consumedEvents;
    }

    @Override
    public void render(Context context, ImageResource value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(template.imageContainer(style,
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(value).getHTML())));
        }
    }

    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Set the text for the tooltip that will show when this cell is hovered over.
     * @param title
     */
    public void setTitle(String title) {
        this.title = title != null ? title : ""; //$NON-NLS-1$
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, ImageResource value,
            NativeEvent event, ValueUpdater<ImageResource> valueUpdater) {
        String eventType = event.getType();
        handleTitlePanel(eventType, parent);
    }

    private void handleTitlePanel(String eventType, Element parent) {
        if (title.isEmpty()) {
            // no need to show/hide title
            return;
        }

        titlePanel.setWidget(new Label(title));
        if (BrowserEvents.MOUSEOVER.equals(eventType)) {
            titlePanel.showRelativeTo(parent);
        }
        else if (BrowserEvents.MOUSEOUT.equals(eventType)) {
            titlePanel.hide();
        }
    }

}
