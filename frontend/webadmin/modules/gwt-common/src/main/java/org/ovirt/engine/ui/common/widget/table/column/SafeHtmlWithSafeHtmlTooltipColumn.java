package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public abstract class SafeHtmlWithSafeHtmlTooltipColumn<T> extends Column<T, SafeHtml>{

    public SafeHtmlWithSafeHtmlTooltipColumn() {
        super(new AbstractCell<SafeHtml>("mouseover", "mouseout"){ //$NON-NLS-1$ //$NON-NLS-2$

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

        tooltipPanel.setWidget(tooltip);
    }

    private final HTML tooltip = new HTML();
    private final DecoratedPopupPanel tooltipPanel = new DecoratedPopupPanel();


    @Override
    public abstract SafeHtml getValue(T networkView);

    public abstract SafeHtml getTooltip(T networkView);

    @Override
    public void onBrowserEvent(Context context, final Element elem, T object, NativeEvent event) {
        super.onBrowserEvent(context, elem, object, event);

        if (event.getType().equals("mouseover")) { //$NON-NLS-1$
            Widget widget = new Widget(){
                @Override
                public com.google.gwt.user.client.Element getElement() {
                    return (com.google.gwt.user.client.Element) elem;
                };
            };

            SafeHtml tooltipHtml= getTooltip(object);
            if(!"".equals(tooltipHtml.asString())){ //$NON-NLS-1$
                tooltip.setHTML(getTooltip(object));
                tooltipPanel.showRelativeTo(widget);
            }
        }
        else if (event.getType().equals("mouseout")) { //$NON-NLS-1$
            tooltipPanel.hide(true);
        }

    }

}
