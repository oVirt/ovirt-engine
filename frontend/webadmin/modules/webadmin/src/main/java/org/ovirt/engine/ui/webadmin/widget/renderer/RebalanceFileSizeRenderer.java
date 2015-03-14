package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;

public class RebalanceFileSizeRenderer<T extends Number> extends AbstractRenderer<T> {

    private final static ApplicationMessages messages = AssetProvider.getMessages();

    @Override
    public String render(T size) {
        if(size.longValue() > SizeConverter.BYTES_IN_GB) {
            return messages.rebalanceFileSizeGb(formatSize(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.GB).doubleValue()));
        } else if(size.longValue() > SizeConverter.BYTES_IN_MB) {
            return messages.rebalanceFileSizeMb(formatSize(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.MB).doubleValue()));
        } else if(size.longValue() > SizeConverter.BYTES_IN_KB) {
            return messages.rebalanceFileSizeKb(formatSize(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.KB).doubleValue()));
        } else {
            return messages.rebalanceFileSizeBytes(formatSize(size.doubleValue()));
        }
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }
}
