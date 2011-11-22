package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.user.cellview.client.Column;

public abstract class LunSelectionColumn extends Column<LunModel, LunModel> {

    public LunSelectionColumn() {
        super(new LunSelectionCell());
    }
}
