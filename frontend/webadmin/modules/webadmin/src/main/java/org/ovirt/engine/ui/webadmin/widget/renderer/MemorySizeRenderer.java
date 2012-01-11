package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class MemorySizeRenderer<T extends Number> extends AbstractRenderer<T> {

    @Override
    public String render(T sizeInMB) {
        return sizeInMB != null ? sizeInMB.toString() + " MB"
                : ClientGinjectorProvider.instance().getApplicationConstants().unAvailablePropertyLabel();
    }

}
