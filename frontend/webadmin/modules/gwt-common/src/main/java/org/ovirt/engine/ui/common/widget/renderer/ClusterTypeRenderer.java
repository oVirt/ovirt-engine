package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;

import com.google.gwt.text.shared.AbstractRenderer;

public class ClusterTypeRenderer extends AbstractRenderer<ClusterGeneralModel.ClusterType> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

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
