package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.ClusterTypeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel.ClusterType;

import com.google.gwt.user.client.ui.ValueLabel;

public class ClusterTypeLabel extends ValueLabel<ClusterType> {

    public ClusterTypeLabel() {
        super(new ClusterTypeRenderer());
    }
}
