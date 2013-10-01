package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.SizeConverter;

public abstract class StorageSizeColumn<T> extends RenderedTextColumn<T, Long> {

    public StorageSizeColumn() {
        super(new DiskSizeRenderer<Long>(SizeConverter.SizeUnit.GB) {
            @Override
            protected boolean isUnavailable(Long size) {
                return size == null || size.longValue() == 0;
            }
        });
    }

}
