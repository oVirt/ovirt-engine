package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RemoveVmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryImageRemoverFromExportDomain;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
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
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@NonTransactiveCommandAttribute
public class RemoveVmFromImportExportCommand<T extends RemoveVmFromImportExportParameters> extends RemoveVmCommand<T>{

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
                        getVmIsBeingRemovedFromExportDomainMessage()));
    }

    private String getVmIsBeingRemovedFromExportDomainMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_REMOVED_FROM_EXPORT_DOMAIN.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    @Override
    protected boolean validate() {
        StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
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
        VM vm = getVmDao().get(getVmId());
        if (vm != null && vm.getStatus() == VMStatus.ImageLocked) {
            if (CommandCoordinatorUtil.hasTasksForEntityIdAndAction(vm.getId(), VdcActionType.ExportVm)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_DURING_EXPORT);
            }
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        removeVmInSpm();
        removeDiskImages();
        removeMemoryImages();

        setSucceeded(true);
    }

    private void removeDiskImages() {
        List<DiskImage> images =
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false, true);
        for (DiskImage image : images) {
            image.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
            image.setStoragePoolId(getParameters().getStoragePoolId());
        }
        removeVmImages(images);
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

    private void removeMemoryImages() {
         new MemoryImageRemoverFromExportDomain(getVm(), this,
                 getParameters().getStoragePoolId(), getParameters().getStorageDomainId())
         .remove();
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
            VdcQueryReturnValue qretVal = runInternalQuery(
                    VdcQueryType.GetVmsFromExportDomain, tempVar);

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
            jobProperties = new HashMap<>();
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
