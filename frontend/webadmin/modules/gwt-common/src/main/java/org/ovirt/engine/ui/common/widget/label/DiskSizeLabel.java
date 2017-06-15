package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class DiskSizeLabel<T extends Number> extends ValueLabel<T> {

    public DiskSizeLabel() {
        super(new DiskSizeRenderer<T>(SizeConverter.SizeUnit.GiB));
    }

    public DiskSizeLabel(SizeConverter.SizeUnit diskSizeUnit) {
        super(new DiskSizeRenderer<T>(diskSizeUnit));
    }
}
