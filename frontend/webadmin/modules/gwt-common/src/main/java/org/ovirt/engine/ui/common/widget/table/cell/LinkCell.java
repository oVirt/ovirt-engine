package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.core.compat.StringHelper;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * This class may be used to display links in cell widgets. It allows the full functionality of a
 * {@link TextCellWithTooltip}, and wraps the HTML result of its rendering within link tags. Click events are caught and
 * passed to a ValueUpdater.
 */
@SuppressWarnings("deprecation")
public class LinkCell extends TextCellWithTooltip {

    public LinkCell(int maxTextLength) {
        super(maxTextLength, BrowserEvents.MOUSEOVER, BrowserEvents.CLICK);
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.appendHtmlConstant("<a href='javascript:;' style='display: inline-block'>"); //$NON-NLS-1$
            super.render(context, value, sb);
            sb.appendHtmlConstant("</a>"); //$NON-NLS-1$
        }
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            String value,
            NativeEvent event,
            ValueUpdater<String> valueUpdater) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (!BrowserEvents.CLICK.equals(event.getType())) {
            return;
        }
        if (valueUpdater != null && !StringHelper.isNullOrEmpty(value)) {
            if (parent.getFirstChild().isOrHasChild(Element.as(event.getEventTarget()))) {
                valueUpdater.update(value);
            }
        }
    }

}
