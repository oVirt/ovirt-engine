package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Interface for all Cells that would otherwise implement GWT Cell. Includes methods for tooltips
 * and Element ID framework.
 *
 * @param <C> cell render type
 */
public interface Cell<C> extends com.google.gwt.cell.client.Cell<C>, CellWithElementId<C> {

    /**
     * Called by AbstractColumn to render a cell.
     * Sends the cell id so your template can include it in the rendered HTML.
     */
    void render(Context context, C value, SafeHtmlBuilder sb, String id);

    /**
     * Allows setting tooltip fallback value, e.g. tooltip of the associated GWT Column.
     */
    void setTooltipFallback(SafeHtml tooltipFallback);

}
