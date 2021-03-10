package org.ovirt.engine.ui.frontend;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

/**
 * Provides utility methods for manipulating user profile properties.
 */
public class UserProfileManager {

    private static final Logger logger = Logger.getLogger(UserProfileManager.class.getName());

    private final Frontend frontend;

    /**
     * WebAdmin option fetched from the backend (contains encoded JSON).
     * Data source for {@linkplain Frontend#getWebAdminSettings()}.
     */
    private UserProfileProperty webAdminUserOption;

    public UserProfileManager(Frontend frontend) {
        this.frontend = frontend;
    }

    public void setWebAdminUserOption(UserProfileProperty option) {
        logger.info("Changed WebAdmin settings. Old: " //$NON-NLS-1$
                + webAdminUserOption
                + " New: " //$NON-NLS-1$
                + option);
        this.webAdminUserOption = option;
    }

    public UserProfileProperty getWebAdminUserOption() {
        return webAdminUserOption;
    }

    /**
     * Fetch and re-set option stored in {@linkplain #webAdminUserOption}.
     * This allows to get the most up-to-date version of user settings.
     * The settings might have changed from the time Admin Portal user logged in.
     *
     * @param callback to be executed after option is successfully fetched.
     * @param model    optional(nullable), target/state object to be informed about query status.
     */
    public void reloadWebAdminSettings(Consumer<WebAdminSettings> callback, Object model) {
        DbUser currentUser = frontend.getLoggedInUser();
        if (currentUser == null) {
            return;
        }
        getUserProfileProperty(WebAdminSettings.WEB_ADMIN, JSON, prop -> {
            setWebAdminUserOption(prop);
            callback.accept(frontend.getWebAdminSettings());
        }, model);

    }

    public void getUserProfileProperty(String name,
            UserProfileProperty.PropertyType type,
            Consumer<UserProfileProperty> successCallback,
            Object model) {
        frontend.runQuery(QueryType.GetUserProfilePropertyByNameAndUserId,
                new IdAndNameQueryParameters(frontend.getLoggedInUser().getId(), name),
                new AsyncQuery<>(model, (AsyncCallback<QueryReturnValue>) result -> {
                    UserProfileProperty prop = result.getReturnValue();
                    if (prop != null && prop.getType().equals(type)) {
                        successCallback.accept(prop);
                    }
                }));
    }

    /**
     * Async best-effort upload of user settings to the server.
     *
     * @param update          to be uploaded
     * @param successCallback after successful upload
     * @param errorCallback   any other error
     * @param model           optional(nullable), target/state object to be informed about query status.
     * @param showErrorDialog flag to switch off default error handling
     */
    void updateUserProfileProperty(UserProfileProperty update,
            IFrontendActionAsyncCallback successCallback,
            IFrontendActionAsyncCallback errorCallback,
            Object model, boolean showErrorDialog) {
        UserProfileProperty withUser = UserProfileProperty.builder()
                .from(update)
                .withUserId(frontend.getLoggedInUser().getId())
                .build();
        frontend.runAction(ActionType.UpdateUserProfileProperty,
                new UserProfilePropertyParameters(withUser),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        UserProfileProperty updated = result.getReturnValue().getActionReturnValue();
                        if (updated != null) {
                            successCallback.executed(result);
                        } else {
                            logger.severe(
                                    "User profile update failed (no option was updated).Property:" //$NON-NLS-1$
                                            + withUser);
                            errorCallback.executed(result);
                        }
                    } else {
                        logger.severe("User profile update failed. Property: " + withUser); //$NON-NLS-1$
                        errorCallback.executed(result);
                    }
                },
                model,
                showErrorDialog
        );
    }

    /**
     * @param newProp         to be created
     * @param successCallback after successful add
     * @param errorCallback   after failure
     * @param model           optional state object
     * @param showErrorDialog flag to switch off default error handling
     */
    public void createUserProfileProperty(UserProfileProperty newProp,
            IFrontendActionAsyncCallback successCallback,
            IFrontendActionAsyncCallback errorCallback,
            Object model,
            boolean showErrorDialog) {
        UserProfileProperty withUser = UserProfileProperty.builder()
                .from(newProp)
                .withUserId(frontend.getLoggedInUser().getId())
                .build();
        frontend.runAction(ActionType.AddUserProfileProperty,
                new UserProfilePropertyParameters(withUser),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        successCallback.executed(result);
                    } else {
                        logger.severe(
                                "Failed to create user profile property.Property:" + withUser); //$NON-NLS-1$
                        errorCallback.executed(result);
                    }
                },
                model,
                showErrorDialog
        );
    }

    public void uploadUserProfileProperty(UserProfileProperty update,
            BiConsumer<FrontendActionAsyncResult, UserProfileProperty> successCallback,
            Object model, boolean showErrorDialog) {
        if (Guid.Empty.equals(update.getPropertyId())) {
            createUserProfileProperty(
                    update,
                    result -> {
                        Guid id = result.getReturnValue().getActionReturnValue();
                        successCallback.accept(result, UserProfileProperty.builder()
                                .from(update)
                                .withUserId(frontend.getLoggedInUser().getId())
                                .withPropertyId(id).build());
                    },
                    result -> {
                        // for future use
                    },
                    model,
                    showErrorDialog);
            return;
        }

        updateUserProfileProperty(update,
                result -> successCallback.accept(result, result.getReturnValue().getActionReturnValue()),
                result -> {
                    // for future use
                },
                model,
                showErrorDialog);
    }

    public void uploadWebAdminSettings(UserProfileProperty update,
            IFrontendActionAsyncCallback successCallback,
            Object model, boolean showErrorDialog) {

        uploadUserProfileProperty(update, (result, updated) -> {
                    setWebAdminUserOption(updated);
                    successCallback.executed(result);
                },
                model,
                showErrorDialog);
    }
}
