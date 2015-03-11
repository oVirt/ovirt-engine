package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.EventHandlingCell;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.view.client.CellPreviewEvent;

/**
 * A Cell that renders a checkbox.
 */
public class EnabledDisabledCheckboxCell extends CheckboxCell implements EventHandlingCell {

    public EnabledDisabledCheckboxCell() {
        super(true, false);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return AbstractCheckboxColumn.handlesEvent(event);
    }

}
