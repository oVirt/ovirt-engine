package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

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
        getUnlimitedStorage().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getUnlimitedStorage().getEntity()) {
                    getSpecificStorage().setEntity(false);
                    getSpecificStorageValue().setIsChangeable(false);
                }
            }
        });

        getSpecificStorage().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getSpecificStorage().getEntity()) {
                    getUnlimitedStorage().setEntity(false);
                    getSpecificStorageValue().setIsChangeable(true);
                }
            }
        });
    }

    public boolean validate() {
        IntegerValidation intValidation = new IntegerValidation();
        intValidation.setMinimum(1);
        intValidation.setMaximum(65535);
        getSpecificStorageValue().setIsValid(true);
        if (getSpecificStorage().getEntity()) {
            getSpecificStorageValue().validateEntity(new IValidation[] { intValidation, new NotEmptyValidation() });
        }
        return getSpecificStorageValue().getIsValid();
    }
}
