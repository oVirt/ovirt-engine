package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.uicommonweb.models.gluster.SizeConverter;
import org.ovirt.engine.ui.uicommonweb.models.gluster.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;

import com.google.gwt.text.shared.AbstractRenderer;

public class RebalanceFileSizeRenderer<T extends Number> extends AbstractRenderer<T> {

    private ApplicationMessages messages;

    @Override
    public String render(T size) {
        if(size.longValue() > SizeConverter.BYTES_IN_GB) {
            return messages.rebalanceFileSizeGb(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.GB));
        } else if(size.longValue() > SizeConverter.BYTES_IN_MB) {
            return messages.rebalanceFileSizeMb(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.MB));
        } else if(size.longValue() > SizeConverter.BYTES_IN_KB) {
            return messages.rebalanceFileSizeKb(SizeConverter.convert(size.longValue(), SizeUnit.BYTES, SizeUnit.KB));
        } else {
            return messages.rebalanceFileSizeBytes(size.longValue());
        }
    }

    public RebalanceFileSizeRenderer(ApplicationMessages messages) {
        this.messages = messages;
    }
}
