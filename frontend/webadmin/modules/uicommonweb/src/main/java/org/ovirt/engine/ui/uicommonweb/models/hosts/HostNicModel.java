package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostNicModel extends Model {

    private final NicLabelModel labelsModel;

    public NicLabelModel getLabelsModel() {
        return labelsModel;
    }

    public HostNicModel(VdsNetworkInterface iface, Collection<String> suggestedLabels, Map<String, String> labelToIface) {
        setTitle(ConstantsManager.getInstance().getMessages().editInterfaceTitle(iface.getName()));
        labelsModel = new NicLabelModel(Collections.singletonList(iface), suggestedLabels, labelToIface);
    }

    public boolean validate() {
        return labelsModel.validate();
    }

}
