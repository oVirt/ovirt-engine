package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CloneCinderDisksCommand<T extends CloneCinderDisksParameters> extends CommandBase<T> {

    public CloneCinderDisksCommand(T parameters) {
        this(parameters, null);
    }

    public CloneCinderDisksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        boolean isSucceeded = true;
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk disk : getParameters().getCinderDisks()) {
            ImagesContainterParametersBase params = buildChildCommandParameters(disk);
            Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                    VdcActionType.CloneSingleCinderDisk,
                    params,
                    cloneContext().withoutExecutionContext().withoutLock());
            try {
                VdcReturnValueBase vdcReturnValueBase = future.get();
                if (!vdcReturnValueBase.getSucceeded()) {
                    log.error("Error cloning Cinder disk '{}': {}", disk.getDiskAlias());
                    getReturnValue().setFault(vdcReturnValueBase.getFault());
                    isSucceeded = false;
                    break;
                }
                Guid imageId = vdcReturnValueBase.getActionReturnValue();
                diskImageMap.put(disk.getId(), imageId);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error cloning Cinder disk '{}': {}", disk.getDiskAlias(), e.getMessage());
                isSucceeded = false;
            }

        }
        getReturnValue().setActionReturnValue(diskImageMap);
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(isSucceeded);
    }

    private ImagesContainterParametersBase buildChildCommandParameters(CinderDisk cinderDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(cinderDisk.getImageId());
        DiskImage templateDisk = getParameters().getDisksMap().get(cinderDisk.getId());
        createParams.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setVmSnapshotId(getParameters().getVmSnapshotId());
        return createParams;
    }

    private void revertCinderDisks() {
        for (VdcActionParametersBase childParams : getParameters().getImagesParameters()) {
            ImagesContainterParametersBase commandParameters =
                    (ImagesContainterParametersBase) childParams;
            Guid destinationImageId = commandParameters.getDestinationImageId();
            Guid storageDomainId = commandParameters.getStorageDomainId();
            removeCinderDisk(destinationImageId, storageDomainId);
        }
    }

    private void removeCinderDisk(Guid cinderDiskId, Guid storageDomainId) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveCinderDisk,
                buildRevertParameters(cinderDiskId),
                null,
                new SubjectEntity(VdcObjectType.Storage, storageDomainId));
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fail to revert disk id '{}'.", cinderDiskId);
            log.error("Exception: ", e);
        }
    }

    private RemoveCinderDiskParameters buildRevertParameters(Guid cinderDiskId) {
        RemoveCinderDiskParameters removeDiskParams = new RemoveCinderDiskParameters(cinderDiskId);
        removeDiskParams.setLockVM(false);
        removeDiskParams.setShouldBeLogged(false);
        return removeDiskParams;
    }

    @Override
    protected void endWithFailure() {
        endActionOnDisks();
        revertCinderDisks();
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        endActionOnDisks();
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return new CloneCinderDisksCommandCallback();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    protected List<VdcReturnValueBase> endActionOnDisks() {
        List<VdcReturnValueBase> returnValues = new ArrayList<>();
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            VdcReturnValueBase returnValue = getBackend().endAction(
                    p.getCommandType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
            returnValues.add(returnValue);
        }
        return returnValues;
    }
}
