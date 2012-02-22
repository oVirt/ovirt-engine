package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class RemoveVmTemplateFromImportExportCommand<T extends VmTemplateImportExportParameters> extends
        RemoveVmTemplateCommand<T> {
    public RemoveVmTemplateFromImportExportCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;
        if (retVal) {
            GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                    .getStoragePoolId(), getParameters().getStorageDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            retVal = qretVal.getSucceeded();

            if (retVal) {
                Map<VmTemplate, DiskImageList> templates = (Map) qretVal.getReturnValue();
                // java.util.ArrayList<DiskImage> images = null; //LINQ
                // templates.FirstOrDefault(t => t.Key.vmt_guid ==
                // VmTemplateImportExportParameters.VmTemplateId).Value;
                DiskImageList images = templates.get(LinqUtils.firstOrNull(templates.keySet(),
                        new Predicate<VmTemplate>() {
                            @Override
                            public boolean eval(VmTemplate t) {
                                return t.getId().equals(getParameters().getVmTemplateId());
                            }
                        }));
                // setVmTemplate(null); //LINQ templates.FirstOrDefault(t =>
                // t.Key.vmt_guid ==
                // VmTemplateImportExportParameters.VmTemplateId).Key;
                VmTemplate template = LinqUtils.firstOrNull(templates.keySet(), new Predicate<VmTemplate>() {
                    @Override
                    public boolean eval(VmTemplate t) {
                        return t.getId().equals(getParameters().getVmTemplateId());
                    }
                });
                setVmTemplate(template);
                if (images != null) {
                    getParameters().setImages(images);
                }
                retVal = !(images == null);
            }
        }
        if (getVmTemplate() == null) {
            retVal = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        if (getParameters().getImages() == null || getParameters().getImages().size() <= 0) {
            addCanDoActionMessage(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST);
            retVal = false;
        }
        if (retVal) {
            retVal = StorageDomainCommandBase.IsDomainActive(getParameters().getStorageDomainId(), getParameters()
                    .getStoragePoolId(), getReturnValue().getCanDoActionMessages());
        }
        if (retVal) {
            storage_domains storage = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                    getParameters().getStorageDomainId(), getParameters().getStoragePoolId());
            if (storage.getstorage_domain_type() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                return false;
            }
        }
        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
        }
        if (retVal) {
            // we fectch from db and not using VmTmplate property becase
            // VmTemplate is the one from export domain and not from database
            VmTemplate tmpl = DbFacade.getInstance().getVmTemplateDAO().get(getVmTemplateId());
            if (tmpl != null) {
                retVal = (tmpl.getstatus() != VmTemplateStatus.Locked);
                if (!retVal) {
                    getReturnValue().getCanDoActionMessages()
                            .add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString());
                }
            }
        }
        return retVal;
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityId(getVmTemplateId());
        RemoveVMVDSCommandParameters tempVar = new RemoveVMVDSCommandParameters(getParameters().getStoragePoolId(),
                getVmTemplateId());
        tempVar.setStorageDomainId(getParameters().getStorageDomainId());
        Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveVM, tempVar);

        List<DiskImage> images = getParameters().getImages();
        for (DiskImage image : images) {
            image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
            image.setstorage_pool_id(getParameters().getStoragePoolId());
        }
        RemoveAllVmImagesParameters tempVar2 = new RemoveAllVmImagesParameters(getVmId(), images);
        tempVar2.setParentCommand(getActionType());
        tempVar2.setEntityId(getParameters().getEntityId());
        tempVar2.setForceDelete(true);
        tempVar2.setParentParemeters(getParameters());
        VdcReturnValueBase vdcRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.RemoveAllVmImages,
                        tempVar2,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

        getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE
                : AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE_FAILED;
    }

    @Override
    protected void EndSuccessfully() {
        EndRemoveTemplate();
    }

    @Override
    protected void EndWithFailure() {
        EndRemoveTemplate();
    }

    protected void EndRemoveTemplate() {
        setCommandShouldBeLogged(false);

        setSucceeded(true);
    }
}
