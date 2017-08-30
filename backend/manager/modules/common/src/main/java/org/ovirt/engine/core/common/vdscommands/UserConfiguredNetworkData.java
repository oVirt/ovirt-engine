package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

public class UserConfiguredNetworkData {
    private final List<NetworkAttachment> networkAttachments;
    private final Set<Guid> removedNetworkAttachments;
    private final Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName;

    public UserConfiguredNetworkData() {
        this(Collections.emptyList(), Collections.emptySet(), new HashMap<>());
    }

    public UserConfiguredNetworkData(List<NetworkAttachment> networkAttachments,
            Set<Guid> removedNetworkAttachments,
            Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName) {
        this.networkAttachments = networkAttachments;
        this.removedNetworkAttachments = removedNetworkAttachments;
        this.userOverriddenNicValuesByNicName = userOverriddenNicValuesByNicName;
    }

    public List<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public Map<String, UserOverriddenNicValues> getUserOverriddenNicValuesByNicName() {
        return userOverriddenNicValuesByNicName;
    }

    public Set<Guid> getRemovedNetworkAttachments() {
        return removedNetworkAttachments;
    }
}
