package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.TooltipPanel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

public abstract class SafeHtmlWithSafeHtmlTooltipColumn<T> extends Column<T, SafeHtml>{

    private final TooltipPanel tooltipPanel = new TooltipPanel();

    public SafeHtmlWithSafeHtmlTooltipColumn() {
        super(new AbstractCell<SafeHtml>(BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT) {

            @Override
            public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context,
                    Element parent,
                    SafeHtml value,
                    NativeEvent event,
                    ValueUpdater<SafeHtml> valueUpdater) {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
            }

            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(value);
                  }

            }

        });
    }

    @Override
    public abstract SafeHtml getValue(T object);

    public abstract SafeHtml getTooltip(T object);

    @Override
    public void onBrowserEvent(Context context, final Element elem, T object, NativeEvent event) {
        super.onBrowserEvent(context, elem, object, event);

        if (BrowserEvents.MOUSEOVER.equals(event.getType())) {
            SafeHtml tooltipHtml = getTooltip(object);
            if (tooltipHtml != null && !tooltipHtml.asString().isEmpty()) {
                tooltipPanel.setText(getTooltip(object));
            }
        }
        tooltipPanel.handleNativeBrowserEvent(elem, event);
    }

}
