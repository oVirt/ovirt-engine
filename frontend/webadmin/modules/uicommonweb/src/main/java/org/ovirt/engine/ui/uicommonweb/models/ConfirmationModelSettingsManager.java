package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;

/**
 * Resolves the settings for the specific confirmation
 * models.
 *
 */
public interface ConfirmationModelSettingsManager {

    boolean isConfirmSuspendingVm();

    UserProfileProperty getIsConfirmSuspendingVm();

    void setConfirmSuspendingVm(boolean confirm);
}
