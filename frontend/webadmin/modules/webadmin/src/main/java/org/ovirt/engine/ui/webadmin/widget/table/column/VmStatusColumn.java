package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;

import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;

/**
 * Image column that corresponds to XAML {@code VmStatusTemplate}.
 */
public class VmStatusColumn<T> extends SortableColumn<T, VM> {

    public VmStatusColumn() {
        super(new VmStatusCell());
    }

    @Override
    public VM getValue(T object) {
        if (object instanceof VM){
            return (VM)object;
        }if (object instanceof PairQueryable && ((PairQueryable) object).getSecond() instanceof VM){
            return ((PairQueryable<VmNetworkInterface, VM>) object).getSecond();
        }
        return null;
    }

}
