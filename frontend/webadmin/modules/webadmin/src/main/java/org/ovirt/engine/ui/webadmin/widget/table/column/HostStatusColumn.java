package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.PairQueryable;

import com.google.gwt.user.cellview.client.Column;

public class HostStatusColumn<S> extends Column<S, VDS> {

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

}
