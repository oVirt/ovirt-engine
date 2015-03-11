package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A Cell with a tooltip. All Cells should implement this by extending ovirt's AbstractCell.
 *
 * @param <C> cell render type
 */
public interface TooltipCell<C> extends Cell<C>, CellWithElementId<C> {

    /**
     * Called by AbstractColumn when an event occurs in a Cell. The only difference from GWT's native
     * Column is that here we ask the column to provide us a tooltip value in addition to the cell's
     * C value.
     */
    public void onBrowserEvent(Context context, final Element parent, C value, final SafeHtml tooltipContent,
            final NativeEvent event, ValueUpdater<C> valueUpdater);

    /**
     * Called by AbstractColumn to render a cell. Sends the cell id so your template can include it
     * in the render.
     */
    public abstract void render(Context context, C value, SafeHtmlBuilder sb, String id);
}
