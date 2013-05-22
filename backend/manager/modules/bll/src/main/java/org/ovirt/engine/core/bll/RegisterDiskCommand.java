package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterDiskCommand <T extends RegisterDiskParameters> extends BaseImagesCommand<T> implements QuotaStorageDependent {

    private static final String DEFAULT_REGISTRATION_FORMAT = "RegisteredDisk_%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS";

    public RegisterDiskCommand(T parameters) {
        super(parameters);
        setStoragePoolId(parameters.getDiskImage().getStoragePoolId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.Disk, parameters.getDiskImage().getId()));
    }

    protected RegisterDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        // Currently this only supports importing images and does not work with luns.
        if (getParameters().getDiskImage().getDiskStorageType() != DiskStorageType.IMAGE) {
            addCanDoActionMessage("$diskId " + getParameters().getDiskImage().getId());
            addCanDoActionMessage("$storageType " + getParameters().getDiskImage().getDiskStorageType());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_UNSUPPORTED_DISK_STORAGE_TYPE);
            return false;
        }

        if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExistAndActive())) {
            addCanDoActionMessage("$diskId " + getParameters().getDiskImage().getId());
            addCanDoActionMessage("$domainId " + getStorageDomainId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_UNAVAILABLE);
            return false;
        }

        if (!getStorageDomain().getStorageDomainType().isDataDomain()) {
            addCanDoActionMessage("$domainId " + getParameters().getStorageDomainId());
            addCanDoActionMessage("$domainType " + getStorageDomain().getStorageDomainType());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        final DiskImage newDiskImage = getParameters().getDiskImage();
        newDiskImage.setDiskAlias(ImagesHandler.getDiskAliasWithDefault(newDiskImage,
                generateDefaultAliasForRegiteredDisk(Calendar.getInstance())));
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(getParameters().getStorageDomainId());
        newDiskImage.setStorageIds(storageIds);
        addDiskImageToDb(newDiskImage, getCompensationContext());
        getReturnValue().setActionReturnValue(newDiskImage.getId());
        getReturnValue().setSucceeded(true);
    }

    protected static String generateDefaultAliasForRegiteredDisk(Calendar time) {
        return String.format(DEFAULT_REGISTRATION_FORMAT, time);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        list.add(new QuotaStorageConsumptionParameter(
                getParameters().getDiskImage().getQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getStorageDomainId(),
                getParameters().getDiskImage().getActualSize()));

        return list;
    }
}
