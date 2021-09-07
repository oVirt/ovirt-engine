package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Resolves the settings for the specific confirmation
 * models.
 *
 */
public interface ConfirmationModelSettingsManager {

    void loadSettings();

    boolean isConfirmSuspendingVm();

    void setConfirmSuspendingVm(boolean confirm, boolean showError);

}
