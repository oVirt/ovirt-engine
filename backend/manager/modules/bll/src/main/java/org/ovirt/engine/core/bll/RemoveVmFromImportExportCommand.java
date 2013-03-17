package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@NonTransactiveCommandAttribute
public class RemoveVmFromImportExportCommand<T extends RemoveVmFromImportExportParamenters> extends RemoveVmCommand<T> {

    // this is needed since overriding getVmTemplate()
    private VM exportVm;

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
        StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                getParameters().getStorageDomainId(), getParameters().getStoragePoolId());
        if (storage == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        if (storage.getStatus() == null || storage.getStatus() != StorageDomainStatus.Active) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            return false;
        }

        if (storage.getStorageDomainType() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            return false;
        }

        // getVm() is the vm from the export domain
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN);
            return false;
        }

        // not using getVm() since its overridden to get vm from export domain
        VM vm = getVmDAO().get(getVmId());
        if (vm != null && vm.getStatus() == VMStatus.ImageLocked) {
            if (AsyncTaskManager.getInstance().hasTasksForEntityIdAndAction(vm.getId(), VdcActionType.ExportVm)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_DURING_EXPORT);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void executeVmCommand() {
        removeVmInSpm(getParameters().getStoragePoolId(), getVmId(), getParameters().getStorageDomainId());
        List<DiskImage> images =
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false);
        for (DiskImage image : images) {
            image.setStorageIds(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
            image.setStoragePoolId(getParameters().getStoragePoolId());
        }
        removeVmImages(images);

        setSucceeded(true);
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
            tempVar.setIds(new ArrayList<Guid>(Collections.singletonList(getVmId())));
            VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetVmsFromExportDomain, tempVar);

            if (qretVal != null && qretVal.getSucceeded()) {
                @SuppressWarnings("unchecked")
                ArrayList<VM> vms = (ArrayList<VM>) qretVal.getReturnValue();
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
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
        }
        return jobProperties;
    }
}
