package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

/**
 * A {@link Cell} used to render text, providing a tooltip in case the text does not fit within the parent element.
 * 
 * @see TextCell
 */
public class TextCellWithTooltip extends AbstractSafeHtmlCell<String> {

    public TextCellWithTooltip() {
        super(SimpleSafeHtmlRenderer.getInstance(), "mouseover");
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(value);
        }
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent,
            String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Ignore events other than 'mouseover'
        if (!"mouseover".equals(event.getType())) {
            return;
        }

        // If the parent element content overflows its area, provide tooltip to the element
        if (contentOverflows(parent)) {
            parent.setTitle(value.trim());
        } else {
            parent.setTitle("");
        }
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
