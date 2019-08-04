package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class RemoveUnregisteredEntityCommand<T extends RemoveUnregisteredEntityParameters> extends
        CommandBase<T> {

    @Inject
    private VmAndTemplatesGenerationsDao vmAndTemplatesGenerationsDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    protected OvfEntityData ovfEntityData;
    protected List<DiskImage> images;

    protected abstract EntityInfo getEntityInfo();
    protected abstract void setUnregisteredEntityAndImages() throws OvfReaderException;
    protected abstract boolean isUnregisteredEntityExists();
    protected abstract EngineMessage getEntityNotExistsMessage();
    protected abstract EngineMessage getRemoveAction();

    public RemoveUnregisteredEntityCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    public void init() {
        initUnregisteredEntity(getParameters().getEntityId(), getParameters().getStorageDomainId());
    }

    private void initUnregisteredEntity(Guid entityId, Guid storageDomainId) {
        List<OvfEntityData> ovfEntityList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(entityId, storageDomainId);
        if (!ovfEntityList.isEmpty()) {
            // We should get only one entity, since we fetched the entity with a specific Storage Domain
            ovfEntityData = ovfEntityList.get(0);
            try {
                setUnregisteredEntityAndImages();
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
        if (!validate(isEntityExists())) {
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

    private ValidationResult isEntityExists() {
        return isUnregisteredEntityExists() ?
                ValidationResult.VALID :
                new ValidationResult(getEntityNotExistsMessage());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(getRemoveAction());
    }

    @Override
    protected void executeCommand() {
        getParameters().setEntityInfo(getEntityInfo());

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
                    new RemoveAllVmImagesParameters(ovfEntityData.getEntityId(), imageToRemove);
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
        // If another entity with the same ID exists in the environment,
        // the ovf generation will not be removed
        TransactionSupport.executeInNewTransaction(() -> {
            vmAndTemplatesGenerationsDao.deleteOvfGenerations(Collections.singletonList(ovfEntityData.getEntityId()));
            unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
            unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
            return null;
        });
    }

    @Override
    protected void endSuccessfully() {
        endRemoveEntity();
    }

    @Override
    protected void endWithFailure() {
        endRemoveEntity();
    }

    private void endRemoveEntity() {
        setCommandShouldBeLogged(false);
        setSucceeded(true);
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
