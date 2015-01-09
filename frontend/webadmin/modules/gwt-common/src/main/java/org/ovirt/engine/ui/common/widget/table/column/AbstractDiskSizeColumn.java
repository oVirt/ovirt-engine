package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

public abstract class AbstractDiskSizeColumn<T> extends AbstractSizeColumn<T> {

    public AbstractDiskSizeColumn() {
        this(SizeConverter.SizeUnit.BYTES);
    }

    public AbstractDiskSizeColumn(SizeConverter.SizeUnit diskSizeUnit) {
        super(new DiskSizeRenderer<Long>(diskSizeUnit));
    }

    public AbstractDiskSizeColumn(SizeConverter.SizeUnit diskSizeUnit, DiskSizeRenderer.Format format) {
        super(new DiskSizeRenderer<Long>(diskSizeUnit, format));
    }

}
