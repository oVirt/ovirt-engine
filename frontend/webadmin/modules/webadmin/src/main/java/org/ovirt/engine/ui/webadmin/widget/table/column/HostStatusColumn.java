package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IdentifiableComparator;

public class HostStatusColumn<S> extends SortableColumn<S, VDS> {

    public HostStatusColumn() {
        super(new HostStatusCell());
    }

    @Override
    public VDS getValue(S object) {
        if (object instanceof VDS){
            return (VDS) object;
        }
        if (object instanceof PairQueryable){
            if (((PairQueryable) object).getSecond() instanceof VDS){
                return (VDS) ((PairQueryable) object).getSecond();
            }
        }
        return null;
    }

    public void makeSortable() {
        makeSortable(new Comparator<S>() {

            IdentifiableComparator<VDSStatus> valueComparator = new Linq.IdentifiableComparator<VDSStatus>();

            @Override
            public int compare(S o1, S o2) {
                VDSStatus status1 = (getValue(o1) == null) ? null : getValue(o1).getStatus();
                VDSStatus status2 = (getValue(o2) == null) ? null : getValue(o2).getStatus();
                return valueComparator.compare(status1, status2);
            }
        });
    }

}
