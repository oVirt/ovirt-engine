package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.webadmin.widget.renderer.VolumeCapacityRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class VolumeCapacityLabel<T extends Number> extends ValueLabel<T> {

    public VolumeCapacityLabel(CommonApplicationConstants constants) {
        super(new VolumeCapacityRenderer<>(constants));
    }

}
