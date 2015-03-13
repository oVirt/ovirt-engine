package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSafeHtmlWithSafeHtmlTooltipColumn<T> extends AbstractSortableColumn<T, SafeHtml>{

    public AbstractSafeHtmlWithSafeHtmlTooltipColumn() {
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
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb, String id) {
                if (value != null) {
                    sb.append(value);
                  }

            }

        });

        tooltipPanel.setWidget(tooltip);
        tooltipPanel.getElement().getStyle().setZIndex(1);
    }

    private final HTML tooltip = new HTML();
    private final DecoratedPopupPanel tooltipPanel = new DecoratedPopupPanel();


    @Override
    public abstract SafeHtml getValue(T object);

    public abstract SafeHtml getTooltip(T object);

    @Override
    public void onBrowserEvent(Context context, final Element elem, T object, NativeEvent event) {
        super.onBrowserEvent(context, elem, object, event);

        if (BrowserEvents.MOUSEOVER.equals(event.getType())) {
            Widget widget = new Widget(){
                @Override
                public com.google.gwt.user.client.Element getElement() {
                    return (com.google.gwt.user.client.Element) elem;
                };
            };

            SafeHtml tooltipHtml= getTooltip(object);
            if(tooltipHtml != null && !"".equals(tooltipHtml.asString())){ //$NON-NLS-1$
                tooltip.setHTML(tooltipHtml);
                tooltipPanel.showRelativeTo(widget);
            }
        }
        else if (BrowserEvents.MOUSEOUT.equals(event.getType())) {
            tooltipPanel.hide(true);
        }

    }

}
