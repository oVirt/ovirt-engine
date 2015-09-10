package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class UserConfiguredNetworkData {
    private final List<NetworkAttachment> networkAttachments;
    private final List<VdsNetworkInterface> nics;
    private final CustomPropertiesForVdsNetworkInterface customProperties;

    public UserConfiguredNetworkData() {
        this(Collections.<NetworkAttachment>emptyList(), Collections.<VdsNetworkInterface>emptyList());
    }

    public UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments, List<VdsNetworkInterface> nics) {
        this(networkAttachments, nics, null);
    }

    public UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments,
            List<VdsNetworkInterface> nics,
            CustomPropertiesForVdsNetworkInterface customProperties) {
        this.networkAttachments = networkAttachments;
        this.nics = nics;
        this.customProperties = customProperties;
    }

    public List<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public List<VdsNetworkInterface> getNics() {
        return nics;
    }

    public CustomPropertiesForVdsNetworkInterface getCustomProperties() {
        return customProperties;
    }
}
