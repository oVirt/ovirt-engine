package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
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
            HostNetworkQos networkQos,
            DnsResolverConfiguration reportedDnsResolverConfiguration) {
        super(network, nic, networkAttachment, networkQos, reportedDnsResolverConfiguration);
    }

    @Override
    protected Model setTitle() {
        return setTitle(
                ConstantsManager.getInstance().getMessages().editManagementNetworkTitle(getNetwork().getName()));
    }
}
