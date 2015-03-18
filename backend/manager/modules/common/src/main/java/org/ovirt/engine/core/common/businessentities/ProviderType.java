package org.ovirt.engine.core.common.businessentities;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;

/**
 * The provider type determines what external provider is used.
 */
public enum ProviderType implements Identifiable {
    OPENSTACK_NETWORK(0, VdcObjectType.Network),
    FOREMAN(1, VdcObjectType.VDS),
    OPENSTACK_IMAGE(2, VdcObjectType.Storage),
    OPENSTACK_VOLUME(3, VdcObjectType.Storage);

    private int value;
    private Set<VdcObjectType> providedTypes;

    private ProviderType(int value, VdcObjectType... providedTypes) {
        this.value = value;
        this.providedTypes = new HashSet<VdcObjectType>();
        for (VdcObjectType providedType : providedTypes) {
            this.providedTypes.add(providedType);
        }
    }

    @Override
    public int getValue() {
        return value;
    }

    public Set<VdcObjectType> getProvidedTypes() {
        return providedTypes;
    }
}
