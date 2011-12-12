package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.webadmin.widget.renderer.DiskSizeRenderer.DiskSizeUnit;

import com.google.gwt.user.client.ui.ValueLabel;

public class DiskSizeLabel<T extends Number> extends ValueLabel<T> {

    public DiskSizeLabel() {
        super(new DiskSizeRenderer<T>(DiskSizeUnit.GIGABYTE));
    }

}
