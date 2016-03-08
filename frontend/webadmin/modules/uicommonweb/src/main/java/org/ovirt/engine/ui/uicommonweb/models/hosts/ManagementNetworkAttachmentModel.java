package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ManagementNetworkAttachmentModel extends NetworkAttachmentModel {

    public ManagementNetworkAttachmentModel(Network network,
            VdsNetworkInterface nic,
            NetworkAttachment networkAttachment,
            HostNetworkQos networkQos) {
        super(network, nic, networkAttachment, networkQos);

        getIpv4Gateway().setIsAvailable(true);
        getIpv6Gateway().setIsAvailable(true);
    }

    @Override
    protected Model setTitle() {
        return setTitle(
                ConstantsManager.getInstance().getMessages().editManagementNetworkTitle(getNetwork().getName()));
    }
}
