package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDS;

import com.google.gwt.user.cellview.client.Column;

public class HostStatusColumn extends Column<VDS, VDS> {

    public HostStatusColumn() {
        super(new HostStatusCell());
    }

    @Override
    public VDS getValue(VDS vds) {
        return vds;
    }

}
