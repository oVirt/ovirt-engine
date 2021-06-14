package org.ovirt.engine.core.common.businessentities;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum VMStatus implements Identifiable {
    Unassigned(-1),
    Down(0),
    Up(1),
    PoweringUp(2),
    Paused(4),
    MigratingFrom(5),
    MigratingTo(6),
    Unknown(7),
    NotResponding(8),
    WaitForLaunch(9),
    RebootInProgress(10),
    SavingState(11),
    RestoringState(12),
    Suspended(13),
    ImageIllegal(14),
    ImageLocked(15),
    PoweringDown(16);

    private int value;
    private static final Map<Integer, VMStatus> valueToStatus =
            Stream.of(values()).collect(Collectors.toMap(VMStatus::getValue, Function.identity()));

    VMStatus(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static VMStatus forValue(int value) {
        return valueToStatus.get(value);
    }

    /**
     * This method reflects whether the VM is surely not running in this status
     *
     * <p>Note: There might be cases in which the VM is not running and this method
     * returns false
     *
     * @return true if this status indicates that the VM is not running for sure, otherwise false
     */
    public boolean isNotRunning() {
        return this == Down || this == Suspended || this == ImageLocked || this == ImageIllegal;
    }

    /**
     * This method reflects whether the VM is qualify to migration in this status
     *
     * @return true if this status indicates that the VM is qualify to migration, otherwise false
     */
    public boolean isQualifyToMigrate() {
        return this == Up || this == PoweringUp || this == Paused || this == RebootInProgress;
    }

    /**
     * This method reflects whether the VM is qualified to have its snapshots merged.  For
     * this to be true, the VM must up with qemu in a non-transient state, or down.
     *
     * @return true if this status indicates that the VM status indicates that snapshot merge
     * may be possible, otherwise false
     */
    public boolean isQualifiedForSnapshotMerge() {
        return isQualifiedForLiveSnapshotMerge() || this == Down;
    }

    /**
     * This method reflects whether the VM is qualified to have its snapshots live merged.
     * For this to be true, the VM must up with qemu in a non-transient state.
     *
     * @return true if this status indicates that the VM status indicates that snapshot live merge
     * may be possible, otherwise false
     */
    public boolean isQualifiedForLiveSnapshotMerge() {
        return this == Up || this == PoweringUp || this == Paused || this == RebootInProgress;
    }

    /**
     * This method reflects whether the VM is qualified to start a backup operation (full/incremental).
     * For this to be true, the VM should be either up or powered-off.
     *
     * @return true if the VM is qualified for backup operation, false otherwise
     */
    public boolean isQualifiedForVmBackup() {
        return this == Up || this == Down;
    }

    /**
     * This method reflects whether the VM is qualified for console connection.
     *
     * @return true if the VM status indicates that console connection may be possible, otherwise false
     */
    public boolean isQualifiedForConsoleConnect() {
        return this == PoweringUp || this == Up || this == RebootInProgress || this == PoweringDown ||
                this == Paused || this == MigratingFrom || this == SavingState;
    }

    /**
     * This method reflects whether the VM is surely running or paused in this status
     *
     * @return true if this status indicates that the VM is paused or running for sure, otherwise false
     * @see #isRunning()
     */
    public boolean isRunningOrPaused() {
        return this.isRunning() || this == Paused || this.isHibernating() || this == RestoringState;
    }

    /**
     * This method reflects whether the VM is surely running in this status
     *
     * <p>Note: There might be cases in which the VM is running and this method
     * returns false
     *
     * @return true if this status indicates that the VM is running for sure, otherwise false
     */
    public boolean isRunning() {
        return this == Up || this == PoweringDown
                || this == PoweringUp || this == MigratingFrom || this == MigratingTo || this == WaitForLaunch || this == RebootInProgress;
    }

    /**
     * This method reflects whether the VM is in Up or Paused status
     *
     * @return true if this status indicates that the VM is Up or Paused, otherwise false
     */
    public boolean isUpOrPaused() {
        return this == Up || this == Paused;
    }

    /**
     * This method reflects whether the VM is starting-up
     *
     * @return true if this status indicates that the VM is starting, otherwise false
     */
    public boolean isStarting() {
        return this == WaitForLaunch || this == PoweringUp;
    }

    /**
     * This method reflects whether the VM is starting-up or in Up state
     *
     * @return true if this status indicates that the VM is starting or up, otherwise false
     */
    public boolean isStartingOrUp() {
        return this == Up || isStarting();
    }

    /**
     * This method reflects whether the VM is in the process of getting into
     * hibernation mode.
     *
     * @return true if the VM is in the middle of hibernation process
     */
    public boolean isHibernating() {
        return this == SavingState;
    }

    public boolean isDownOrSuspended() {
        return this == Down || this == Suspended;
    }

    public boolean isSuspended() {
        return this == Suspended;
    }

    /**
     * This method reflects whether live Qos update is allowed on the VM
     *
     * @return true if live QoS update is allowed, false otherwise
     */
    public boolean isQualifiedForQosChange() {
        // TODO - Add other status, if live QoS change is possible when the VM has the status
        return this == Up;
    }

    /**
     * This method reflects whether the guest CPU is surely running.
     *
     * @return true if the guest CPU is surely running in this status, false otherwise
     */
    public boolean isGuestCpuRunning() {
        return EnumSet.of(Up, PoweringUp).contains(this);
    }

    /**
     * This method is used to check if HA VM is powering UP and
     * if other HA VMs should wait for it.
     */
    public boolean isPoweringUpOrMigrating() {
        switch (this) {
        // VM is powering UP
        case WaitForLaunch:
        case PoweringUp:

        // If there is a reason why the VM was paused,
        // others should wait for it.
        case Paused:

        // The VM was restarted during startup,
        case RebootInProgress:

        // User can trigger a migration when the VM is 'PoweringUp'.
        // Other VMs should wait for migration to finish.
        case MigratingTo:
        case MigratingFrom:
        case RestoringState:
            return true;

        default:
            return false;
        }
    }

    /**
     * Check whether the VM is migrating to or from another host.
     *
     * @return true if the status is one of the Migrating* ones
     */
    public boolean isMigrating() {
        return EnumSet.of(MigratingFrom, MigratingTo).contains(this);
    }

    /**
     * Sole purpose of this enum is allow to pass multiple VMStatus items to {@link EditableVmField} annotation
     * without the need of enumerating them.
     */
    public enum Group {
        NONE(EnumSet.noneOf(VMStatus.class)),
        UP(EnumSet.of(Up)),
        RUNNING_OR_PAUSED(EnumSet.of(
                Up,
                PoweringDown,
                PoweringUp,
                MigratingFrom,
                MigratingTo,
                WaitForLaunch,
                RebootInProgress,
                Paused,
                RestoringState,
                SavingState));

        private final EnumSet<VMStatus> states;

        Group(EnumSet<VMStatus> states) {
            this.states = states;
        }

        public EnumSet<VMStatus> getStates() {
            return states;
        }
    }
}
