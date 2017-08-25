package org.ovirt.engine.ui.common;

import com.google.gwt.user.cellview.client.DataGrid;

public interface MainTableResources extends DataGrid.Resources {

    interface Style extends DataGrid.Style {
    }

    @Override
    @Source({ DataGrid.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/TabDataGrid.css" })
    Style dataGridStyle();

}
