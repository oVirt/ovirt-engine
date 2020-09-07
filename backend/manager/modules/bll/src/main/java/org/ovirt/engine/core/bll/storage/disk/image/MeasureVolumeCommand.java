package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.MeasureVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class MeasureVolumeCommand<T extends MeasureVolumeParameters> extends CommandBase<T> {
    @Inject
    private VmDao vmDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public MeasureVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public MeasureVolumeCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        Map<Boolean, List<VM>> vms = vmDao.getForDisk(getParameters().getImageGroupId(), true);
        DiskImage diskImage = diskImageDao.getSnapshotById(getParameters().getImageId());

        // We cannot measure active images used by VMs
        if (isAttachedToRunningVMs(vms) && diskImage.getActive() && !diskImage.getStorageTypes().get(0).isBlockDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IMAGE_CANNOT_BE_MEASURED_WHILE_USED,
                    List.of(ReplacementUtils.createSetVariableString("imageId", getParameters().getImageId())));
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        Map<Boolean, List<VM>> vms = vmDao.getForDisk(getParameters().getImageGroupId(), true);

        // We want to prepare only if the SD is block and the disk is not plugged
        if ((vms.isEmpty() ||
                !isAttachedToRunningVMs(vms)) &&
                storageDomainDao.get(getParameters().getStorageDomainId()).getStorageType().isBlockDomain()) {
            if (!executeWithHost(getParameters().getVdsRunningOn(),
                    host -> imagesHandler.prepareImage(getParameters().getStoragePoolId(),
                    getParameters().getStorageDomainId(),
                    getParameters().getImageGroupId(),
                    getParameters().getImageId(),
                    host).getSucceeded())) {
                log.error("Failed to prepare image '{}/{}'",
                        getParameters().getImageGroupId(),
                        getParameters().getImageId());
                return;
            }

            getParameters().setShouldTeardown(true);
        }

        MeasureVolumeVDSCommandParameters params =
                new MeasureVolumeVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupId(),
                        getParameters().getImageId(),
                        getParameters().getDstVolFormat());
        params.setWithBacking(getParameters().isWithBacking());
        params.setVdsId(getParameters().getVdsRunningOn());

        long volumeSize = (long) vdsCommandsHelper.runVdsCommandWithoutFailover(
                VDSCommandType.MeasureVolume,
                params,
                getParameters().getStoragePoolId(),
                null)
                .getReturnValue();

        persistCommandIfNeeded();
        setActionReturnValue(volumeSize);
        setSucceeded(true);
    }

    private boolean isAttachedToRunningVMs(Map<Boolean, List<VM>> vms) {
        return !vms.isEmpty() && vms.computeIfAbsent(Boolean.TRUE, b -> new ArrayList<>())
                .stream()
                .anyMatch(VM::isRunning);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return new ArrayList<>();
    }

    @Override
    protected void endSuccessfully() {
        getReturnValue().setEndActionTryAgain(false);
        if (getParameters().isShouldTeardown()) {
            Guid hostForExecution = vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId());

            if (!executeWithHost(hostForExecution,
                    host -> imagesHandler.teardownImage(
                            getParameters().getStoragePoolId(),
                            getParameters().getStorageDomainId(),
                            getParameters().getImageGroupId(),
                            getParameters().getImageId(),
                            host).getSucceeded())) {
                log.warn("Failed to tear down image '{}/{}'",
                        getParameters().getImageGroupId(),
                        getParameters().getImageId());
            }
        }

        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endWithFailure() {
        endSuccessfully();
    }

    private boolean executeWithHost(Guid hostId, Function<Guid, Boolean> func) {
        if (hostId == null) {
            log.error("Cannot find a host to execute command on");
            return false;
        }

        try {
            func.apply(hostId);
        } catch (Exception e) {
            log.error("Failed to execute", e);
            return false;
        }

        return true;
    }
}
