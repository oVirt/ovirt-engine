package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@NonTransactiveCommandAttribute
public class RemoveVmTemplateFromImportExportCommand<T extends VmTemplateImportExportParameters> extends
        RemoveVmTemplateCommand<T> {

    private Map<VmTemplate, List<DiskImage>> templatesFromExport;
    // this is needed since overriding getVmTemplate()
    private VmTemplate exportTemplate;

    public RemoveVmTemplateFromImportExportCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = validate(templateExists());
        if (retVal) {
            List<DiskImage> images = templatesFromExport.get(LinqUtils.firstOrNull(templatesFromExport.keySet(),
                        new Predicate<VmTemplate>() {
                            @Override
                            public boolean eval(VmTemplate t) {
                                return t.getId().equals(getParameters().getVmTemplateId());
                            }
                        }));

            if (images != null) {
                getParameters().setImages(images);
            } else {
                retVal = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            }
        }

        if (retVal) {
            StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
            retVal = validate(validator.isDomainExistAndActive());
        }
        if (retVal && getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            retVal = false;
        }
        if (retVal) {
            // we fectch from db and not using VmTmplate property becase
            // VmTemplate is the one from export domain and not from database
            VmTemplate tmpl = DbFacade.getInstance().getVmTemplateDao().get(getVmTemplateId());
            if (tmpl != null) {
                retVal = (tmpl.getStatus() != VmTemplateStatus.Locked);
                if (!retVal) {
                    getReturnValue().getCanDoActionMessages()
                            .add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString());
                }
            }
        }
        return retVal;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        RemoveVMVDSCommandParameters tempVar = new RemoveVMVDSCommandParameters(getParameters().getStoragePoolId(),
                getVmTemplateId());
        tempVar.setStorageDomainId(getParameters().getStorageDomainId());
        Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveVM, tempVar);

        List<DiskImage> images = getParameters().getImages();
        setSucceeded(true);
        if (!images.isEmpty()) {
            for (DiskImage image : images) {
                ArrayList<Guid> storageIds = new ArrayList<Guid>();
                storageIds.add(getParameters().getStorageDomainId());
                image.setStorageIds(storageIds);
                image.setStoragePoolId(getParameters().getStoragePoolId());
            }
            RemoveAllVmImagesParameters tempVar2 = new RemoveAllVmImagesParameters(getVmId(), images);
            tempVar2.setParentCommand(getActionType());
            tempVar2.setEntityInfo(getParameters().getEntityInfo());
            tempVar2.setForceDelete(true);
            tempVar2.setParentParameters(getParameters());
            VdcReturnValueBase vdcRetValue =
                    runInternalActionWithTasksContext(VdcActionType.RemoveAllVmImages,
                            tempVar2);
            if (vdcRetValue.getSucceeded()) {
                getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
            } else {
                getReturnValue().setFault(vdcRetValue.getFault());
                setSucceeded(false);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE
                : AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE_FAILED;
    }

    @Override
    protected void endSuccessfully() {
        endRemoveTemplate();
    }

    @Override
    protected void endWithFailure() {
        endRemoveTemplate();
    }

    protected void endRemoveTemplate() {
        setCommandShouldBeLogged(false);
        setSucceeded(true);
    }

    /*
     * get template from export domain
     */
    @Override
    public VmTemplate getVmTemplate() {
        if (exportTemplate == null) {
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getStorageDomainId());
            VdcQueryReturnValue qretVal = runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            if (qretVal.getSucceeded()) {
                templatesFromExport = qretVal.getReturnValue();
                exportTemplate = LinqUtils.firstOrNull(templatesFromExport.keySet(), new Predicate<VmTemplate>() {
                    @Override
                    public boolean eval(VmTemplate t) {
                        return t.getId().equals(getParameters().getVmTemplateId());
                    }
                });
                setVmTemplate(exportTemplate);
            }
        }
        return exportTemplate;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
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
