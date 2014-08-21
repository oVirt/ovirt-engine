package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;

public abstract class DiskSizeColumn<T> extends AbstractSizeColumn<T> {

    public DiskSizeColumn() {
        this(SizeConverter.SizeUnit.BYTES);
    }

    public DiskSizeColumn(SizeConverter.SizeUnit diskSizeUnit) {
        super(new DiskSizeRenderer<Long>(diskSizeUnit));
    }

    public DiskSizeColumn(SizeConverter.SizeUnit diskSizeUnit, DiskSizeRenderer.Format format) {
        super(new DiskSizeRenderer<Long>(diskSizeUnit, format));
    }

}
