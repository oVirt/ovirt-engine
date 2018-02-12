package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public enum VmTestPairs {
    EXTERNAL_VM("0") {
        @Override
        Pair<VM, VdsmVm> build() {
            return pairOf(null, createVmInternalData(VMStatus.Up));
        }
    },
    DB_ONLY_NOT_RUNNING("1") {
        @Override
        Pair<VM, VdsmVm> build() {
            return pairOf(createDbVm(), null);
        }
    },
    STATUS_CHANGED_TO_UP("2") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createStatusChangedToUp();
        }
    },
    STATUS_CHANGED_TO_DOWN("3") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createStatusChangedToDown();
        }
    },
    MIGRATING_FROM("4") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createMigratingFrom();
        }
    },
    MIGRATING_TO("5") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createMigratingTo();
        }
    },
    MIGRATION_DONE("6") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createMigrationDone();
        }
    },
    MIGRATION_FAILED("7") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createMigrationFailed();
        }
    },
    HA_VM_CRASHED("8") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createHAThatShutdownAbnormally();
        }
    },
    DST_VM_WITH_STATUS_UP("9") {
        @Override
        Pair<VM, VdsmVm> build() {
            Pair<VM, VdsmVm> pair = pairOf(createDbVm(), createVmInternalData(null));
            pair.getSecond().getVmDynamic().setStatus(VMStatus.Up);
            pair.getSecond().getVmDynamic().setRunOnVds(DST_HOST_ID);
            return pair;
        }
    },
    DST_VM_WITH_STATUS_MIGRATING_TO("A") {
        @Override
        Pair<VM, VdsmVm> build() {
            Pair<VM, VdsmVm> pair = pairOf(createDbVm(), createVmInternalData(null));
            pair.getSecond().getVmDynamic().setStatus(VMStatus.MigratingTo);
            pair.getSecond().getVmDynamic().setRunOnVds(DST_HOST_ID);
            return pair;
        }
    },
    HA_VM_NOT_RUNNING_AND_UNKNOWN("B") {
        @Override
        Pair<VM, VdsmVm> build() {
            return createHANotRunningAndUknown();
        }
    },
    EXTERNAL_VM2("C") {
        @Override
        Pair<VM, VdsmVm> build() {
            return pairOf(null, createVmInternalData(VMStatus.Down));
        }
    },
    EXTERNAL_VM3("D") {
        @Override
        Pair<VM, VdsmVm> build() {
            return pairOf(null, createVmInternalData(VMStatus.Paused));
        }
    };
    public static final Guid DST_HOST_ID = Guid.newGuid();
    public static final Guid SRC_HOST_ID = Guid.newGuid();
    public static final Guid CLUSTER_ID = Guid.newGuid();

    Guid id;
    private Pair<VM, VdsmVm> pair;

    VmTestPairs(String id) {
        this.id = Guid.createGuidFromString(id + "0000000-0000-0000-0000-000000000000");
        pair = build();
    }

    abstract Pair<VM, VdsmVm> build();

    void reset() {
        pair = build();
    }

    VM dbVm() {
        return pair.getFirst();
    }

    VdsmVm vdsmVm() {
        return pair.getSecond();
    }

    Pair<VM, VdsmVm> createStatusChangedToUp() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.Down, VMStatus.Up);
        return pair;
    }

    Pair<VM, VdsmVm> createStatusChangedToDown() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.Up, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Normal);
        return pair;
    }

    Pair<VM, VdsmVm> createMigratingFrom() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.MigratingFrom);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VdsmVm> createMigratingTo() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.MigratingTo);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VdsmVm> createMigrationDone() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Normal);
        pair.getSecond().getVmDynamic().setExitReason(VmExitReason.MigrationSucceeded);
        setDstHost(pair);
        return pair;
    }

    private void setDstHost(Pair<VM, VdsmVm> pair) {
        pair.getFirst().setMigratingToVds(DST_HOST_ID);
    }

    Pair<VM, VdsmVm> createMigrationFailed() {
        Pair<VM, VdsmVm> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.Up);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VdsmVm> createHAThatShutdownAbnormally() {
        Pair<VM, VdsmVm> pair = createPair();
        pair.getFirst().setAutoStartup(true);
        setPairStatuses(pair, VMStatus.Up, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Error);
        return pair;
    }

    Pair<VM, VdsmVm> createHANotRunningAndUknown() {
        Pair<VM, VdsmVm> pair = pairOf(createDbVm(), null);
        pair.getFirst().setAutoStartup(true);
        pair.getFirst().setStatus(VMStatus.Unknown);
        // pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Error);
        return pair;
    }

    Pair<VM, VdsmVm> createPair() {
        Pair<VM, VdsmVm> pair = pairOf(createDbVm(), createVmInternalData(null));
        addWatchDogEvents(pair);
        addClientIpChanged(pair);
        return pair;
    }

    Pair<VM, VdsmVm> pairOf(VM vm, VdsmVm vit) {
        return new Pair<>(vm, vit);
    }

    VM createDbVm() {
        VM vm = new VM();
        vm.setDynamicData(new VmDynamic());
        vm.setId(id);
        vm.setName(name());
        vm.setRunOnVds(SRC_HOST_ID);
        return vm;
    }

    private void setPairStatuses(Pair<VM, VdsmVm> pair, VMStatus dbStatus, VMStatus vdsmStatus) {
        pair.getFirst().setStatus(dbStatus);
        pair.getSecond().getVmDynamic().setStatus(vdsmStatus);
    }

    VdsmVm createVmInternalData(VMStatus status) {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(id);
        vmDynamic.setRunOnVds(SRC_HOST_ID);
        if (status != null) {
            vmDynamic.setStatus(status);
        }
        return new VdsmVm(-1d)
                .setVmDynamic(vmDynamic)
                .setVmStatistics(new VmStatistics())
                .setInterfaceStatistics(new ArrayList<>())
                .setLunsMap(new HashMap<>());
    }

    private void addWatchDogEvents(Pair<VM, VdsmVm> pair) {
        pair.getFirst().getDynamicData().setLastWatchdogEvent(Long.MIN_VALUE);
        pair.getSecond().getVmDynamic().setLastWatchdogEvent(Long.MAX_VALUE);
    }

    private void addClientIpChanged(Pair<VM, VdsmVm> pair) {
        pair.getFirst().setClientIp("1.1.1.1");
        pair.getSecond().getVmDynamic().setClientIp("2.2.2.2");
    }
}
