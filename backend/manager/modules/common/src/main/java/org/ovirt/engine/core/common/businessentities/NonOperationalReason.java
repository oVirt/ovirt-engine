package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum NonOperationalReason {

    NONE(0),
    GENERAL(1),
    CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER(2),
    STORAGE_DOMAIN_UNREACHABLE(3),
    NETWORK_UNREACHABLE(4),
    VERSION_INCOMPATIBLE_WITH_CLUSTER(5),
    KVM_NOT_RUNNING(6),
    TIMEOUT_RECOVERING_FROM_CRASH(7),
    VM_NETWORK_IS_BRIDGELESS(8),
    GLUSTER_COMMAND_FAILED(9),
    GLUSTER_HOST_UUID_NOT_FOUND(10),
    EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER(11),
    UNTRUSTED(12),
    UNINITIALIZED(13),
    CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER(14),
    GLUSTER_HOST_UUID_ALREADY_EXISTS(15),
    ARCHITECTURE_INCOMPATIBLE_WITH_CLUSTER(16),
    NETWORK_INTERFACE_IS_DOWN(17),
    RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER(18),
    EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER_LEVEL(19),
    MIXING_RHEL_VERSIONS_IN_CLUSTER(20),
    HOST_FEATURES_INCOMPATIBILE_WITH_CLUSTER(21),
    LIBRBD_PACKAGE_NOT_AVAILABLE(22),
    LIBVIRT_SECRETS_REGISTRATION_FAILURE(23),
    VDS_CANNOT_CONNECT_TO_GLUSTERFS(24),
    KUBEVIRT_NOT_SCHEDULABLE(25),
    HE_HOST_IN_NON_HE_CLUSTER(26),
    FIPS_INCOMPATIBLE_WITH_CLUSTER(27),
    CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION(28);

    private final int value;

    private static final Map<Integer, NonOperationalReason> valueMap = new HashMap<>(values().length);

    static {
        for (NonOperationalReason reason : values()) {
            valueMap.put(reason.value, reason);
        }
    }

    private NonOperationalReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NonOperationalReason forValue(int value) {
        return valueMap.get(value);
    }
}
