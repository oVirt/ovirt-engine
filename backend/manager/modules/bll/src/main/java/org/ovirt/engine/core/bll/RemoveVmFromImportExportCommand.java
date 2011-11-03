package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmFromImportExportCommand<T extends RemoveVmFromImportExportParamenters> extends RemoveVmCommand<T> {
    public RemoveVmFromImportExportCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        parameters.setEntityId(parameters.getVmId());
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        storage_domains storage = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                getParameters().getStorageDomainId(), getParameters().getStoragePoolId());
        if (storage == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        if (storage.getstatus() == null || storage.getstatus() != StorageDomainStatus.Active) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            return false;
        }

        if (storage.getstorage_domain_type() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            return false;
        }

        GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(
                getParameters().getStoragePoolId(), getParameters().getStorageDomainId());
        tempVar.setGetAll(true);
        tempVar.setIds(new java.util.ArrayList<Guid>(java.util.Arrays.asList(new Guid[] { getVmId() })));
        VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(
                VdcQueryType.GetVmsFromExportDomain, tempVar);
        if (!qretVal.getSucceeded() || qretVal.getReturnValue() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN);
            return false;
        }

        java.util.ArrayList<VM> vms = (java.util.ArrayList<VM>) qretVal.getReturnValue();
        if (vms.size() != 1) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN);
            return false;
        }

        setVm(vms.get(0));

        VM vm = getVmDAO().getById(vms.get(0).getvm_guid());
        if (vm != null && vm.getstatus() == VMStatus.ImageLocked) {
            if(AsyncTaskManager.getInstance().hasTasksForEntityIdAndAction(vm.getvm_guid(), VdcActionType.ExportVm)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_DURING_EXPORT);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void ExecuteVmCommand() {
        RemoveVmInSpm(getParameters().getStoragePoolId(), getVmId(), getParameters().getStorageDomainId());
        java.util.ArrayList<DiskImage> images = new java.util.ArrayList<DiskImage>(getVm().getDiskMap().values());
        for (DiskImage image : images) {
            image.setstorage_id(getParameters().getStorageDomainId());
            image.setstorage_pool_id(getParameters().getStoragePoolId());
        }
        RemoveVmImages(images);

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_VM : AuditLogType.IMPORTEXPORT_REMOVE_VM_FAILED;
    }

    @Override
    protected void EndVmCommand() {
        setCommandShouldBeLogged(false);

        setSucceeded(true);
    }
}
