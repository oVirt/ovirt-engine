package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.ui.common.widget.renderer.BiosTypeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class BiosTypeLabel extends ValueLabel<BiosType> {

    public BiosTypeLabel() {
        super(new BiosTypeRenderer());
    }

    public BiosTypeLabel(BiosTypeRenderer renderer) {
        super(renderer);
    }
}
