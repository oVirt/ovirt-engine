package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;

import com.google.gwt.resources.client.ImageResource;

public class NicActivateStatusColumn extends ImageResourceColumn<VmNetworkInterface> {
    @Override
    public ImageResource getValue(VmNetworkInterface object) {
        return object.isActive() ?
                getCommonResources().upImage() : getCommonResources().downImage();
    }
}
