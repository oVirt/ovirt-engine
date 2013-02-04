package org.ovirt.engine.ui.common.widget.label;


import com.google.gwt.user.client.ui.ValueLabel;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.renderer.ClusterTypeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel.ClusterType;

public class ClusterTypeLabel extends ValueLabel<ClusterType> {

    public ClusterTypeLabel(CommonApplicationConstants constants) {
        super(new ClusterTypeRenderer(constants));
    }
}
