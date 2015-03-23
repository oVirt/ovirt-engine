package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

public abstract class StorageSizeColumn<T> extends AbstractSizeColumn<T> {

    public StorageSizeColumn() {
        super(new DiskSizeRenderer<Long>(SizeConverter.SizeUnit.GB));
    }
}
