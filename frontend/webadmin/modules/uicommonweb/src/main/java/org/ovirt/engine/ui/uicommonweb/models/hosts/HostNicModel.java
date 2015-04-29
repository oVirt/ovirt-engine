package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostNicModel extends Model {

    private static final VfsConfigModel EMPTY_VFS_CONFIG_MODEL = new VfsConfigModel();
    private static final PfNicLabelModel EMPTY_LABELS_MODEL = new PfNicLabelModel();

    private PfNicLabelModel labelsModel = EMPTY_LABELS_MODEL;
    private VfsConfigModel vfsConfigModel = EMPTY_VFS_CONFIG_MODEL;
    private VdsNetworkInterface iface;

    public PfNicLabelModel getLabelsModel() {
        return labelsModel;
    }

    public HostNicModel(VdsNetworkInterface iface,
            Collection<String> suggestedLabels,
            Map<String, String> labelToIface,
            HostNicVfsConfig vfsConfig,
            List<Network> allClusterNetworks,
            SortedSet<String> dcLabels) {
        setTitle(ConstantsManager.getInstance().getMessages().editInterfaceTitle(iface.getName()));
        this.iface = iface;

        if (labelToIface != null) {
            labelsModel = new PfNicLabelModel(Collections.singletonList(iface), suggestedLabels, labelToIface);
        }

        if (vfsConfig != null) {
            vfsConfigModel = new VfsConfigModel(vfsConfig, allClusterNetworks, dcLabels);
        }
    }

    public boolean validate() {
        return labelsModel == EMPTY_LABELS_MODEL ? true : labelsModel.validate();
    }

    public VfsConfigModel getVfsConfigModel() {
        return vfsConfigModel;
    }

    public VdsNetworkInterface getInterface() {
        return iface;
    }

    public boolean hasVfsConfig() {
        return getVfsConfigModel() != HostNicModel.EMPTY_VFS_CONFIG_MODEL;
    }

    public boolean hasLabels() {
        return getLabelsModel() != HostNicModel.EMPTY_LABELS_MODEL;
    }
}
