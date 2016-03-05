package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class UserConfiguredNetworkData {
    private final List<NetworkAttachment> networkAttachments;
    private final Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName;

    public UserConfiguredNetworkData() {
        this(Collections.<NetworkAttachment>emptyList(), new HashMap<String, UserOverriddenNicValues>());
    }

    public UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments,
            Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName) {
        this.networkAttachments = networkAttachments;
        this.userOverriddenNicValuesByNicName = userOverriddenNicValuesByNicName;
    }

    public List<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public Map<String, UserOverriddenNicValues> getUserOverriddenNicValuesByNicName() {
        return userOverriddenNicValuesByNicName;
    }
}
