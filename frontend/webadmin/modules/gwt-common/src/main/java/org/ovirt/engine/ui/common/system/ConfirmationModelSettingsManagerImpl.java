package org.ovirt.engine.ui.common.system;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

/**
 * Resolves the settings for the specific confirmation models from the user profile.
 *
 */
public class ConfirmationModelSettingsManagerImpl implements ConfirmationModelSettingsManager {

    private static final String CONFIRM_SUSPENDING_VM = "webAdmin.confirmSuspendingVm"; //$NON-NLS-1$

    private UserProfileProperty confirmSuspendingVmProperty;

    private boolean confirmSuspendingVm = true;

    @Override
    public boolean isConfirmSuspendingVm() {
        return confirmSuspendingVm;
    }

    @Override
    public void loadConfirmSuspendingVm(Runnable successCallback) {
        loadUserProfileProperty(CONFIRM_SUSPENDING_VM,
                result -> {
                    initConfirmSuspendingVm(result);
                    successCallback.run();
                });
    }

    @Override
    public void setConfirmSuspendingVm(boolean confirm,
            boolean showError,
            BiConsumer<FrontendActionAsyncResult, UserProfileProperty> successCallback) {

        if (confirmSuspendingVm != confirm) {
            confirmSuspendingVm = confirm;
            UserProfileProperty propertyToSave =
                    confirmSuspendingVmProperty == null ? createConfirmSuspendingVmProperty()
                            : confirmSuspendingVmProperty;

            saveUserProfileProperty(propertyToSave,
                    Boolean.toString(confirmSuspendingVm),
                    (result, property) -> {
                        initConfirmSuspendingVm(property);
                        successCallback.accept(result, property);
                    },
                    result -> {
                        // reload in case of error
                        loadConfirmSuspendingVm(() -> {});
                    },
                    showError);
        }
    }

    private void initConfirmSuspendingVm(UserProfileProperty loadedProperty) {
        confirmSuspendingVmProperty = loadedProperty;
        confirmSuspendingVm = Boolean.parseBoolean(loadedProperty.getContent());
    }

    private UserProfileProperty createConfirmSuspendingVmProperty() {
        return createUserProfileProperty(CONFIRM_SUSPENDING_VM,
                Boolean.toString(confirmSuspendingVm));
    }

    private UserProfileProperty createUserProfileProperty(String key,
            String content) {

        UserProfileProperty property = UserProfileProperty.builder()
                .withPropertyId(Guid.Empty)
                .withName(key)
                .withType(PropertyType.JSON)
                .withContent(content)
                .build();
        return property;
    }

    private void loadUserProfileProperty(String key,
            Consumer<UserProfileProperty> successCallback) {
        Frontend.getInstance()
                .getUserProfileManager()
                .getUserProfileProperty(key, PropertyType.JSON, successCallback, this);
    }

    private void saveUserProfileProperty(UserProfileProperty oldProperty,
            String content,
            BiConsumer<FrontendActionAsyncResult, UserProfileProperty> successCallback,
            IFrontendActionAsyncCallback errorCallback,
            boolean showError) {

        UserProfileProperty newProperty = UserProfileProperty.builder()
                .from(oldProperty)
                .withContent(content)
                .build();

        Frontend.getInstance()
                .getUserProfileManager()
                .uploadUserProfileProperty(
                        newProperty,
                        successCallback,
                        errorCallback,
                        this,
                        showError);
    }
}
