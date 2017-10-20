package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.webadmin.widget.table.cell.HostStatusCell;

public class HostStatusColumn<S> extends AbstractColumn<S, VDS> {

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
        makeSortable(Comparator.comparing(
                o -> getValue(o) == null ? null : getValue(o).getStatus(), Linq.IdentifiableComparator));
    }

}
