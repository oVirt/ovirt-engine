package org.ovirt.engine.ui.common.idhandler;

import com.google.gwt.cell.client.Cell;

// TODO I think all of our custom cells should implement this. No reason
// not to have IDs on every table cell.

/**
 * A cell that sets an ID when it renders.
 *
 * @param <C>
 *            Cell data type.
 */
public interface CellWithElementId<C> extends Cell<C> {

    /**
     * Set the element id prefix.
     * @param elementIdPrefix
     */
    public void setElementIdPrefix(String elementIdPrefix);

    /**
     * Set the column id.
     * @param columnId
     */
    public void setColumnId(String columnId);

    public String getElementIdPrefix();

    public String getColumnId();
}
