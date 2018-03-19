package org.ovirt.engine.core.bll.exportimport;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RemoveVmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

@NonTransactiveCommandAttribute
public class RemoveVmFromImportExportCommand<T extends RemoveVmFromImportExportParameters> extends RemoveVmCommand<T>{


    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    // this is needed since overriding getVmTemplate()
    private VM exportVm;

    public RemoveVmFromImportExportCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getVmId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, parameters.getVmId()));
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.REMOTE_VM,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_REMOVED_FROM_EXPORT_DOMAIN)
                                .withOptional("VmName", getVmName())));
    }

    @Override
    protected boolean validate() {
        StorageDomain storage = storageDomainDao.getForStoragePool(
                getParameters().getStorageDomainId(), getParameters().getStoragePoolId());
        if (storage == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (storage.getStatus() == null || storage.getStatus() != StorageDomainStatus.Active) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }

        if (storage.getStorageDomainType() != StorageDomainType.ImportExport) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        // getVm() is the vm from the export domain
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN);
        }

        // not using getVm() since its overridden to get vm from export domain
        VM vm = vmDao.get(getVmId());
        if (vm != null && vm.getStatus() == VMStatus.ImageLocked) {
            if (commandCoordinatorUtil.hasTasksForEntityIdAndAction(vm.getId(), ActionType.ExportVm)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_DURING_EXPORT);
            }
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        removeVmInSpm();
        removeDiskImages();

        setSucceeded(true);
    }

    private void removeDiskImages() {
        List<DiskImage> images =
                DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
        boolean shouldWipe = false;
        for (DiskImage image : images) {
            image.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
            image.setStoragePoolId(getParameters().getStoragePoolId());
            shouldWipe |= image.isWipeAfterDelete();
        }

        Set<Guid> allMemoryDisks = MemoryUtils.getMemoryDiskIdsFromSnapshots(getVm().getSnapshots());
        for (Guid memoryDiskId : allMemoryDisks) {
            DiskImage metadataDisk = createMemoryDisk(memoryDiskId, shouldWipe);
            images.add(metadataDisk);
        }

        removeVmImages(images);
    }

    private DiskImage createMemoryDisk(Guid diskId, boolean shouldWipe) {
        DiskImage disk = new DiskImage();
        disk.setId(diskId);
        // The image ID is meaningless when removing a memory disk but has to be set to a distinguished
        // value due to how the remove command works
        disk.setImageId(Guid.newGuid());
        disk.setStoragePoolId(getParameters().getStoragePoolId());
        disk.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
        disk.setWipeAfterDelete(shouldWipe);
        disk.setActive(true);
        return disk;
    }

    private boolean removeVmInSpm() {
        return runVdsCommand(VDSCommandType.RemoveVM, buildRemoveVmParameters())
                .getSucceeded();
    }

    private RemoveVMVDSCommandParameters buildRemoveVmParameters() {
        return new RemoveVMVDSCommandParameters(
                getParameters().getStoragePoolId(),
                getVmId(),
                getParameters().getStorageDomainId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_VM : AuditLogType.IMPORTEXPORT_REMOVE_VM_FAILED;
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);

        setSucceeded(true);
    }

    /*
     * get vm from export domain
     */
    @Override
    public VM getVm() {
        if (exportVm == null) {
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(
                    getParameters().getStoragePoolId(), getParameters().getStorageDomainId());
            tempVar.setIds(new ArrayList<>(Collections.singletonList(getVmId())));
            QueryReturnValue qretVal = runInternalQuery(
                    QueryType.GetVmsFromExportDomain, tempVar);

            if (qretVal != null && qretVal.getSucceeded()) {
                ArrayList<VM> vms = qretVal.getReturnValue();
                if (!vms.isEmpty()) {
                    exportVm = vms.get(0);
                    setVm(exportVm);
                }
            }
        }
        return exportVm;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
        }
        return jobProperties;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(
                getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
