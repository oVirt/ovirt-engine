package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;

import com.google.gwt.resources.client.ImageResource;

public class NicActivateStatusColumn<T> extends ImageResourceColumn<T> {
    @Override
    public ImageResource getValue(T object) {
        VmNetworkInterface vnic = null;
        if (object instanceof VmNetworkInterface){
            vnic = (VmNetworkInterface) object;
        }else if (object instanceof PairQueryable && ((PairQueryable) object).getFirst() instanceof VmNetworkInterface){
            vnic = ((PairQueryable<VmNetworkInterface, VM>) object).getFirst();
        }

        if (vnic != null){
            return vnic.isActive() ?
                    getCommonResources().upImage() : getCommonResources().downImage();
        }

        return null;
    }
}
