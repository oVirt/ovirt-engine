package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.idhandler.CellWithElementId;
import org.ovirt.engine.ui.common.widget.table.cell.EventHandlingCell;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.user.client.DOM;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * Base class for all Cells that would otherwise extend GWT CheckboxCell.
 * Supports rendering Element ids via the oVirt Element-ID framework.
 */
public class EnabledDisabledCheckboxCell extends CheckboxCell implements EventHandlingCell, CellWithElementId<Boolean> {

    private String elementIdPrefix = DOM.createUniqueId(); // default
    private String columnId;

    public EnabledDisabledCheckboxCell() {
        super(true, false);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return AbstractCheckboxColumn.handlesEvent(event);
    }

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
