package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LongValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class EditQuotaStorageModel extends EntityModel<QuotaStorage> {
    EntityModel<Boolean> unlimitedStorage;

    EntityModel<Boolean> specificStorage;

    EntityModel<Long> specificStorageValue;

    public EntityModel<Boolean> getUnlimitedStorage() {
        return unlimitedStorage;
    }

    public void setUnlimitedStorage(EntityModel<Boolean> unlimitedStorage) {
        this.unlimitedStorage = unlimitedStorage;
    }

    public EntityModel<Boolean> getSpecificStorage() {
        return specificStorage;
    }

    public void setSpecificStorage(EntityModel<Boolean> specificStorage) {
        this.specificStorage = specificStorage;
    }

    public EntityModel<Long> getSpecificStorageValue() {
        return specificStorageValue;
    }

    public void setSpecificStorageValue(EntityModel<Long> specificStorageValue) {
        this.specificStorageValue = specificStorageValue;
    }

    public EditQuotaStorageModel() {
        setSpecificStorage(new EntityModel<Boolean>());
        getSpecificStorage().setEntity(true);
        setUnlimitedStorage(new EntityModel<Boolean>());
        getUnlimitedStorage().setEntity(false);
        setSpecificStorageValue(new EntityModel<Long>());
        getUnlimitedStorage().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getUnlimitedStorage().getEntity()) {
                getSpecificStorage().setEntity(false);
                getSpecificStorageValue().setIsChangeable(false);
            }
        });

        getSpecificStorage().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getSpecificStorage().getEntity()) {
                getUnlimitedStorage().setEntity(false);
                getSpecificStorageValue().setIsChangeable(true);
            }
        });
    }

    public boolean validate() {
        LongValidation longValidation = new LongValidation(1, 65535);
        getSpecificStorageValue().setIsValid(true);
        if (getSpecificStorage().getEntity()) {
            getSpecificStorageValue().validateEntity(new IValidation[] { longValidation, new NotEmptyValidation() });
        }
        return getSpecificStorageValue().getIsValid();
    }
}
