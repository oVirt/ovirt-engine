package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;

import com.google.gwt.user.cellview.client.Column;

/**
 * Image column that corresponds to XAML {@code VmStatusTemplate}.
 */
public class VmStatusColumn extends Column<VM, VM> {

    public VmStatusColumn() {
        super(new VmStatusCell());
    }

    @Override
    public VM getValue(VM vm) {
        return vm;
    }

}
