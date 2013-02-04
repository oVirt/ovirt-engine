package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;

public class ClusterTypeRenderer extends AbstractRenderer<ClusterGeneralModel.ClusterType> {

    private CommonApplicationConstants constants;

    public ClusterTypeRenderer(CommonApplicationConstants constants) {
        this.constants = constants;
    }

    @Override
    public String render(ClusterGeneralModel.ClusterType object) {
        switch (object) {
            case BOTH:
                return constants.virt() + constants.andBreak() + constants.gluster();
            case GLUSTER:
                return constants.gluster();
            case VIRT:
                return constants.virt();
            default:
                return ""; //$NON-NLS-1$
        }
    }
}
