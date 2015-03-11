package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

public class DiskContainersCell extends TextCell {

    private String title = ""; //$NON-NLS-1$

    public DiskContainersCell() {
        super(TextCell.UNLIMITED_LENGTH);
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent,
            String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Ignore events other than 'mouseover'
        if (!BrowserEvents.MOUSEOVER.equals(event.getType())) {
            return;
        }

        parent.setTitle(title);
    }

    @Override
    public void setTitle(String title) {
        this.title = title != null ? title : ""; //$NON-NLS-1$
    }

}
