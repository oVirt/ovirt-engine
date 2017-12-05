package org.ovirt.engine.core.bll.validator;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

/** A Validator for various VM validate needs */
public class VmValidator {
    private VM vm;

    public VmValidator(VM vm) {
        this.vm = vm;
    }

    /** @return Validation result that indicates if the VM is during migration or not. */
    public ValidationResult vmNotDuringMigration() {
        if (vm.getStatus() == VMStatus.MigratingFrom || vm.getStatus() == VMStatus.MigratingTo) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is down or not. */
    public ValidationResult vmDown() {
        if (!vm.isDown()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is qualified to have its snapshots merged. */
    public ValidationResult vmQualifiedForSnapshotMerge() {
        if (!vm.isQualifiedForSnapshotMerge()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP,
                    String.format("$VmName %s", vm.getName()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotLocked() {
        if (vm.getStatus() == VMStatus.ImageLocked) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotSavingRestoring() {
        if (vm.getStatus().isHibernating() || vm.getStatus() == VMStatus.RestoringState) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateVmStatusUsingMatrix(ActionType actionType) {
        if (!ActionUtils.canExecute(Arrays.asList(vm), VM.class,
                actionType)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL,
                    LocalizedVmStatus.from(vm.getStatus()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotIlegal() {
        if (vm.getStatus() == VMStatus.ImageIllegal) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmWithoutLocalDiskUserProperty() {
        Map<String, String>
                properties = VmPropertiesUtils.getInstance().getVMProperties(
                vm.getCompatibilityVersion(),
                vm.getStaticData());
        if (properties.containsKey("localdisk")) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_WITH_LOCALDISK_USER_PROPERTY);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotRunningStateless() {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            EngineMessage message = vm.isRunning() ? EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS :
                    EngineMessage.ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER;
            return new ValidationResult(message);
        }

        return ValidationResult.VALID;
    }

    /**
     * @return ValidationResult indicating whether snapshots of disks are attached to other vms.
     */
    public ValidationResult vmNotHavingDeviceSnapshotsAttachedToOtherVms(boolean onlyPlugged) {
        List<Disk> vmDisks = getDbFacade().getDiskDao().getAllForVm(vm.getId());
        ValidationResult result =
                new DiskImagesValidator(DisksFilter.filterImageDisks(vmDisks, ONLY_NOT_SHAREABLE, ONLY_ACTIVE))
                        .diskImagesSnapshotsNotAttachedToOtherVms(onlyPlugged);
        if (result != ValidationResult.VALID) {
            return result;
        }

        return ValidationResult.VALID;
    }

    /**
     * Determines whether VirtIO-SCSI can be disabled for the VM
     * (can be disabled when no disk uses VirtIO-SCSI interface).
     */
    public ValidationResult canDisableVirtioScsi(Collection<? extends Disk> vmDisks) {
        if (vmDisks == null) {
            vmDisks = getDiskDao().getAllForVm(vm.getId(), true);
            populateDisksWithVmData(vmDisks, vm.getId());
        }

        boolean isVirtioScsiDiskExist =
                vmDisks.stream().anyMatch(d -> d.getDiskVmElementForVm(vm.getId()).getDiskInterface() == DiskInterface.VirtIO_SCSI);

        if (isVirtioScsiDiskExist) {
            return new ValidationResult(EngineMessage.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);
        }

        return ValidationResult.VALID;
    }

    private void populateDisksWithVmData(Collection<? extends Disk> disks, Guid vmId) {
        for (Disk disk : disks) {
            DiskVmElement dve = getDiskVmElementDao().get(new VmDeviceId(disk.getId(), vmId));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }
    }

    public DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    public DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * @return ValidationResult indicating whether a vm contains non-migratable, plugged, passthrough vnics
     */
    public ValidationResult allPassthroughVnicsMigratable() {
        List<VmNetworkInterface> vnics =
                getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId());

        List<String> nonMigratablePassthroughVnicNames = vnics.stream()
                .filter(isVnicMigratable(vm).negate())
                .map(VmNic::getName)
                .collect(Collectors.toList());

        if (!nonMigratablePassthroughVnicNames.isEmpty()) {
            Collection<String> replacements =
                    ReplacementUtils.replaceWith("interfaces", nonMigratablePassthroughVnicNames);
            replacements.add(String.format("$vmName %s", vm.getName()));
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_MIGRATION_OF_NON_MIGRATABLE_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED,
                    replacements);
        }
        return ValidationResult.VALID;
    }

    private Predicate<? super VmNetworkInterface> isVnicMigratable(VM vm) {
        return vnic -> !vnic.isPassthrough() || !vnic.isPlugged()
                || (FeatureSupported.sriovHotPlugSupported(vm.getClusterCompatibilityVersion())
                        && getVnicProfile(vnic).isMigratable());
    }

    private VnicProfile getVnicProfile(VmNic vnic) {
        VnicProfile profile = getDbFacade().getVnicProfileDao().get(vnic.getVnicProfileId());
        return profile;
    }

    /**
     * Checks whether VM uses lun with scsi reservation true.
     * @return If scsi lun with scsi reservation is plugged to VM
     */
    public ValidationResult isVmPluggedDiskNotUsingScsiReservation() {
        List<DiskVmElement> dves = getDbFacade().getDiskVmElementDao().getAllPluggedToVm(vm.getId());
        if (dves.stream().anyMatch(dve -> dve.isUsingScsiReservation())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION,
                    String.format("$VmName %s", vm.getName()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult vmNotHavingPciPassthroughDevices() {
        if (getHostDeviceManager().checkVmNeedsPciDevices(vm.getId())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES);
        }
        return ValidationResult.VALID;
    }

    private HostDeviceManager getHostDeviceManager() {
        return Injector.get(HostDeviceManager.class);
    }

    public ValidationResult isPinnedVmRunningOnDedicatedHost(VM recentVm, VmStatic paramVm){
        boolean isPinned = paramVm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST;
        Guid vdsId = recentVm.getRunOnVds();
        List<Guid> hostList = paramVm.getDedicatedVmForVdsList();

        // If hostList is empty -> all hosts are allowed
        if (isPinned && vdsId != null && !hostList.isEmpty() && !hostList.contains(vdsId)){
            // VM is NOT running on a dedicated host
            // fail with error message
            String hostName = String.format("$hostName %1$s", recentVm.getRunOnVdsName());
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PINNED_VM_NOT_RUNNING_ON_DEDICATED_HOST, hostName, hostName);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isVmExists() {
        if (vm == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        return ValidationResult.VALID;
    }

    /**
     * This method checks that with the given parameters, the max PCI and IDE limits defined are not passed.
     */
    public static ValidationResult checkPciAndIdeLimit(
            int osId,
            Version clusterVersion,
            int monitorsNumber,
            List<? extends VmNic> interfaces,
            List<DiskVmElement> diskVmElements,
            boolean virtioScsiEnabled,
            boolean hasWatchdog,
            boolean isBalloonEnabled,
            boolean isSoundDeviceEnabled) {

        // this adds: monitors + 2 * (interfaces with type rtl_pv) + (all other
        // interfaces) + (all disks that are not IDE)
        int pciInUse = monitorsNumber;

        for (VmNic a : interfaces) {
            if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.rtl8139_pv) {
                pciInUse += 2;
            } else if (a.getType() != null && VmInterfaceType.forValue(a.getType()) == VmInterfaceType.spaprVlan) {
                // Do not count sPAPR VLAN devices since they are not PCI
            } else {
                pciInUse += 1;
            }
        }

        pciInUse += diskVmElements.stream().filter(dve -> dve.getDiskInterface() == DiskInterface.VirtIO).count();

        // VirtIO SCSI controller requires one PCI slot
        pciInUse += virtioScsiEnabled ? 1 : 0;

        // VmWatchdog controller requires one PCI slot
        pciInUse += hasWatchdog ? 1 : 0;

        // Balloon controller requires one PCI slot
        pciInUse += isBalloonEnabled ? 1 : 0;

        // Sound device controller requires one PCI slot
        pciInUse += isSoundDeviceEnabled ? 1 : 0;

        OsRepository osRepository = Injector.get(OsRepository.class);

        int maxPciSlots = osRepository.getMaxPciDevices(osId, clusterVersion);

        ArrayList<EngineMessage> messages = new ArrayList<>();
        if (pciInUse > maxPciSlots) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_PCI_SLOTS);
        }
        else if (VmCommand.MAX_IDE_SLOTS < diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.IDE).count()) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_IDE_SLOTS);
        }
        else if (VmCommand.MAX_VIRTIO_SCSI_DISKS <
                diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.VirtIO_SCSI).count()) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_VIRTIO_SCSI_DISKS);
        }
        else if (VmCommand.MAX_SPAPR_SCSI_DISKS <
                diskVmElements.stream().filter(a -> a.getDiskInterface() == DiskInterface.SPAPR_VSCSI).count()) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_EXCEEDED_MAX_SPAPR_VSCSI_DISKS);
        }

        if (!messages.isEmpty()) {
           return new ValidationResult(messages);
        }
        return ValidationResult.VALID;
    }

    public static ValidationResult validateCpuSockets(VmBase vmBase, Version compatibilityVersion) {
        int num_of_sockets = vmBase.getNumOfSockets();
        int cpu_per_socket = vmBase.getCpuPerSocket();
        int threadsPerCpu = vmBase.getThreadsPerCpu();

        String version = compatibilityVersion.toString();

        if ((num_of_sockets * cpu_per_socket * threadsPerCpu) >
                Config.<Integer> getValue(ConfigValues.MaxNumOfVmCpus, version)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_CPU);
        }
        if (num_of_sockets > Config.<Integer> getValue(ConfigValues.MaxNumOfVmSockets, version)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_SOCKETS);
        }
        if (cpu_per_socket > Config.<Integer> getValue(ConfigValues.MaxNumOfCpuPerSocket, version)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET);
        }
        if (threadsPerCpu > Config.<Integer> getValue(ConfigValues.MaxNumOfThreadsPerCpu, version)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MAX_THREADS_PER_CPU);
        }
        if (cpu_per_socket < 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET);
        }
        if (num_of_sockets < 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIN_NUM_SOCKETS);
        }
        if (threadsPerCpu < 1) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIN_THREADS_PER_CPU);
        }
        return ValidationResult.VALID;
    }

    public DiskVmElementDao getDiskVmElementDao() {
        return DbFacade.getInstance().getDiskVmElementDao();
    }

}
