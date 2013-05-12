package org.ovirt.engine.core.common.businessentities;

/**
 * The provider type determines what external provider is used.
 */
public enum ProviderType implements Identifiable {
    OPENSTACK_NETWORK(0),
    FOREMAN(1);

    private int value;

    private ProviderType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
