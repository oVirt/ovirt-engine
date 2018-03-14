package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VmExitReason {
    Unknown(-1),
    Success(0),
    GenericError(1),
    LostQEMUConnection(2),
    LibvirtStartFailed(3),
    MigrationSucceeded(4),
    SaveStateSucceeded(5),
    AdminShutdown(6),
    UserShutdown(7),
    MigrationFailed(8),
    LibvirtDomainMissing(9),
    DestroyedOnStartup(10),
    HostShutdown(11),
    PostcopyMigrationFailed(12),
    DestroyedOnReboot(13),
    DestroyedOnResume(14);

    private final int reason;
    private static final Map<Integer, VmExitReason> valueToReason =
            Stream.of(values()).collect(Collectors.toMap(VmExitReason::getValue, Function.identity()));

    private VmExitReason(int value) {
        this.reason = value;
    }

    public int getValue() {
        return reason;
    }

    public static VmExitReason forValue(int value) {
        return valueToReason.get(value);
    }
}
