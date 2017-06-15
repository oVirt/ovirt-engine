package org.ovirt.engine.ui.common.widget.label;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.ui.common.widget.renderer.VolumeTransportTypeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class VolumeTransportTypeLabel extends ValueLabel<Set<TransportType>> {
    public VolumeTransportTypeLabel() {
        super(new VolumeTransportTypeRenderer());
    }
}
