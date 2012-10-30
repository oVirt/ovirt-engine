package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.widget.renderer.GuidRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class GuidLabel extends ValueLabel<Guid> {

    public GuidLabel() {
        super(new GuidRenderer());
    }
}
