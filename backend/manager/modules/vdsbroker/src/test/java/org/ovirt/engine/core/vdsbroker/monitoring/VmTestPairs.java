package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public enum VmTestPairs {
    EXTERNAL_VM("0") {
        @Override
        Pair<VM, VmInternalData> build() {
            return pairOf(null, createVmInternalData());
        }
    },
    DB_ONLY_NOT_RUNNING("1") {
        @Override
        Pair<VM, VmInternalData> build() {
            return pairOf(createDbVm(), null);
        }
    },
    STATUS_CHANGED_TO_UP("2") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createStatusChangedToUp();
        }
    },
    STATUS_CHANGED_TO_DOWN("3") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createStatusChangedToDown();
        }
    },
    MIGRATING_FROM("4") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createMigratingFrom();
        }
    },
    MIGRATING_TO("5") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createMigratingTo();
        }
    },
    MIGRATION_DONE("6") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createMigrationDone();
        }
    },
    MIGRATION_FAILED("7") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createMigrationFailed();
        }
    },
    HA_VM_CRASHED("8") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createHAThatShutdownAbnormally();
        }
    },
    DST_VM_WITH_STATUS_UP("9") {
        @Override
        Pair<VM, VmInternalData> build() {
            Pair<VM, VmInternalData> pair = pairOf(createDbVm(), createVmInternalData());
            pair.getSecond().getVmDynamic().setStatus(VMStatus.Up);
            pair.getSecond().getVmDynamic().setRunOnVds(DST_HOST_ID);
            return pair;
        }
    },
    DST_VM_WITH_STATUS_MIGRATING_TO("A") {
        @Override
        Pair<VM, VmInternalData> build() {
            Pair<VM, VmInternalData> pair = pairOf(createDbVm(), createVmInternalData());
            pair.getSecond().getVmDynamic().setStatus(VMStatus.MigratingTo);
            pair.getSecond().getVmDynamic().setRunOnVds(DST_HOST_ID);
            return pair;
        }
    },
    HA_VM_NOT_RUNNING_AND_UNKNOWN("B") {
        @Override
        Pair<VM, VmInternalData> build() {
            return createHANotRunningAndUknown();
        }
    };
    public static final Guid DST_HOST_ID = Guid.newGuid();
    public static final Guid SRC_HOST_ID = Guid.newGuid();
    public static final Guid CLUSTER_ID = Guid.newGuid();

    Guid id;
    private Pair<VM, VmInternalData> pair;

    VmTestPairs(String id) {
        this.id = Guid.createGuidFromString(id + "0000000-0000-0000-0000-000000000000");
        pair = build();
    }

    abstract Pair<VM, VmInternalData> build();

    void reset() {
        pair = build();
    }

    VM dbVm() {
        return pair.getFirst();
    }

    VmInternalData vdsmVm() {
        return pair.getSecond();
    }

    Pair<VM, VmInternalData> createStatusChangedToUp() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.Down, VMStatus.Up);
        return pair;
    }

    Pair<VM, VmInternalData> createStatusChangedToDown() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.Up, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Normal);
        return pair;
    }

    Pair<VM, VmInternalData> createMigratingFrom() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.MigratingFrom);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VmInternalData> createMigratingTo() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.MigratingTo);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VmInternalData> createMigrationDone() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Normal);
        setDstHost(pair);
        return pair;
    }

    private void setDstHost(Pair<VM, VmInternalData> pair) {
        pair.getFirst().setMigratingToVds(DST_HOST_ID);
    }

    Pair<VM, VmInternalData> createMigrationFailed() {
        Pair<VM, VmInternalData> pair = createPair();
        setPairStatuses(pair, VMStatus.MigratingFrom, VMStatus.Up);
        setDstHost(pair);
        return pair;
    }

    Pair<VM, VmInternalData> createHAThatShutdownAbnormally() {
        Pair<VM, VmInternalData> pair = createPair();
        pair.getFirst().setAutoStartup(true);
        setPairStatuses(pair, VMStatus.Up, VMStatus.Down);
        pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Error);
        return pair;
    }

    Pair<VM, VmInternalData> createHANotRunningAndUknown() {
        Pair<VM, VmInternalData> pair = pairOf(createDbVm(), null);
        pair.getFirst().setAutoStartup(true);
        pair.getFirst().setStatus(VMStatus.Unknown);
        // pair.getSecond().getVmDynamic().setExitStatus(VmExitStatus.Error);
        return pair;
    }

    Pair<VM, VmInternalData> createPair() {
        Pair<VM, VmInternalData> pair = pairOf(createDbVm(), createVmInternalData());
        addWatchDogEvents(pair);
        addClientIpChanged(pair);
        return pair;
    }

    Pair<VM, VmInternalData> pairOf(VM vm, VmInternalData vit) {
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

    private void setPairStatuses(Pair<VM, VmInternalData> pair, VMStatus dbStatus, VMStatus vdsmStatus) {
        pair.getFirst().setStatus(dbStatus);
        pair.getSecond().getVmDynamic().setStatus(vdsmStatus);
    }

    VmInternalData createVmInternalData() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(id);
        vmDynamic.setRunOnVds(SRC_HOST_ID);
        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setVmBalloonInfo(new VmBalloonInfo());
        vmStatistics.setInterfaceStatistics(new ArrayList<>());
        ArrayList<VmGuestAgentInterface> vmGuestAgentInterfaces = null;
        HashMap<String, LUNs> lunsMap = new HashMap<>();
        VmInternalData vmInternalData =
                new VmInternalData(vmDynamic, vmStatistics,
                        vmGuestAgentInterfaces, lunsMap, -1d);
        return vmInternalData;
    }

    private void addWatchDogEvents(Pair<VM, VmInternalData> pair) {
        pair.getFirst().getDynamicData().setLastWatchdogEvent(Long.MIN_VALUE);
        pair.getSecond().getVmDynamic().setLastWatchdogEvent(Long.MAX_VALUE);
    }

    private void addClientIpChanged(Pair<VM, VmInternalData> pair) {
        pair.getFirst().setClientIp("1.1.1.1");
        pair.getSecond().getVmDynamic().setClientIp("2.2.2.2");
    }
}
