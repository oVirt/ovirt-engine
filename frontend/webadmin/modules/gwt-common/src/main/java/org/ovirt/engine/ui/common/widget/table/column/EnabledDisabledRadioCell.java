package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.EventHandlingCell;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.view.client.CellPreviewEvent;

public class EnabledDisabledRadioCell extends RadioboxCell implements EventHandlingCell {

    public EnabledDisabledRadioCell() {
        super(true, false);
    }

    @Override
    public boolean handlesEvent(CellPreviewEvent<EntityModel> event) {
        return AbstractCheckboxColumn.handlesEvent(event);
    }

}

