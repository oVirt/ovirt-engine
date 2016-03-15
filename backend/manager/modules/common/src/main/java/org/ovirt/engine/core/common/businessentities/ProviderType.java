package org.ovirt.engine.core.common.businessentities;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;

/**
 * The provider type determines what external provider is used.
 */
public enum ProviderType implements Identifiable {
    OPENSTACK_NETWORK(0, true, true, false, VdcObjectType.Network),
    FOREMAN(1, false, false, false, VdcObjectType.VDS),
    OPENSTACK_IMAGE(2, true, true, false, VdcObjectType.Storage),
    OPENSTACK_VOLUME(3, true, true, false, VdcObjectType.Storage),
    VMWARE(4, false, false, false, VdcObjectType.VM),
    EXTERNAL_NETWORK(5, false, true, true, VdcObjectType.Network);

    private int value;
    private Set<VdcObjectType> providedTypes;
    private boolean isTenantAware;
    private boolean isAuthUrlAware;
    private boolean isReadOnlyAware;

    private ProviderType(int value,
            boolean isTenantAware,
            boolean isAuthUrlAware,
            boolean isReadOnlyAware,
            VdcObjectType... providedTypes) {

        this.value = value;
        this.isTenantAware = isTenantAware;
        this.isAuthUrlAware = isAuthUrlAware;
        this.isReadOnlyAware = isReadOnlyAware;
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

    public boolean isTenantAware(){
        return isTenantAware;
    }

    public boolean isAuthUrlAware(){
        return isAuthUrlAware;
    }

    public boolean isReadOnlyAware(){
        return isAuthUrlAware;
    }
}
