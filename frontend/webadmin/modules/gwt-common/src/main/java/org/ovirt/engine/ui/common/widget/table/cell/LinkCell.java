package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * This class may be used to display links in cell widgets. It allows the full functionality of a
 * {@link TextCell}, and wraps the HTML result of its rendering within link tags. Click events are caught and
 * passed to a ValueUpdater. Supports tooltips.
 */
@SuppressWarnings("deprecation")
public class LinkCell extends TextCell {

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    private static final String CONTENT_ID_SUFFIX = "_content"; //$NON-NLS-1$

    public interface CellTemplate extends SafeHtmlTemplates {
        @Template("<a id='{0}' href='javascript:;' style='display: inline-block'>")
        SafeHtml link(String id);
    }

    private CellTemplate template = GWT.create(CellTemplate.class);

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            sb.append(template.link(id));
            super.render(context, value, sb, id + CONTENT_ID_SUFFIX);
            sb.appendHtmlConstant("</a>"); //$NON-NLS-1$
        }
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            String value,
            SafeHtml tooltipContent,
            NativeEvent event,
            ValueUpdater<String> valueUpdater) {

        super.onBrowserEvent(context, parent, value, tooltipContent, event, valueUpdater);
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
