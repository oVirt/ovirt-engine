package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class DiskSizeRenderer extends AbstractRenderer<Long> {
    @Override
    public String render(Long sizeInBytes) {
        long sizeInGB = (long) (sizeInBytes / Math.pow(1024, 3));

        return sizeInGB >= 1 ? sizeInGB + " GB" : "< 1 GB";
    }
}