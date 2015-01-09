package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IdentifiableComparator;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VmStatusCell;

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

    public void makeSortable() {
        makeSortable(new Comparator<T>() {

            IdentifiableComparator<VMStatus> valueComparator = new Linq.IdentifiableComparator<VMStatus>();

            @Override
            public int compare(T o1, T o2) {
                VMStatus status1 = (getValue(o1) == null) ? null : getValue(o1).getStatus();
                VMStatus status2 = (getValue(o2) == null) ? null : getValue(o2).getStatus();
                return valueComparator.compare(status1, status2);
            }
        });
    }

}
