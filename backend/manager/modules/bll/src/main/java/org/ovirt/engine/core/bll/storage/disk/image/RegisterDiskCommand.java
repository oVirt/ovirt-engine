package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RegisterCinderDiskParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RegisterDiskCommand <T extends RegisterDiskParameters> extends BaseImagesCommand<T> implements QuotaStorageDependent {

    private static final String DEFAULT_REGISTRATION_FORMAT = "RegisteredDisk_%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS";

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private DiskDao diskDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private ImageDao imageDao;

    public RegisterDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStoragePoolId(getParameters().getDiskImage().getStoragePoolId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getDiskImage().getId()));
    }

    public RegisterDiskCommand(Guid commandId) {
        super(commandId);
    }

    private void refreshDiskImageIfNecessery() {
        if (getParameters().isRefreshFromStorage()) {
            GetUnregisteredDiskQueryParameters unregQueryParams =
                    new GetUnregisteredDiskQueryParameters(getParameters().getDiskImage().getId(),
                            getStorageDomainId(),
                            getStoragePoolId());
            QueryReturnValue unregQueryReturn = runInternalQuery(QueryType.GetUnregisteredDisk, unregQueryParams);
            if (unregQueryReturn.getSucceeded()) {
                setDiskImage(unregQueryReturn.getReturnValue());
            }
        } else {
            setDiskImage(getParameters().getDiskImage());
        }
    }

    @Override
    protected boolean validate() {
        refreshDiskImageIfNecessery();
        // Currently this only supports importing DiskImages or CinderDisks and does not work with LunDisks.
        if (getDiskImage().getDiskStorageType() != DiskStorageType.IMAGE &&
                getDiskImage().getDiskStorageType() != DiskStorageType.CINDER) {
            addValidationMessageVariable("diskId", getDiskImage().getId());
            addValidationMessageVariable("storageType", getDiskImage().getDiskStorageType());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_DISK_STORAGE_TYPE);
            return false;
        }

        if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExist())) {
            addValidationMessageVariable("diskId", getDiskImage().getId());
            addValidationMessageVariable("domainId", getStorageDomainId());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_UNAVAILABLE);
            return false;
        }

        if (!getStorageDomain().getStorageDomainType().isDataDomain() &&
                !(getStorageDomain().getStorageDomainType() == StorageDomainType.Volume)) {
            addValidationMessageVariable("domainId", getParameters().getStorageDomainId());
            addValidationMessageVariable("domainType", getStorageDomain().getStorageDomainType());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
            return false;
        }

        if (diskDao.get(getDiskImage().getId()) != null) {
            String diskAlias =
                    ImagesHandler.getDiskAliasWithDefault(getDiskImage(),
                            generateDefaultAliasForRegiteredDisk(Calendar.getInstance()));
            addValidationMessageVariable("diskAliases", diskAlias);
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
            return false;
        }
        if (!validate(createDiskImagesValidator(getDiskImage()).isQcowVersionSupportedForDcVersion())) {
            return false;
        }
        if (getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE &&
                !setAndValidateDiskProfiles()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            if (getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE) {
                final DiskImage newDiskImage = getDiskImage();
                newDiskImage.setDiskAlias(ImagesHandler.getDiskAliasWithDefault(newDiskImage,
                        generateDefaultAliasForRegiteredDisk(Calendar.getInstance())));
                addRegisterInitatedAuditLog();
                ArrayList<Guid> storageIds = new ArrayList<>();
                storageIds.add(getParameters().getStorageDomainId());
                newDiskImage.setStorageIds(storageIds);
                addDiskImageToDb(newDiskImage, getCompensationContext(), Boolean.TRUE);

                unregisteredDisksDao.removeUnregisteredDisk(newDiskImage.getId(), null);
                getReturnValue().setActionReturnValue(newDiskImage.getId());
                getReturnValue().setSucceeded(true);
            } else if (getDiskImage().getDiskStorageType() == DiskStorageType.CINDER) {
                ActionReturnValue returnValue = runInternalAction(ActionType.RegisterCinderDisk, new RegisterCinderDiskParameters(
                        (CinderDisk) getDiskImage(), getParameters().getStorageDomainId()));
                setReturnValue(returnValue);
            }
            return null;
        });
        fetchQcowCompat();
    }

    private void addRegisterInitatedAuditLog() {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("DiskAlias", getDiskImage().getDiskAlias());
        auditLogDirector.log(logable, AuditLogType.USER_REGISTER_DISK_INITIATED);
    }

    private void fetchQcowCompat() {
        if (getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE
                && getDiskImage().getVolumeFormat().equals(VolumeFormat.COW)) {
            DiskImage newDiskImage = getDiskImage();
            try {
                setQcowCompat(newDiskImage.getImage(),
                        newDiskImage.getStoragePoolId(),
                        newDiskImage.getId(),
                        newDiskImage.getImageId(),
                        getParameters().getStorageDomainId(),
                        null);
                // TODO: We should update the insert to also set qcow compat.
                imageDao.update(newDiskImage.getImage());
            } catch (EngineException e) {
                // Logging only
                log.error("Unable to update the image info for image '{}' (image group: '{}') on domain '{}'",
                        newDiskImage.getImageId(),
                        newDiskImage.getId(),
                        getParameters().getStorageDomainId());
            }
        }
    }

    protected static String generateDefaultAliasForRegiteredDisk(Calendar time) {
        return String.format(DEFAULT_REGISTRATION_FORMAT, time);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    protected boolean setAndValidateDiskProfiles() {
        return validate(diskProfileHelper.setAndValidateDiskProfiles(Collections.singletonMap(getDiskImage(),
                getStorageDomainId()),
                getCurrentUser()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        refreshDiskImageIfNecessery();

        list.add(new QuotaStorageConsumptionParameter(
                getParameters().getDiskImage().getQuotaId(),
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId(),
                getDiskImage().getActualSize()));

        return list;
    }

    protected DiskImagesValidator createDiskImagesValidator(DiskImage diskImage) {
        return new DiskImagesValidator(Collections.singletonList(diskImage));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getDiskImage().getDiskAlias());
        return getSucceeded() ? AuditLogType.USER_REGISTER_DISK_FINISHED_SUCCESS
                : AuditLogType.USER_REGISTER_DISK_FINISHED_FAILURE;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getDiskImage().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
