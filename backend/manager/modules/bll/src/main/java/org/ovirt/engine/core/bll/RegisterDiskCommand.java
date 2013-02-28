package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RegisterDiskCommand <T extends RegisterDiskParameters> extends BaseImagesCommand<T> implements QuotaStorageDependent {

    private static final long serialVersionUID = -1201881996330878181L;
    private static final String DEFAULT_REGISTRATION_FORMAT = "RegisteredDisk_%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS";

    public RegisterDiskCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getDiskImage().getStorageIds().get(0));
        setStoragePoolId(parameters.getDiskImage().getStoragePoolId());
        parameters.setEntityId(parameters.getDiskImage().getId());
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
        } else if (!validate(new StorageDomainValidator(getStorageDomain()).isDomainExistAndActive())) {
            addCanDoActionMessage("$diskId " + getParameters().getDiskImage().getId());
            addCanDoActionMessage("$domainId " + getStorageDomainId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_UNAVAILABLE);
            return false;
        } else if (getStorageDomain().getStorageDomainType() != StorageDomainType.Data) {
            addCanDoActionMessage("$domainId " + getParameters().getStorageDomainId());
            addCanDoActionMessage("$domainType " + getStorageDomain().getStorageDomainType());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        final DiskImage newDiskImage = getParameters().getDiskImage();
        newDiskImage.setDiskAlias(ImagesHandler.getDiskAliasWithDefault(newDiskImage,
                generateDefaultAliasForRegiteredDisk(Calendar.getInstance())));
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
                getStorageDomainId().getValue(),
                getParameters().getDiskImage().getActualSize()));

        return list;
    }
}
