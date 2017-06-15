package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.LunSelectionCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.user.cellview.client.Column;

public abstract class AbstractLunSelectionColumn extends Column<LunModel, LunModel> {

    public AbstractLunSelectionColumn() {
        super(new LunSelectionCell());
    }

    public AbstractLunSelectionColumn(boolean multiSelection) {
        super(new LunSelectionCell(multiSelection));
    }
}
