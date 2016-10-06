package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * A {@link TextCell} whose text may be edited. Supports wrapping with a css style. Supports tooltips.
 */
public class EditTextCell extends TextCell implements EventHandlingCell {

    private com.google.gwt.cell.client.EditTextCell delegate = new com.google.gwt.cell.client.EditTextCell();

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        set.add(BrowserEvents.KEYUP);
        set.add(BrowserEvents.KEYDOWN);
        set.add(BrowserEvents.BLUR);
        return set;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        // TODO use ID? perhaps wrap in a div
        delegate.render(context, value, sb);
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            String value,
            NativeEvent event,
            ValueUpdater<String> valueUpdater) {
        delegate.onBrowserEvent(context, parent, value, event, valueUpdater);
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return true;
    }

}
