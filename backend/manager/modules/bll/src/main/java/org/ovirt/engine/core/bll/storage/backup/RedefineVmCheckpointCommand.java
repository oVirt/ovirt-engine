package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointIds;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RedefineVmCheckpointCommand<T extends VmBackupParameters> extends VmCommand<T> {

    @Inject
    private VmCheckpointDao vmCheckpointDao;

    public RedefineVmCheckpointCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmBackup().getVmId());
        setVdsId(getVm().getRunOnVds());
    }

    @Override
    public void executeCommand() {
        if (FeatureSupported.isBackupSingleCheckpointSupported(getCluster().getCompatibilityVersion())) {
            setSucceeded(redefineFromCheckpoint());
            return;
        }

        List<VmCheckpoint> checkpoints = vmCheckpointDao.getAllForVm(getVmId());
        if (checkpoints.isEmpty()) {
            log.info("No previous VM checkpoints found for VM '{}', skipping redefining VM checkpoints", getVmId());
            setSucceeded(true);
            return;
        }

        VDSReturnValue listVdsReturnValue = performVmCheckpointsOperation(VDSCommandType.ListVmCheckpoints,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        List<Guid> definedCheckpointsIds = (List<Guid>) listVdsReturnValue.getReturnValue();
        definedCheckpointsIds.sort(
                Comparator.comparingInt(
                        checkpoints.stream().map(VmCheckpoint::getId).collect(Collectors.toList())::indexOf));
        List<VmCheckpoint> checkpointsToSync = getCheckpointIdsToSync(checkpoints, definedCheckpointsIds);

        if (checkpointsToSync == null) {
            log.warn("Checkpoints chain for VM '{}' isn't synced with libvirt, removing the VM checkpoints chain",
                    getVmId());
            removeCheckpointChain(definedCheckpointsIds);
            return;
        }

        if (checkpointsToSync.isEmpty()) {
            log.info("Checkpoints chain is already defined for VM '{}'", getVmId());
            setSucceeded(true);
            return;
        }

        for (VmCheckpoint checkpoint : checkpointsToSync) {
            boolean checkpointRedefined = false;
            // Checkpoint can be redefined in bulks, currently redefine one checkpoint at a time
            VDSReturnValue redefineVdsReturnValue = performRedefineCheckpoint(checkpoint);
            if (redefineVdsReturnValue != null) {
                VmCheckpointIds vmCheckpointIds = (VmCheckpointIds) redefineVdsReturnValue.getReturnValue();
                checkpointRedefined = vmCheckpointIds != null && vmCheckpointIds.getError() == null &&
                        !vmCheckpointIds.getCheckpointIds().isEmpty();
            }

            if (!checkpointRedefined) {
                log.error("Failed to redefine VM '{}' checkpoint '{}', removing the VM checkpoints chain",
                        getVmId(),
                        checkpoint.getId());
                removeCheckpointChain(definedCheckpointsIds);
                return;
            }
        }
        setSucceeded(true);
    }

    private boolean redefineFromCheckpoint() {
        if (getParameters().getVmBackup().getFromCheckpointId() == null) {
            log.info("The checkpoint to start the backup from wasn't provided for VM '{}' backup," +
                    " skipping redefining the VM checkpoint", getVmId());
            return true;
        }

        // Redefine fromCheckpoint, no need to check if checkpoint already defined,
        // redefinition will succeed in this case.
        VmCheckpoint fromCheckpoint = vmCheckpointDao.get(getParameters().getVmBackup().getFromCheckpointId());
        boolean checkpointRedefined = false;

        VDSReturnValue redefineVdsReturnValue = performRedefineCheckpoint(fromCheckpoint);
        if (redefineVdsReturnValue != null) {
            VmCheckpointIds vmCheckpointIds = (VmCheckpointIds) redefineVdsReturnValue.getReturnValue();
            checkpointRedefined = vmCheckpointIds != null &&
                    vmCheckpointIds.getError() == null &&
                    !vmCheckpointIds.getCheckpointIds().isEmpty() &&
                    fromCheckpoint.getId().equals(Guid.createGuidFromString(vmCheckpointIds.getCheckpointIds().get(0)));
        }

        if (!checkpointRedefined) {
            log.error("Failed to redefine VM '{}' checkpoint '{}', removing the VM checkpoints chain",
                    getVmId(),
                    fromCheckpoint.getId());

            VDSReturnValue listVdsReturnValue = performVmCheckpointsOperation(VDSCommandType.ListVmCheckpoints,
                    new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
            List<Guid> definedCheckpointsIds = (List<Guid>) listVdsReturnValue.getReturnValue();
            removeCheckpointChain(definedCheckpointsIds);
        }

        return checkpointRedefined;
    }

    private List<VmCheckpoint> getCheckpointIdsToSync(List<VmCheckpoint> vmCheckpoints,
            List<Guid> definedCheckpointsIds) {
        if (definedCheckpointsIds.isEmpty()) {
            // Need to redefine the entire chain
            return vmCheckpoints;
        }

        Iterator<Guid> definedCheckpointsIterator = definedCheckpointsIds.listIterator();
        for (int i = 0; i < vmCheckpoints.size(); i++) {
            if (!definedCheckpointsIterator.hasNext()) {
                // A part of the DB checkpoints chains should be redefined
                return vmCheckpoints.subList(i, vmCheckpoints.size());
            }
            if (!vmCheckpoints.get(i).getId().equals(definedCheckpointsIterator.next())) {
                // The checkpoint chains in the DB and in Libvirt aren't synced
                return null;
            }
        }

        // Checkpoints chain is synced
        return Collections.emptyList();
    }

    private VDSReturnValue performRedefineCheckpoint(VmCheckpoint checkpoint) {
        log.info("Redefine VM '{}' checkpoint '{}'", getVmId(), checkpoint.getId());
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = runVdsCommand(VDSCommandType.RedefineVmCheckpoints,
                    buildRedefineCheckpointsParameters(checkpoint));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
        } catch (EngineException e) {
            log.error("Failed to redefine VM '{}' checkpoints: {}", getVmId(), e);
            return null;
        }
        return vdsRetVal;
    }

    private VmBackupVDSParameters buildRedefineCheckpointsParameters(VmCheckpoint checkpoint) {
        // Only the disks that exist on the VM can be used for generating the checkpoint XML
        vmHandler.updateDisksFromDb(getVm());
        List<DiskImage> checkpointDisks = vmCheckpointDao.getDisksByCheckpointId(checkpoint.getId())
                .stream()
                .filter(diskImage -> getVm().getDiskMap().containsKey(diskImage.getId()))
                .collect(Collectors.toList());

        VmBackup vmBackup = new VmBackup();
        // Checkpoint's backup isn't needed for redefining a checkpoint
        vmBackup.setId(Guid.Empty);
        vmBackup.setVmId(getVmId());
        vmBackup.setDisks(checkpointDisks);
        vmBackup.setToCheckpointId(checkpoint.getId());
        vmBackup.setFromCheckpointId(checkpoint.getParentId());
        vmBackup.setCreationDate(checkpoint.getCreationDate());

        return new VmBackupVDSParameters(getVdsId(), vmBackup);
    }

    private VDSReturnValue performVmCheckpointsOperation(VDSCommandType vdsCommandType,
            VdsAndVmIDVDSParametersBase params) {
        VDSReturnValue vdsRetVal;
        try {
            vdsRetVal = runVdsCommand(vdsCommandType, params);
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                engineException.setVdsReturnValue(vdsRetVal);
                throw engineException;
            }
        } catch (EngineException e) {
            log.error("Failed to execute '{}': {}", vdsCommandType, e);
            throw e;
        }

        return vdsRetVal;
    }

    private void removeCheckpointChain(List<Guid> definedCheckpointsIds) {
        for (Guid checkpointId : definedCheckpointsIds) {
            // Best effort to remove all checkpoints in the chain from libvirt,
            // starting from the oldest checkpoint to the leaf.
            VmCheckpoint vmCheckpoint = new VmCheckpoint();
            vmCheckpoint.setId(checkpointId);
            performVmCheckpointsOperation(VDSCommandType.DeleteVmCheckpoints,
                    new VmCheckpointsVDSParameters(getVdsId(), getVmId(), List.of(vmCheckpoint)));
        }

        // Removing all the checkpoints from the Engine database
        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpointDao.removeAllCheckpointsByVmId(getVmId());
            return null;
        });
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        getParameters().getVmBackup()
                .getDisks()
                .forEach(disk -> permissionList.add(new PermissionSubject(disk.getId(),
                        VdcObjectType.Disk,
                        ActionGroup.BACKUP_DISK)));
        return permissionList;
    }
}
