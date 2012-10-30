package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.text.shared.AbstractRenderer;

public class GuidRenderer extends AbstractRenderer<Guid> {

    public GuidRenderer() {
        super();
    }

    @Override
    public String render(Guid valueToRender) {
        return valueToRender.toString();
    }

}
