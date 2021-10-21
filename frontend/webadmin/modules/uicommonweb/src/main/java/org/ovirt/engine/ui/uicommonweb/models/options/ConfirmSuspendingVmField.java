package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

class ConfirmSuspendingVmField implements Field<Boolean> {

    private final EntityModel<Boolean> confirmSuspendingVm;
    private boolean originalConfirmSuspendingVm = true;
    private final ConfirmationModelSettingsManager confirmationModelSettingsManager;

    public ConfirmSuspendingVmField(EntityModel<Boolean> model,
            ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        this.confirmSuspendingVm = model;
        this.confirmationModelSettingsManager = confirmationModelSettingsManager;
    }

    @Override
    public EntityModel<Boolean> getEntity() {
        return confirmSuspendingVm;
    }

    @Override
    public boolean isUpdated() {
        return !Objects.equals(originalConfirmSuspendingVm,
                confirmSuspendingVm.getEntity());
    }

    @Override
    public UserProfileProperty toProp() {
        return UserProfileProperty.builder()
                .from(confirmationModelSettingsManager.getIsConfirmSuspendingVm())
                .withContent(Boolean.toString(confirmSuspendingVm.getEntity()))
                .build();
    }

    @Override
    public void fromProp(UserProfileProperty prop) {
        boolean flag = confirmationModelSettingsManager.isConfirmSuspendingVm();
        originalConfirmSuspendingVm = flag;
        confirmSuspendingVm.setEntity(flag);
    }

    @Override
    public boolean isSupported(UserProfileProperty prop) {
        return confirmationModelSettingsManager.getIsConfirmSuspendingVm().getName().equals(prop.getName());
    }
}
