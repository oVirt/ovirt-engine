package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;

import com.google.gwt.user.client.ui.ValueLabel;

public class DiskSizeLabel<T extends Number> extends ValueLabel<T> {

    public DiskSizeLabel() {
        super(new DiskSizeRenderer<T>(DiskSizeUnit.GIGABYTE));
    }

    public DiskSizeLabel(DiskSizeUnit diskSizeUnit) {
        super(new DiskSizeRenderer<T>(diskSizeUnit));
    }

}
