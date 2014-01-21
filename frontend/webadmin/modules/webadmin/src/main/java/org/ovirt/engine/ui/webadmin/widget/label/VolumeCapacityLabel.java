package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.webadmin.widget.renderer.VolumeCapacityRenderer;

public class VolumeCapacityLabel<T extends Number> extends TextBoxLabelBase<T> {

    public VolumeCapacityLabel(CommonApplicationConstants constants) {
        super(new VolumeCapacityRenderer<Number>(constants));
    }

}
