package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RemoveVmTemplateCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

@NonTransactiveCommandAttribute
public class RemoveVmTemplateFromImportExportCommand<T extends VmTemplateImportExportParameters> extends
        RemoveVmTemplateCommand<T> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    private Map<VmTemplate, List<DiskImage>> templatesFromExport;
    // this is needed since overriding getVmTemplate()
    private VmTemplate exportTemplate;

    public RemoveVmTemplateFromImportExportCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    public void init() {
    }

    @Override
    protected boolean validate() {
        if (!validate(templateExists())) {
            return false;
        }

        List<DiskImage> images = templatesFromExport.get(templatesFromExport.keySet().stream().filter(
                t -> t.getId().equals(getParameters().getVmTemplateId())).findFirst().orElse(null));

        if (images != null) {
            getParameters().setImages(images);
        } else {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        if (!validate(validator.isDomainExistAndActive())) {
            return false;
        }

        if (getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        // We fetch from DB instead of using the VmTemplate property because
        // it's the one from the export domain and not from database
        VmTemplate tmpl = vmTemplateDao.get(getVmTemplateId());
        if (tmpl != null) {
            if (tmpl.getStatus() == VmTemplateStatus.Locked) {
                return failValidation(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        RemoveVMVDSCommandParameters tempVar = new RemoveVMVDSCommandParameters(getParameters().getStoragePoolId(),
                getVmTemplateId());
        tempVar.setStorageDomainId(getParameters().getStorageDomainId());
        runVdsCommand(VDSCommandType.RemoveVM, tempVar);

        List<DiskImage> images = getParameters().getImages();
        setSucceeded(true);
        if (!images.isEmpty()) {
            for (DiskImage image : images) {
                ArrayList<Guid> storageIds = new ArrayList<>();
                storageIds.add(getParameters().getStorageDomainId());
                image.setStorageIds(storageIds);
                image.setStoragePoolId(getParameters().getStoragePoolId());
            }
            RemoveAllVmImagesParameters tempVar2 = new RemoveAllVmImagesParameters(getVmId(), images);
            tempVar2.setParentCommand(getActionType());
            tempVar2.setEntityInfo(getParameters().getEntityInfo());
            tempVar2.setForceDelete(true);
            tempVar2.setParentParameters(getParameters());
            ActionReturnValue vdcRetValue =
                    runInternalActionWithTasksContext(ActionType.RemoveAllVmImages,
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
            QueryReturnValue qretVal = runInternalQuery(
                    QueryType.GetTemplatesFromExportDomain, tempVar);

            if (qretVal.getSucceeded()) {
                templatesFromExport = qretVal.getReturnValue();
                exportTemplate = templatesFromExport.keySet().stream().filter(
                        t -> t.getId().equals(getParameters().getVmTemplateId())).findFirst().orElse(null);
                setVmTemplate(exportTemplate);
            }
        }
        return exportTemplate;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
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
