package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.DOM;

/**
 * A {@link Cell} used to render text, providing a tooltip in case the text does not fit within the parent element.
 * 
 * @see TextCell
 */
public class TextCellWithTooltip extends AbstractSafeHtmlCell<String> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div id=\"{0}\">{1}</div>")
        SafeHtml textContainer(String id, String text);
    }

    public static final int UNLIMITED_LENGTH = -1;
    private static final String TOO_LONG_TEXT_POSTFIX = "...";

    // DOM element ID settings for the text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    // Text longer than this value will be shortened, providing a tooltip with original text
    private final int maxTextLength;

    private static CellTemplate template;

    public TextCellWithTooltip(int maxTextLength) {
        super(SimpleSafeHtmlRenderer.getInstance(), "mouseover");
        this.maxTextLength = maxTextLength;

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        String rawData = value != null ? value.asString() : "";

        sb.append(template.textContainer(
                getContainerElementId(context),
                getRenderedValue(rawData)));
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent,
            String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Ignore events other than 'mouseover'
        if (!"mouseover".equals(event.getType())) {
            return;
        }

        // Enforce tooltip when the presented text doesn't match original value
        boolean forceTooltip = (value != null && !value.equals(getRenderedValue(value)));

        // If the parent element content overflows its area, provide tooltip to the element
        if (forceTooltip || contentOverflows(parent)) {
            parent.setTitle(value);
        } else {
            parent.setTitle("");
        }
    }

    /**
     * Returns the DOM element ID for the text container element.
     */
    String getContainerElementId(Cell.Context context) {
        StringBuilder sb = new StringBuilder(elementIdPrefix);
        sb.append("_");
        sb.append(columnId != null ? columnId : "col" + String.valueOf(context.getColumn()));
        sb.append("_row");
        sb.append(String.valueOf(context.getIndex()));
        return sb.toString();
    }

    /**
     * Returns the text value to be rendered by this cell.
     */
    String getRenderedValue(final String text) {
        String result = text;

        // Check if the text needs to be shortened
        if (maxTextLength > 0 && text.length() > maxTextLength) {
            result = result.substring(0, Math.max(maxTextLength - TOO_LONG_TEXT_POSTFIX.length(), 0));
            result = result + TOO_LONG_TEXT_POSTFIX;
        }

        return result;
    }

    /**
     * Returns {@code true} when the content of the given element overflows the element's content area, {@code false}
     * otherwise.
     */
    boolean contentOverflows(Element elm) {
        String overflowValue = elm.getStyle().getOverflow();

        // Temporarily allow element content to overflow through scroll bars
        elm.getStyle().setProperty("overflow", "auto");
        boolean overflowX = elm.getScrollWidth() > elm.getClientWidth();
        boolean overflowY = elm.getScrollHeight() > elm.getClientHeight();

        // Revert to the original overflow value
        elm.getStyle().setProperty("overflow", overflowValue);

        return overflowX || overflowY;
    }

}
