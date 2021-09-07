package org.ovirt.engine.ui.uicommonweb.models;

import java.util.function.BiConsumer;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;

/**
 * Resolves the settings for the specific confirmation
 * models.
 *
 */
public interface ConfirmationModelSettingsManager {

    void loadConfirmSuspendingVm(Runnable successCallback);

    boolean isConfirmSuspendingVm();

    void setConfirmSuspendingVm(boolean confirm,
            boolean showError,
            BiConsumer<FrontendActionAsyncResult, UserProfileProperty> successCallback);

}
