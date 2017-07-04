package org.ovirt.engine.ui.common.idhandler;

import com.google.gwt.cell.client.Cell;

/**
 * A cell that sets an ID when it renders.
 * TODO All of our custom cells should implement this.
 *
 * @param <C> Cell data type.
 */
public interface CellWithElementId<C> extends Cell<C> {

    /**
     * Set the element id prefix.
     */
    void setElementIdPrefix(String elementIdPrefix);

    /**
     * Set the column id.
     */
    void setColumnId(String columnId);

    /**
     * Get the element id prefix.
     */
    String getElementIdPrefix();

    /**
     * Get the column id.
     */
    String getColumnId();

}
