package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class MemorySizeRenderer<T extends Number> extends AbstractRenderer<T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Override
    public String render(T sizeInMB) {
        return sizeInMB != null ? messages.megabytes(sizeInMB.toString()) : constants.unAvailablePropertyLabel();
    }

}
