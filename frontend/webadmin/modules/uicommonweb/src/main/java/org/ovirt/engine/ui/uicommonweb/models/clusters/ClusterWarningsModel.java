package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ClusterEditWarnings;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterWarningsModel extends ConfirmationModel {

    private ClusterEditWarnings warnings;

    public ClusterWarningsModel() {
        setTitle(ConstantsManager.getInstance().getConstants().confirmClusterWarnings());
        setHelpTag(HelpTag.cluster_edit_warnings);
        setHashName("cluster_edit_warnings"); //$NON-NLS-1$
    }

    public void init(ClusterEditWarnings warnings) {
        this.warnings = warnings;
    }

    public List<ClusterEditWarnings.Warning> getHostWarnings() {
        return warnings.getHostWarnings();
    }

    public List<ClusterEditWarnings.Warning> getVmWarnings() {
        return warnings.getVmWarnings();
    }
}
