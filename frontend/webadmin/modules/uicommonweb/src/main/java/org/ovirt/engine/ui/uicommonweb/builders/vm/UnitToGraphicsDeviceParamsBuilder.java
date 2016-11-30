package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.action.HasGraphicsDevices;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class UnitToGraphicsDeviceParamsBuilder extends BaseSyncBuilder<UnitVmModel, HasGraphicsDevices> {

    @Override
    protected void build(UnitVmModel source, HasGraphicsDevices destination) {
        if (source.getDisplayType().getSelectedItem() == null || source.getGraphicsType().getSelectedItem() == null) {
            return;
        }

        for (GraphicsType graphicsType : GraphicsType.values()) {
            destination.getGraphicsDevices().put(graphicsType, null); // reset

            // if not headless VM then set the selected graphic devices
            if (!source.getIsHeadlessModeEnabled().getEntity() &&
                    source.getGraphicsType().getSelectedItem().getBackingGraphicsTypes().contains(graphicsType)) {
                GraphicsDevice d = new GraphicsDevice(graphicsType.getCorrespondingDeviceType());
                destination.getGraphicsDevices().put(d.getGraphicsType(), d);
            }
        }
    }

}
