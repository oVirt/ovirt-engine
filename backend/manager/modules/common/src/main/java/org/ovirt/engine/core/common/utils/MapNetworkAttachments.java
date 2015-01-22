package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

public class MapNetworkAttachments {

    private final List<NetworkAttachment> networkAttachments;

    /*
     *Pointless method to present to please static analyzer.
     */
    public MapNetworkAttachments() {
        this(new ArrayList<NetworkAttachment>(0));
    }

    public MapNetworkAttachments(List<NetworkAttachment> networkAttachments) {
        this.networkAttachments = networkAttachments;
    }

    private <K, I> Map<K, I> group(CalculateKey<I, K> calculateKey, List<I> instances) {
        Map<K, I> result = new HashMap<>(instances.size());
        for (I instance : instances) {
            result.put(calculateKey.keyFrom(instance), instance);
        }
        return result;
    }

    public Map<Guid, NetworkAttachment> byNetworkId() {
        return group(new ByNetworkId(), networkAttachments);
    }

    public Map<String, NetworkAttachment> byNicName() {
        return group(new ByNicName(), networkAttachments);
    }

    private static interface CalculateKey<I, K> {
         K keyFrom(I instance);
    }

    private static class ByNetworkId implements CalculateKey<NetworkAttachment, Guid> {
        @Override
        public Guid keyFrom(NetworkAttachment networkAttachment) {
            return networkAttachment.getNetworkId();
        }
    }

    private static class ByNicName implements CalculateKey<NetworkAttachment, String> {
        @Override
        public String keyFrom(NetworkAttachment networkAttachment) {
            return networkAttachment.getNicName();
        }
    }
}
