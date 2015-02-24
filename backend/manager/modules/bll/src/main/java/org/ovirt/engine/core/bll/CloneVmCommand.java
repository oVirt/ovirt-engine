package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class CloneVmCommand<T extends CloneVmParameters> extends AddVmAndCloneImageCommand<T> {

    private Collection<DiskImage> diskImagesFromConfiguration;

    private Guid oldVmId;

    private VM vm;

    private VM sourceVm;

    public CloneVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void postConstruct() {
        super.postConstruct();
        oldVmId = getParameters().getVmId();
        setVmName(getParameters().getNewName());

        // init the parameters only at first instantiation (not subsequent for end action)
        if (Guid.isNullOrEmpty(getParameters().getNewVmGuid())) {
            setupParameters();
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.error("Can not found any default active domain for one of the disks of snapshot with id '{}'",
                oldVmId);
    }

    @Override
    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return getVm().getStoragePoolId();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();

        for (DiskImage image: getImagesToCheckDestinationStorageDomains()) {
            locks.put(image.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskSharedLockMessage()));
        }

        locks.put(getSourceVmFromDb().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_BEING_CLONED));

        return locks;
    }

    @Override
    protected Collection<DiskImage> getAdjustedDiskImagesFromConfiguration() {
        VdcQueryReturnValue vdcReturnValue = runInternalQuery(
                VdcQueryType.GetAllDisksByVmId,
                new IdQueryParameters(oldVmId));

        List<Disk> loadedImages = vdcReturnValue.getReturnValue() != null ? (List<Disk>) vdcReturnValue.getReturnValue() : new ArrayList<Disk>();

        if (diskImagesFromConfiguration == null) {
            diskImagesFromConfiguration =
                    ImagesHandler.filterImageDisks(loadedImages,
                            false,
                            true,
                            true);
        }
        return diskImagesFromConfiguration;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    StringUtils.defaultString(getParameters().getNewName()));
        }
        return jobProperties;
    }

    @Override
    protected Guid getSourceVmId() {
        return oldVmId;
    }

    protected VM getVmFromConfiguration() {
        return getVm();
    }

    protected VM getSourceVmFromDb() {
        if (sourceVm == null) {
            sourceVm = getVmDAO().get(oldVmId);
        }

        return sourceVm;
    }

    @Override
    public VM getVm() {
        if (vm == null) {
            vm = getVmDAO().get(oldVmId);
            VmDeviceUtils.setVmDevices(vm.getStaticData());
            VmHandler.updateDisksFromDb(vm);
            VmHandler.updateVmGuestAgentVersion(vm);
            VmHandler.updateNetworkInterfacesFromDb(vm);
            VmHandler.updateVmInitFromDB(vm.getStaticData(), true);

            vm.setName(getParameters().getNewName());
            vm.setId(getVmId());
        }

        return vm;
    }

    private void fillDisksToParameters() {

        for (Disk image : getAdjustedDiskImagesFromConfiguration()) {
                diskInfoDestinationMap.put(image.getId(), (DiskImage) image);
        }

        getParameters().setDiskInfoDestinationMap(diskInfoDestinationMap);
    }

    @Override
    protected void removeVmRelatedEntitiesFromDb() {
        detachDisks();
        super.removeVmRelatedEntitiesFromDb();
    }

    @Override
    protected boolean addVmImages() {
        if (super.addVmImages()) {
            attachDisks();
            return true;
        }

        return false;
    }

    private void detachDisks() {
        attachDetachDisks(VdcActionType.DetachDiskFromVm);
    }

    private void attachDisks() {
        attachDetachDisks(VdcActionType.AttachDiskToVm);
    }

    private void attachDetachDisks(VdcActionType actionType) {
        VdcQueryReturnValue vdcReturnValue = runInternalQuery(
                VdcQueryType.GetAllDisksByVmId,
                new IdQueryParameters(oldVmId));

        List<Disk> loadedImages = vdcReturnValue.getReturnValue() != null ? (List<Disk>) vdcReturnValue.getReturnValue() : new ArrayList<Disk>();

        for (Disk disk : loadedImages) {
            if (disk.getDiskStorageType() == Disk.DiskStorageType.LUN || disk.isShareable()) {
                attachDetachDisk(disk, actionType);
            }
        }
    }

    private void attachDetachDisk(Disk disk, VdcActionType actionType) {
        runInternalAction(
                actionType,
                new AttachDetachVmDiskParameters(
                        getParameters().getNewVmGuid(),
                        disk.getId(),
                        disk.getPlugged(),
                        disk.getReadOnly()
                )
        );
    }

    private void setupParameters() {
        setVmId(Guid.newGuid());
        VM vmToClone = getVm();
        getParameters().setNewVmGuid(getVmId());
        getParameters().setVm(vmToClone);

        List<VmDevice> devices = getVmDeviceDao().getVmDeviceByVmId(oldVmId);
        getParameters().setSoundDeviceEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.SOUND));
        getParameters().setConsoleEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.CONSOLE));
        getParameters().setVirtioScsiEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI));
        getParameters().setBalloonEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.BALLOON));
        setGraphicsDevices(devices);

        VdcQueryReturnValue watchdogs = runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(oldVmId));
        if (!((List<VmWatchdog>) watchdogs.getReturnValue()).isEmpty()) {
            VmWatchdog watchdog = ((List<VmWatchdog>) watchdogs.getReturnValue()).iterator().next();
            getParameters().setUpdateWatchdog(true);
            getParameters().setWatchdog(watchdog);
        }


        fillDisksToParameters();
    }

    private void setGraphicsDevices(List<VmDevice> devices) {
        for (GraphicsType graphicsType : GraphicsType.values()) {
            getParameters().getGraphicsDevices().put(graphicsType, null); // prevent copying from the template
        }

        for (VmDevice device : devices) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                GraphicsDevice graphicsDevice = new GraphicsDevice(device);
                getParameters().getGraphicsDevices().put(graphicsDevice.getGraphicsType(), graphicsDevice);
            }
        }
    }

    private boolean containsDeviceWithType(List<VmDevice> devices, VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        for (VmDevice device : devices) {
            if (device.getType() == generalType) {
                if (deviceType == null || (deviceType.getName() != null && deviceType.getName().equals(device.getDevice()))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsDeviceWithType(List<VmDevice> devices, VmDeviceGeneralType type) {
        return containsDeviceWithType(devices, type, null);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));

        return permissionList;
    }

    @Override
    protected boolean canDoAction() {
        if (!(getSourceVmFromDb().getStatus() == VMStatus.Suspended || getSourceVmFromDb().isDown())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        return super.canDoAction();
    }

    @Override
    protected void updateOriginalTemplate(VmStatic vmStatic) {
        vmStatic.setOriginalTemplateGuid(getVm().getOriginalTemplateGuid());
        vmStatic.setOriginalTemplateName(getVm().getOriginalTemplateName());
    }
}
