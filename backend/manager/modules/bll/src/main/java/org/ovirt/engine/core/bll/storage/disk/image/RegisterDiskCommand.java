package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RegisterCinderDiskParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class RegisterDiskCommand <T extends RegisterDiskParameters> extends BaseImagesCommand<T> implements QuotaStorageDependent {

    private static final String DEFAULT_REGISTRATION_FORMAT = "RegisteredDisk_%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS";

    public RegisterDiskCommand(T parameters) {
        this(parameters, null);
    }

    public RegisterDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStoragePoolId(parameters.getDiskImage().getStoragePoolId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.Disk, parameters.getDiskImage().getId()));
    }

    protected RegisterDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        // Currently this only supports importing DiskImages or CinderDisks and does not work with LunDisks.
        if (getParameters().getDiskImage().getDiskStorageType() != DiskStorageType.IMAGE &&
                getParameters().getDiskImage().getDiskStorageType() != DiskStorageType.CINDER) {
            addValidationMessageVariable("diskId", getParameters().getDiskImage().getId());
            addValidationMessageVariable("storageType", getParameters().getDiskImage().getDiskStorageType());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_DISK_STORAGE_TYPE);
            return false;
        }

        if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExist())) {
            addValidationMessageVariable("diskId", getParameters().getDiskImage().getId());
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

        if (getParameters().getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE &&
                !setAndValidateDiskProfiles()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE) {
            final DiskImage newDiskImage = getParameters().getDiskImage();
            newDiskImage.setDiskAlias(ImagesHandler.getDiskAliasWithDefault(newDiskImage,
                    generateDefaultAliasForRegiteredDisk(Calendar.getInstance())));
            ArrayList<Guid> storageIds = new ArrayList<>();
            storageIds.add(getParameters().getStorageDomainId());
            newDiskImage.setStorageIds(storageIds);
            addDiskImageToDb(newDiskImage, getCompensationContext(), Boolean.TRUE);
            getReturnValue().setActionReturnValue(newDiskImage.getId());
            getReturnValue().setSucceeded(true);
        } else if (getParameters().getDiskImage().getDiskStorageType() == DiskStorageType.CINDER) {
            VdcReturnValueBase returnValue = runInternalAction(VdcActionType.RegisterCinderDisk, new RegisterCinderDiskParameters(
                    (CinderDisk) getParameters().getDiskImage(), getParameters().getStorageDomainId()));
            setReturnValue(returnValue);
        }
    }

    protected static String generateDefaultAliasForRegiteredDisk(Calendar time) {
        return String.format(DEFAULT_REGISTRATION_FORMAT, time);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_DISK);
    }

    protected boolean setAndValidateDiskProfiles() {
        return validate(DiskProfileHelper.setAndValidateDiskProfiles(Collections.singletonMap(getParameters().getDiskImage(),
                getStorageDomainId()),
                getStoragePool().getCompatibilityVersion(),
                getCurrentUser()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        list.add(new QuotaStorageConsumptionParameter(
                getParameters().getDiskImage().getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId(),
                getParameters().getDiskImage().getActualSize()));

        return list;
    }
}
