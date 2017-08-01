package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * This class may be used to display links in cell widgets. It allows the full functionality of
 * a {@link TextCell} while wrapping the display value in a link tag. Click events are caught
 * and passed to a ValueUpdater. Supports tooltips.
 */
@SuppressWarnings("deprecation")
public class LinkCell extends TextCell {

    protected interface CellTemplate extends TextCell.CellTemplate {
        @Template("<a style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap;' class='{0}' id='{1}' href='javascript:;'>{2}</a>")
        SafeHtml containerAndLink(String cellStyle, String id, SafeHtml text);
    }

    protected CellTemplate template = GWT.create(CellTemplate.class);

    public LinkCell() {
        super(UNLIMITED_LENGTH, true);
    }

    /**
     * Render as if it is an overflow truncation {@link TextCell} but with a text value as
     * a link instead of normal text.
     */
    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            SafeHtml safeValue = SafeHtmlUtils.fromString(value);

            sb.append(template.containerAndLink(
                    getStyleClass(),
                    getRenderElementId(context),
                    safeValue));
        }
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
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
