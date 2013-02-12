package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class EditQuotaStorageModel extends EntityModel {
    EntityModel unlimitedStorage;

    EntityModel specificStorage;

    EntityModel specificStorageValue;

    public EntityModel getUnlimitedStorage() {
        return unlimitedStorage;
    }

    public void setUnlimitedStorage(EntityModel unlimitedStorage) {
        this.unlimitedStorage = unlimitedStorage;
    }

    public EntityModel getSpecificStorage() {
        return specificStorage;
    }

    public void setSpecificStorage(EntityModel specificStorage) {
        this.specificStorage = specificStorage;
    }

    public EntityModel getSpecificStorageValue() {
        return specificStorageValue;
    }

    public void setSpecificStorageValue(EntityModel specificStorageValue) {
        this.specificStorageValue = specificStorageValue;
    }

    public EditQuotaStorageModel() {
        setSpecificStorage(new EntityModel());
        getSpecificStorage().setEntity(true);
        setUnlimitedStorage(new EntityModel());
        getUnlimitedStorage().setEntity(false);
        setSpecificStorageValue(new EntityModel());
        getUnlimitedStorage().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getUnlimitedStorage().getEntity()) {
                    getSpecificStorage().setEntity(false);
                    getSpecificStorageValue().setIsChangable(false);
                }
            }
        });

        getSpecificStorage().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getSpecificStorage().getEntity()) {
                    getUnlimitedStorage().setEntity(false);
                    getSpecificStorageValue().setIsChangable(true);
                }
            }
        });
    }

    public boolean Validate() {
        IntegerValidation intValidation = new IntegerValidation();
        intValidation.setMinimum(1);
        intValidation.setMaximum(65535);
        getSpecificStorageValue().setIsValid(true);
        if ((Boolean) getSpecificStorage().getEntity()) {
            getSpecificStorageValue().ValidateEntity(new IValidation[] { intValidation, new NotEmptyValidation() });
        }
        return getSpecificStorageValue().getIsValid();
    }
}
