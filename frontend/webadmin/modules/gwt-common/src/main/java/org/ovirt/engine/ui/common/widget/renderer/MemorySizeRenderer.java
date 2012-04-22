package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.text.shared.AbstractRenderer;

public class MemorySizeRenderer<T extends Number> extends AbstractRenderer<T> {

    private final CommonApplicationConstants constants;

    public MemorySizeRenderer(CommonApplicationConstants constants) {
        this.constants = constants;
    }

    @Override
    public String render(T sizeInMB) {
        return sizeInMB != null ? sizeInMB.toString() + " MB" : constants.unAvailablePropertyLabel(); //$NON-NLS-1$
    }

}
