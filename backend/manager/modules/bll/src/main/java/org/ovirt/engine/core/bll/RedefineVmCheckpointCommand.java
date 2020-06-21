package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointIds;

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
        List<VmCheckpoint> checkpoints = vmCheckpointDao.getAllForVm(getVmId());
        if (checkpoints.isEmpty()) {
            log.info("No previous VM checkpoints found for VM '{}', skipping redefining VM checkpoints", getVmId());
            setSucceeded(true);
            return;
        }

        VDSReturnValue listVdsReturnValue = performVmCheckpointsOperation(VDSCommandType.ListVmCheckpoints,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        List<Guid> definedCheckpointsIds = (List<Guid>) listVdsReturnValue.getReturnValue();
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
            log.info("Redefine VM '{}' checkpoint '{}'", getVmId(), checkpoint.getId());
            // Checkpoint can be redefined in bulks, currently redefine one checkpoint at a time
            VDSReturnValue redefineVdsReturnValue = performVmCheckpointsOperation(VDSCommandType.RedefineVmCheckpoints,
                    new VmCheckpointsVDSParameters(getVdsId(), getVmId(), List.of(checkpoint)));
            VmCheckpointIds vmCheckpointIds = (VmCheckpointIds) redefineVdsReturnValue.getReturnValue();
            if (vmCheckpointIds == null || vmCheckpointIds.getError() != null ||
                    vmCheckpointIds.getCheckpointIds().isEmpty()) {
                log.error("Failed to redefine VM '{}' checkpoint '{}', removing the VM checkpoints chain",
                        getVmId(),
                        checkpoint.getId());
                removeCheckpointChain(definedCheckpointsIds);
                return;
            }
        }
        setSucceeded(true);
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
