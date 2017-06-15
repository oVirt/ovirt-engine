package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class StorageSizeLabel<T extends Number> extends ValueLabel<T> {

    public StorageSizeLabel() {
        super(new DiskSizeRenderer<T>(SizeConverter.SizeUnit.GiB));
    }

}
