package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public class MemorySizeRenderer<T extends Number> extends AbstractRenderer<T> {
    @Override
    public String render(T sizeInMB) {
        return sizeInMB != null ? sizeInMB.toString() + " MB" : new EmptyValueRenderer<String>(true).render(null);
    }
}
