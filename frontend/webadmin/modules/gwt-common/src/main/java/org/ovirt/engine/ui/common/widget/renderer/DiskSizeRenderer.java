package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
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
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

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
                return renderGibibyteSize(size.longValue());

            case HUMAN_READABLE:
                return renderHumanReadableSize(size.longValue());

            default:
                throw new RuntimeException("Format '" + format + "' is not supported!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private String renderGibibyteSize(long size) {
        long sizeInGiB = (unit == SizeUnit.GiB) ?
                size : SizeConverter.convert(size, unit, SizeUnit.GiB).longValue();
        return messages.gibibytes(sizeInGiB >= 1 ? String.valueOf(sizeInGiB) : "< 1"); //$NON-NLS-1$
    }

    private String renderHumanReadableSize(long size) {
        long sizeInBytes = SizeConverter.convert(size, unit, SizeUnit.BYTES).longValue();
        if(sizeInBytes >= SizeConverter.BYTES_IN_GB) {
            return messages.gibibytes(String.valueOf(
                    SizeConverter.convert(sizeInBytes, SizeUnit.BYTES, SizeUnit.GiB).longValue()));
        } else if(sizeInBytes >= SizeConverter.BYTES_IN_MB) {
            return messages.megabytes(String.valueOf(
                    SizeConverter.convert(sizeInBytes, SizeUnit.BYTES, SizeUnit.MiB).longValue()));
        } else if(sizeInBytes >= SizeConverter.BYTES_IN_KB) {
            return messages.kilobytes(String.valueOf(
                    SizeConverter.convert(sizeInBytes, SizeUnit.BYTES, SizeUnit.KiB).longValue()));
        } else {
            return messages.bytes(String.valueOf(sizeInBytes));
        }
    }
}
