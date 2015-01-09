package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * This Cell type allows you to edit the text that would be presented as tooltip.
 * It overrides the usual  {@link TextCellWithTooltip} tooltip behavior
 */
public class TextCellWithEditableTooltip extends TextCellWithTooltip {

    private String title = ""; //$NON-NLS-1$

    public TextCellWithEditableTooltip() {
        super(TextCellWithTooltip.UNLIMITED_LENGTH);
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent,
                               String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Ignore events other than 'mouseover' or when title was left empty string
        if (!BrowserEvents.MOUSEOVER.equals(event.getType()) || "".equals(title)) { //$NON-NLS-1$
            return;
        }

        parent.setTitle(title);
    }

    @Override
    public void setTitle(String title) {
        this.title = title != null ? title : ""; //$NON-NLS-1$
    }

}
