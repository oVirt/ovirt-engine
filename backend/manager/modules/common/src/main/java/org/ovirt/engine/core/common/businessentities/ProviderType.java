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
    OPENSTACK_IMAGE(2, true, false, false, VdcObjectType.Storage),
    OPENSTACK_VOLUME(3, true, false, false, VdcObjectType.Storage),
    VMWARE(4, false, false, false, VdcObjectType.VM),
    EXTERNAL_NETWORK(5, true, true, true, VdcObjectType.Network),
    KVM(6, false, false, false, VdcObjectType.VM),
    XEN(7, false, false, false, VdcObjectType.VM),
    KUBEVIRT(8, false, false, false, VdcObjectType.Cluster);

    private int value;
    private Set<VdcObjectType> providedTypes;
    private boolean isAuthUrlAware;
    private boolean isReadOnlyAware;
    private boolean isUnmanagedAware;

    private ProviderType(int value,
            boolean isAuthUrlAware,
            boolean isReadOnlyAware,
            boolean isUnmanagedAware,
            VdcObjectType... providedTypes) {

        this.value = value;
        this.isAuthUrlAware = isAuthUrlAware;
        this.isReadOnlyAware = isReadOnlyAware;
        this.providedTypes = new HashSet<>();
        this.isUnmanagedAware = isUnmanagedAware;
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

    public boolean isAuthUrlAware() {
        return isAuthUrlAware;
    }

    public boolean isReadOnlyAware() {
        return isReadOnlyAware;
    }

    public boolean isUnmanagedAware() {
        return isUnmanagedAware;
    }
}
