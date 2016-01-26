package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import com.google.gwt.text.shared.AbstractRenderer;

public class DiskSizeRenderer<T extends Number> extends AbstractRenderer<T> {

    public enum Format {
        GIGABYTE,
        HUMAN_READABLE
    }

    private final SizeConverter.SizeUnit unit;
    public final Format format;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public DiskSizeRenderer(SizeConverter.SizeUnit unit) {
        this(unit, Format.GIGABYTE);
    }

    public DiskSizeRenderer(SizeConverter.SizeUnit unit, Format format) {
        if (unit == null) {
            throw new IllegalArgumentException("The unit can not be null!"); //$NON-NLS-1$
        }

        this.unit = unit;
        this.format = format;
    }

    protected boolean isUnavailable(T size) {
        return size == null;
    }

    @Override
    public String render(T size) {
        if (isUnavailable(size)) {
            return constants.unAvailablePropertyLabel();
        }

        switch (format) {
            case GIGABYTE:
                return renderGigabyteSize(size.longValue());

            case HUMAN_READABLE:
                return renderHumanReadableSize(size.longValue());

            default:
                throw new RuntimeException("Format '" + format + "' is not supported!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private String renderGigabyteSize(long size) {
        long sizeInGB = (unit == SizeUnit.GiB) ?
                size : SizeConverter.convert(size, unit, SizeUnit.GiB).longValue();
        return sizeInGB >= 1 ? sizeInGB + " GB" : "< 1 GB"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String renderHumanReadableSize(long size) {
        long sizeInBytes = SizeConverter.convert(size, unit, SizeUnit.BYTES).longValue();
        if(sizeInBytes > SizeConverter.BYTES_IN_GB) {
            return SizeConverter.convert(sizeInBytes, SizeConverter.SizeUnit.BYTES, SizeUnit.GiB).longValue() + " GB"; //$NON-NLS-1$
        } else if(sizeInBytes > SizeConverter.BYTES_IN_MB) {
            return SizeConverter.convert(sizeInBytes, SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.MiB).longValue() + " MB"; //$NON-NLS-1$
        } else if(sizeInBytes > SizeConverter.BYTES_IN_KB) {
            return SizeConverter.convert(sizeInBytes, SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.KiB).longValue() + " KB"; //$NON-NLS-1$
        } else {
            return sizeInBytes + " Bytes"; //$NON-NLS-1$
        }
    }
}
