package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
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
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveUnregisteredVmTemplateCommand<T extends VmTemplateImportExportParameters> extends
        CommandBase<T> {

    @Inject
    private OvfHelper ovfHelper;
    @Inject
    private VmAndTemplatesGenerationsDao vmAndTemplatesGenerationsDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    private OvfEntityData ovfEntityData;
    private VmTemplate vmTemplateFromConfiguration;
    private List<DiskImage> images;

    public RemoveUnregisteredVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    public void init() {
        initUnregisteredTemplate();
    }

    private void initUnregisteredTemplate() {
        List<OvfEntityData> ovfEntityList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(getParameters().getVmTemplateId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityList.isEmpty()) {
            // We should get only one entity, since we fetched the entity with a specific Storage Domain
            ovfEntityData = ovfEntityList.get(0);
            try {
                FullEntityOvfData fullEntityOvfData = ovfHelper.readVmTemplateFromOvf(ovfEntityData.getOvfData());
                vmTemplateFromConfiguration = fullEntityOvfData.getVmTemplate();
                setVmTemplate(vmTemplateFromConfiguration);
                images = fullEntityOvfData.getDiskImages();
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception: ", e);
            }
        }
    }

    @Override
    protected boolean validate() {
        if (!validate(isTemplateExists())) {
            return false;
        }

        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        if (!validate(validator.isDomainExistAndActive())) {
            return false;
        }

        if (!validate(validator.isDataDomain())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        return true;
    }

    private ValidationResult isTemplateExists() {
        return getVmTemplate() == null ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST)
                : ValidationResult.VALID;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));

        setSucceeded(true);
        if (images != null && !images.isEmpty()) {
            List<DiskImage> imageToRemove = new ArrayList<>();
            for (DiskImage image : images) {
                if (image.getStorageIds().get(0).equals(getParameters().getStorageDomainId())) {
                    image.setStorageIds(Collections.singletonList(getParameters().getStorageDomainId()));
                    image.setStoragePoolId(getParameters().getStoragePoolId());
                    imageToRemove.add(image);
                }
            }
            RemoveAllVmImagesParameters removeAllVmImagesParameters =
                    new RemoveAllVmImagesParameters(getVmTemplateId(), imageToRemove);
            removeAllVmImagesParameters.setParentCommand(getActionType());
            removeAllVmImagesParameters.setEntityInfo(getParameters().getEntityInfo());
            removeAllVmImagesParameters.setForceDelete(true);
            removeAllVmImagesParameters.setParentParameters(getParameters());
            ActionReturnValue vdcRetValue =
                    runInternalActionWithTasksContext(ActionType.RemoveAllVmImages,
                            removeAllVmImagesParameters);
            if (vdcRetValue.getSucceeded()) {
                getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
            } else {
                getReturnValue().setFault(vdcRetValue.getFault());
                setSucceeded(false);
            }
        }
        // If another template with the same ID exists in the environment,
        // the ovf generation will not be removed
        TransactionSupport.executeInNewTransaction(() -> {
            vmAndTemplatesGenerationsDao.deleteOvfGenerations(Collections.singletonList(ovfEntityData.getEntityId()));
            unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
            unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
            return null;
        });
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

    @Override
    public VmTemplate getVmTemplate() {
        return vmTemplateFromConfiguration;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            jobProperties.put("vmtemplatename", (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
        }
        return jobProperties;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(
                getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }
}
