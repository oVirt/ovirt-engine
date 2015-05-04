package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class SerialNumberPolicyModel extends Model {
    EntityModel<Boolean> overrideSerialNumberPolicy;
    EntityModel<SerialNumberPolicy> serialNumberPolicy;
    EntityModel<String> customSerialNumber;

    public EntityModel<SerialNumberPolicy> getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    private void setSerialNumberPolicy(EntityModel<SerialNumberPolicy> serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
    }

    public EntityModel<String> getCustomSerialNumber() {
        return customSerialNumber;
    }

    private void setCustomSerialNumber(EntityModel<String> customSerialNumber) {
        this.customSerialNumber = customSerialNumber;
    }

    public EntityModel<Boolean> getOverrideSerialNumberPolicy() {
        return overrideSerialNumberPolicy;
    }

    public void setOverrideSerialNumberPolicy(EntityModel<Boolean> overrideSerialNumberPolicy) {
        this.overrideSerialNumberPolicy = overrideSerialNumberPolicy;
    }

    public SerialNumberPolicyModel() {
        setOverrideSerialNumberPolicy(new EntityModel<Boolean>());
        setSerialNumberPolicy(new EntityModel<SerialNumberPolicy>());
        setCustomSerialNumber(new EntityModel<String>());

        getOverrideSerialNumberPolicy().getEntityChangedEvent().addListener(this);
        getSerialNumberPolicy().getEntityChangedEvent().addListener(this);

        // hide the editors by default
        getSerialNumberPolicy().setIsAvailable(false);
        getCustomSerialNumber().setIsAvailable(false);

        // grey out the custom serial number by default
        getCustomSerialNumber().setIsChangeable(false);
    }

    public SerialNumberPolicy getSelectedSerialNumberPolicy() {
        if (isOverride()) {
            return serialNumberPolicy.getEntity();
        } else {
            return null;
        }
    }

    public void setSelectedSerialNumberPolicy(SerialNumberPolicy policy) {
        overrideSerialNumberPolicy.setEntity(policy != null);
        serialNumberPolicy.setEntity(policy);
    }

    public boolean isOverride() {
        return Boolean.TRUE.equals(overrideSerialNumberPolicy.getEntity());
    }

    public boolean validate() {
        if (isOverride() && serialNumberPolicy.getEntity() == SerialNumberPolicy.CUSTOM) {
            customSerialNumber.validateEntity(new IValidation[] {new NotEmptyValidation()});
            return customSerialNumber.getIsValid();
        }

        return true;
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getOverrideSerialNumberPolicy()) {
                overrideSerialNumberPolicyChanged();
            } else if (sender == getSerialNumberPolicy()) {
                serialNumberPolicyChanged();
            }
        }
    }

    private void overrideSerialNumberPolicyChanged() {
        final boolean enabled = Boolean.TRUE.equals(getOverrideSerialNumberPolicy().getEntity());
        serialNumberPolicy.setIsAvailable(enabled);
        customSerialNumber.setIsAvailable(enabled);
    }

    private void serialNumberPolicyChanged() {
        customSerialNumber.setIsChangeable(getSerialNumberPolicy().getEntity() == SerialNumberPolicy.CUSTOM);
    }
}
