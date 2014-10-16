package org.ovirt.engine.ui.common.widget.table.column;

import java.util.List;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.user.client.DOM;

/**
 * A composite cell that sets an ID when it renders.
 *
 * @param <C>
 *            Cell data type.
 */
public class CompositeCellWithElementId<C> extends CompositeCell<C> implements CellWithElementId<C> {

    public CompositeCellWithElementId(List<HasCell<C, ?>> hasCells) {
        super(hasCells);
    }

    // DOM element ID settings for text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getElementIdPrefix() {
        return elementIdPrefix;
    }

    public String getColumnId() {
        return columnId;
    }

}
