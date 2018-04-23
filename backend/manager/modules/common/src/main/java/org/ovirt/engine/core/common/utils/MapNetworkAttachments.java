package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

public class MapNetworkAttachments {

    private final Collection<NetworkAttachment> networkAttachments;

    /*
     *Pointless method to present to please static analyzer.
     */
    public MapNetworkAttachments() {
        this(new ArrayList<NetworkAttachment>(0));
    }

    public MapNetworkAttachments(Collection<NetworkAttachment> networkAttachments) {
        this.networkAttachments = networkAttachments;
    }

    private <K, I> Map<K, I> group(CalculateKey<I, K> calculateKey, Collection<I> instances) {
        Map<K, I> result = new HashMap<>(instances.size());
        for (I instance : instances) {
            result.put(calculateKey.keyFrom(instance), instance);
        }
        return result;
    }

    private <K, I> Map<K, Set<I>> groupMultipleValues(CalculateKey<I, K> calculateKey, Collection<I> instances) {
        Map<K, Set<I>> result = new HashMap<>(instances.size());

        for (I instance : instances) {
            getSetForKey(calculateKey.keyFrom(instance), result).add(instance);
        }
        return result;
    }

    private <K, I> Set<I> getSetForKey(K key, Map<K, Set<I>> result) {
        if (!result.containsKey(key)) {
            Set<I> values = new HashSet<>();
            result.put(key, values);
            return values;
        } else {
            return result.get(key);
        }
    }

    public Set<String> nicNames() {
        Set<String> result = new HashSet<>();
        for (NetworkAttachment attachment : networkAttachments) {
            result.add(new ByNicName().keyFrom(attachment));
        }
        return result;
    }

    public Map<Guid, NetworkAttachment> byNetworkId() {
        return group(new ByNetworkId(), networkAttachments);
    }

    public Map<Guid, Set<NetworkAttachment>> byNicId() {
        return groupMultipleValues(new ByNicId(), networkAttachments);
    }

    private interface CalculateKey<I, K> {
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

    private static class ByNicId implements CalculateKey<NetworkAttachment, Guid> {
        @Override
        public Guid keyFrom(NetworkAttachment networkAttachment) {
            return networkAttachment.getNicId();
        }
    }
}
