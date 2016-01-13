package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ManagementNetworkAttachmentModel extends NetworkAttachmentModel {

    public ManagementNetworkAttachmentModel(Network network, VdsNetworkInterface nic, NetworkAttachment networkAttachment, HostNetworkQos networkQos) {
        super(network, nic, networkAttachment, networkQos);
        setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
        setNoneBootProtocolAvailable(false);
        getIpv4Gateway().setIsAvailable(true);
    }
}
