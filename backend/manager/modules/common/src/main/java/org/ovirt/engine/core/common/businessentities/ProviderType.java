package org.ovirt.engine.core.common.businessentities;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;

/**
 * The provider type determines what external provider is used.
 */
public enum ProviderType implements Identifiable {
    OPENSTACK_NETWORK(0, true, true, true, true, VdcObjectType.Network),
    FOREMAN(1, false, false, false, false, VdcObjectType.VDS),
    OPENSTACK_IMAGE(2, true, true, false, true, VdcObjectType.Storage),
    OPENSTACK_VOLUME(3, true, true, false, true, VdcObjectType.Storage),
    VMWARE(4, false, false, false, false, VdcObjectType.VM),
    EXTERNAL_NETWORK(5, true, true, true, false, VdcObjectType.Network);

    private int value;
    private Set<VdcObjectType> providedTypes;
    private boolean isTenantAware;
    private boolean isAuthUrlAware;
    private boolean isReadOnlyAware;
    private boolean isTenantRequired;

    private ProviderType(int value,
            boolean isTenantAware,
            boolean isAuthUrlAware,
            boolean isReadOnlyAware,
            boolean isTenantRequired,
            VdcObjectType... providedTypes) {

        this.value = value;
        this.isTenantAware = isTenantAware;
        this.isAuthUrlAware = isAuthUrlAware;
        this.isReadOnlyAware = isReadOnlyAware;
        this.isTenantRequired = isTenantRequired;
        this.providedTypes = new HashSet<>();
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

    public boolean isTenantAware() {
        return isTenantAware;
    }

    public boolean isAuthUrlAware() {
        return isAuthUrlAware;
    }

    public boolean isReadOnlyAware() {
        return isReadOnlyAware;
    }

    public boolean isTenantRequired() {
        return isTenantRequired;
    }
}
