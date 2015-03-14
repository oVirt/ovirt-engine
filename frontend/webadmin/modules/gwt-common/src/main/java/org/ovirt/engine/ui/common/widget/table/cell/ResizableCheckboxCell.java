package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * ResizableCheckboxCell. Supports tooltips.
 *
 */
public class ResizableCheckboxCell extends SafeHtmlCell {
    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        set.add(BrowserEvents.MOUSEDOWN);
        set.add(BrowserEvents.MOUSEMOVE);
        set.add(BrowserEvents.CHANGE);
        set.add(BrowserEvents.KEYDOWN);
        return set;
    }
}
