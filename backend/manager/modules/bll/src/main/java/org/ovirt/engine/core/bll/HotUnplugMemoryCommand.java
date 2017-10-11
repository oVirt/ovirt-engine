package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * It tries to hot unplug memory device of a VM.
 */
@NonTransactiveCommandAttribute
public class HotUnplugMemoryCommand<P extends HotUnplugMemoryParameters> extends HotUnplugMemoryCommandBase<P> {

    private static final String AUDIT_LOG_VAR_NEW_MEMORY_MB = "newMemoryMb";
    private static final String AUDIT_LOG_VAR_OLD_MEMORY_MB = "oldMemoryMb";

    @Inject
    private VmHandler vmHandler;

    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Inject
    private SnapshotDao snapshotDao;

    public HotUnplugMemoryCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        final int oldMemoryMb = getVm().getMemSizeMb();
        final int oldMinAllocatedMemoryMb = getVm().getMinAllocatedMem();
        final int decrementedMemoryMb = oldMemoryMb - getUnpluggedDeviceSize();
        /*
         * Amount of unplugged memory can't be just subtracted since in combination with delayed (not reported)
         * failures of memory hot unplugs it can led negative values of memory and minMemory.
         */
        final int minMemoryAfterHotUnplugMb = decrementedMemoryMb > oldMinAllocatedMemoryMb
                ? oldMinAllocatedMemoryMb
                : Math.max(0, decrementedMemoryMb);
        final int memoryAfterHotUnplugMb = decrementedMemoryMb >= minMemoryAfterHotUnplugMb && decrementedMemoryMb > 0
                ? decrementedMemoryMb
                : oldMemoryMb;

        addCustomValue(AUDIT_LOG_VAR_NEW_MEMORY_MB, String.valueOf(memoryAfterHotUnplugMb));
        addCustomValue(AUDIT_LOG_VAR_OLD_MEMORY_MB, String.valueOf(oldMemoryMb));

        final VDSReturnValue vdsReturnValue = executeHotUnplug(minMemoryAfterHotUnplugMb);

        final boolean memoryChanged = memoryAfterHotUnplugMb != oldMemoryMb
                || minMemoryAfterHotUnplugMb != oldMinAllocatedMemoryMb;

        if (vdsReturnValue.getSucceeded() && memoryChanged) {
            updateVm(memoryAfterHotUnplugMb, minMemoryAfterHotUnplugMb);
        }

        setSucceeded(true);
    }

    private void updateVm(int memoryAfterHotUnplugMb, int minMemoryAfterHotUnplugMb) {
        updateNextRunConfiguration(memoryAfterHotUnplugMb, minMemoryAfterHotUnplugMb);
        updateCurrentConfiguration(memoryAfterHotUnplugMb, minMemoryAfterHotUnplugMb);
    }

    private void updateCurrentConfiguration(int memoryAfterHotUnplugMb, int minMemoryAfterHotUnplugMb) {
        final VmStatic updatedVmStatic = new VmStatic(getVm().getStaticData());
        updatedVmStatic.setMemSizeMb(memoryAfterHotUnplugMb);
        updatedVmStatic.setMinAllocatedMem(minMemoryAfterHotUnplugMb);
        getVmManager().update(updatedVmStatic);
    }

    private void updateNextRunConfiguration(int memoryAfterHotUnplugMb, int minMemoryAfterHotUnplugMb) {
        final VmStatic nextRunConfigurationStatic = getNextRunConfiguration();
        if (nextRunConfigurationStatic == null) {
            return;
        }
        // Update next run snapshot only if old memory state matches old next run snapshot state
        if (getVm().getMemSizeMb() != nextRunConfigurationStatic.getMemSizeMb()) {
            return;
        }

        nextRunConfigurationStatic.setMemSizeMb(memoryAfterHotUnplugMb);
        nextRunConfigurationStatic.setMinAllocatedMem(minMemoryAfterHotUnplugMb);
        TransactionSupport.executeInNewTransaction(() -> {
            vmHandler.createNextRunSnapshot(
                    getVm(), nextRunConfigurationStatic, null, getCompensationContext());
            return null;
        });
    }

    private VmStatic getNextRunConfiguration() {
        final Snapshot snapshot = snapshotDao.get(getVmId(), Snapshot.SnapshotType.NEXT_RUN);
        if (snapshot == null) {
            return null;
        }
        final VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
        return vm.getStaticData();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getReturnValue().getSucceeded()
                ? AuditLogType.MEMORY_HOT_UNPLUG_SUCCESSFULLY_REQUESTED_PLUS_MEMORY_INFO
                : super.getAuditLogTypeValue();
    }
}
