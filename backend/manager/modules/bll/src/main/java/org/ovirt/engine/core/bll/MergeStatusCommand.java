package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ReconcileVolumeChainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FullListVdsCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class MergeStatusCommand<T extends MergeParameters>
        extends CommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeStatusCommand.class);

    public MergeStatusCommand(T parameters) {
        super(parameters);
    }

    public MergeStatusCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        attemptResolution();
    }

    public void attemptResolution() {
        Set<Guid> images;
        if (getVmDAO().get(getParameters().getVmId()).isDown()) {
            StoragePool pool = getStoragePoolDAO().get(getParameters().getStoragePoolId());
            if (pool.getSpmVdsId() == null || pool.getStatus() != StoragePoolStatus.Up) {
                log.info("VM down, waiting on SPM election to resolve Live Merge");
                setSucceeded(true);
                return;
            } else {
                log.error("VM is not running, proceeding with Live Merge recovery");
                images = getVolumeChainFromRecovery();
            }
        } else {
            images = getVolumeChain();
        }
        if (images == null) {
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        Set<Guid> imagesToRemove = getImagesToRemove();
        images.retainAll(imagesToRemove);
        if (images.size() != 1) {
            log.error("Failed to live merge, still in volume chain: {}", images);
            setCommandStatus(CommandStatus.FAILED);
            return;
        }

        imagesToRemove.removeAll(images);
        log.info("Successfully removed volume(s): {}", imagesToRemove);

        // Direction: base exists => backwards merge (commit); else (top exists) => forward merge (rebase)
        VmBlockJobType jobType = (images.contains(getParameters().getBaseImage().getImageId()))
                ? VmBlockJobType.COMMIT : VmBlockJobType.PULL;
        log.info("Volume merge type '{}'", jobType.name());

        MergeStatusReturnValue returnValue = new MergeStatusReturnValue(jobType, imagesToRemove);
        getReturnValue().setActionReturnValue(returnValue);
        setSucceeded(true);
        persistCommand(getParameters().getParentCommand(), true);
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    private Set<Guid> getVolumeChain() {
        List<String> vmIds = new ArrayList<>();
        vmIds.add(getParameters().getVmId().toString());
        VDS vds = getVdsDAO().get(getParameters().getVdsId());
        Map[] vms = (Map[]) (new FullListVdsCommand<FullListVDSCommandParameters>(
                new FullListVDSCommandParameters(vds, vmIds)).executeWithReturnValue());

        if (vms == null || vms.length == 0) {
            log.error("Failed to retrieve VM information");
            return null;
        }

        Map vm = (Map) vms[0];
        if (vm == null || vm.get(VdsProperties.vm_guid) == null) {
            log.error("Received incomplete VM information");
            return null;
        }

        Guid vmId = new Guid((String) vm.get(VdsProperties.vm_guid));
        if (!vmId.equals(getParameters().getVmId())) {
            log.error("Invalid VM returned when querying status: expected '{}', got '{}'",
                    getParameters().getVmId(), vmId);
            return null;
        }

        Set<Guid> images = new HashSet<>();
        DiskImage activeDiskImage = getParameters().getActiveImage();
        for (Object o : (Object[]) vm.get(VdsProperties.Devices)) {
            Map device = (Map<String, Object>) o;
            if (VdsProperties.Disk.equals(device.get(VdsProperties.Type))
                    && activeDiskImage.getId().equals(Guid.createGuidFromString(
                    (String) device.get(VdsProperties.ImageId)))) {
                Object[] volumeChain = (Object[]) device.get("volumeChain");
                for (Object v : volumeChain) {
                    Map<String, Object> volume = (Map<String, Object>) v;
                    images.add(Guid.createGuidFromString((String) volume.get(VdsProperties.VolumeId)));
                }
                break;
            }
        }
        return images;
    }

    private Set<Guid> getVolumeChainFromRecovery() {
        ReconcileVolumeChainVDSCommandParameters parameters =
                new ReconcileVolumeChainVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupId(),
                        getParameters().getImageId()
                );

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ReconcileVolumeChain,
                parameters);
        if (!vdsReturnValue.getSucceeded()) {
            log.error("Unable to retrieve volume list during Live Merge recovery");
            return null;
        }
        return new HashSet<>((List<Guid>) vdsReturnValue.getReturnValue());
    }

    /**
     * Returns the set of images which may be merged/removed in the live merge operation
     * on this disk.  We don't know whether VDSM will choose a forward or backward merge
     * so we return both the top and bottom images here; the caller will need to figure
     * out the merge direction and preserve the appropriate image.
     *
     * @return  Set of Guids representing the images which may be removed
     */
    public Set<Guid> getImagesToRemove() {
        Set<Guid> imagesToRemove = new HashSet<>();
        DiskImage curr = getParameters().getTopImage();
        imagesToRemove.add(curr.getImageId());
        while (!curr.getParentId().equals(getParameters().getBaseImage().getParentId())) {
            curr = getDbFacade().getDiskImageDao().getSnapshotById(curr.getParentId());
            imagesToRemove.add(curr.getImageId());
        }
        return imagesToRemove;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    public CommandCallback getCallback() {
        return new MergeStatusCommandCallback();
    }
}
