package org.ovirt.engine.ui.common.system;

import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.ACCEPT_REMOTE_AS_SUCCESS;
import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.OVERWRITE_REMOTE;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.UserProfileManager;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;

/**
 * Resolves the settings for the specific confirmation models from the user profile.
 *
 */
public class ConfirmationModelSettingsManagerImpl implements ConfirmationModelSettingsManager {

    private static final String CONFIRM_SUSPENDING_VM = "webAdmin.confirmSuspendingVm"; //$NON-NLS-1$
    private final UserProfileManager userProfileManger;

    public ConfirmationModelSettingsManagerImpl() {
        this.userProfileManger = Frontend.getInstance().getUserProfileManager();
    }

    @Override
    public boolean isConfirmSuspendingVm() {
        return Boolean.parseBoolean(getIsConfirmSuspendingVm().getContent());
    }

    @Override
    public UserProfileProperty getIsConfirmSuspendingVm() {
        return userProfileManger
                .getUserProfile()
                .getUserProfileProperty(CONFIRM_SUSPENDING_VM, UserProfileProperty.PropertyType.JSON)
                .orElseGet(this::createConfirmSuspendingVmProperty);
    }

    @Override
    public void setConfirmSuspendingVm(boolean confirm) {

        if (isConfirmSuspendingVm() == confirm) {
            return;
        }

        userProfileManger.uploadUserProfileProperty(
                UserProfileProperty.builder()
                        .from(getIsConfirmSuspendingVm())
                        .withContent(Boolean.toString(confirm))
                        .build(),
                property -> {
                },
                result -> {
                },
                (remote, local) -> remote.getContent().equals(local.getContent()) ? ACCEPT_REMOTE_AS_SUCCESS
                        : OVERWRITE_REMOTE,
                null,
                false);
    }

    private UserProfileProperty createConfirmSuspendingVmProperty() {
        return createUserProfileProperty(CONFIRM_SUSPENDING_VM,
                Boolean.toString(true));
    }

    private UserProfileProperty createUserProfileProperty(String key,
            String content) {
        return UserProfileProperty.builder()
                .withPropertyId(Guid.Empty)
                .withName(key)
                .withType(PropertyType.JSON)
                .withContent(content)
                .build();
    }

}
