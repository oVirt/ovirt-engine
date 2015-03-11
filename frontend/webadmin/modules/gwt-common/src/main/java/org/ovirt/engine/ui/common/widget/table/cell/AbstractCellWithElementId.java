package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.user.client.DOM;

/**
 * A cell that sets an ID when it renders. Convenience implementation of CellWithElementId.
 *
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractCellWithElementId<C> extends AbstractCell<C> implements CellWithElementId<C> {

    public AbstractCellWithElementId(String... consumedEvents) {
        super(consumedEvents);
    }

    private String elementIdPrefix = DOM.createUniqueId(); // default
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
