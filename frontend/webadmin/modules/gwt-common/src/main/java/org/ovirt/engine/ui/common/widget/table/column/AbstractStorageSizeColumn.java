package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

public abstract class AbstractStorageSizeColumn<T> extends AbstractSizeColumn<T> {

    public AbstractStorageSizeColumn() {
        super(new DiskSizeRenderer<Long>(SizeConverter.SizeUnit.GiB));
    }
}
