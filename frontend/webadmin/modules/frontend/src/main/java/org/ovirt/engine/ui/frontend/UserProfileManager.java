package org.ovirt.engine.ui.frontend;

import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.JSON;
import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.REPORT_ERROR;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;

/**
 * Provides utility methods for manipulating user profile properties.
 */
public class UserProfileManager {

    private static final Logger logger = Logger.getLogger(UserProfileManager.class.getName());

    private final Frontend frontend;

    public interface ConflictResolutionStrategy {
        void apply(
                UserProfileProperty remoteProp,
                Consumer<Params> action,
                Params params,
                UserProfileManager userProfileManager,
                FrontendActionAsyncResult originalResult);
    }

    public enum BaseConflictResolutionStrategy implements ConflictResolutionStrategy {
        OVERWRITE_REMOTE {
            @Override
            public void apply(UserProfileProperty remoteProp,
                    Consumer<Params> action,
                    Params params,
                    UserProfileManager userProfileManager,
                    FrontendActionAsyncResult originalResult) {
                action.accept(Params.builder()
                        .from(params)
                        .withTargetProp(
                                // use remote prop as base but replace the content
                                // the new content is needed for create/replace
                                UserProfileProperty.builder()
                                        .from(remoteProp)
                                        .withContent(params.getTargetProp().getContent())
                                        .build())
                        .build());
            }
        },
        REPORT_ERROR {
            @Override
            public void apply(UserProfileProperty remoteProp,
                    Consumer<Params> action,
                    Params params,
                    UserProfileManager userProfileManager,
                    FrontendActionAsyncResult originalResult) {
                params.getErrorCallback().accept(originalResult);
            }
        },

        ACCEPT_REMOTE_AS_SUCCESS {
            @Override
            public void apply(UserProfileProperty remoteProp,
                    Consumer<Params> action,
                    Params params,
                    UserProfileManager userProfileManager,
                    FrontendActionAsyncResult originalResult) {
                userProfileManager.replaceAllVersionsInProfile(remoteProp);
                params.getSuccessCallback().accept(remoteProp);
            }
        }
    }

    /**
     * WebAdmin option injected in the page by the GWT servlet.
     */
    private UserProfileProperty injectedWebAdminUserOption;
    private UserProfile userProfile = new UserProfile();

    public UserProfileManager(Frontend frontend) {
        this.frontend = frontend;
    }

    public void setInjectedWebAdminUserOption(UserProfileProperty option) {
        logger.fine("Load WebAdmin settings injected into the page. Value: " + option); //$NON-NLS-1$
        this.injectedWebAdminUserOption = option;
    }

    /**
     * WebAdmin option fetched from the backend (contains encoded JSON). Data source for
     * {@linkplain Frontend#getWebAdminSettings()}.
     */
    public UserProfileProperty getWebAdminUserOption() {
        return userProfile.getUserProfileProperty(WebAdminSettings.WEB_ADMIN, JSON)
                .orElse(injectedWebAdminUserOption);
    }

    public void reload(Consumer<UserProfile> onSuccess) {
        DbUser currentUser = frontend.getLoggedInUser();
        if (currentUser == null) {
            return;
        }
        frontend.runQuery(QueryType.GetUserProfilePropertiesByUserId,
                new UserProfilePropertyIdQueryParameters(frontend.getLoggedInUser().getId(), null),
                new AsyncQuery<>(null, (AsyncCallback<QueryReturnValue>) result -> {
                    List<UserProfileProperty> allProperties = result.getReturnValue();
                    if (allProperties != null) {
                        UserProfile profile = UserProfile.builder()
                                .withUserId(frontend.getLoggedInUser().getId())
                                .withProperties(allProperties)
                                .build();
                        setUserProfile(profile);
                        onSuccess.accept(profile);
                    }
                }));
    }

    public void fetchUserProfileProperty(String name,
            UserProfileProperty.PropertyType type,
            Consumer<UserProfileProperty> onSuccess,
            Object model) {
        fetchUserProfileProperty(
                name,
                type,
                // by default update the profile on every successful fetch
                ((Consumer<UserProfileProperty>) this::replaceAllVersionsInProfile).andThen(onSuccess),
                // the caller should rely on default values (no low-level error handling)
                queryReturnValue -> {
                },
                () -> {
                },
                model);
    }

    private void fetchUserProfileProperty(String name,
            UserProfileProperty.PropertyType type,
            Consumer<UserProfileProperty> onSuccess,
            Consumer<QueryReturnValue> onError,
            Runnable onMissing,
            Object model) {
        frontend.runQuery(QueryType.GetUserProfilePropertyByNameAndUserId,
                new IdAndNameQueryParameters(frontend.getLoggedInUser().getId(), name),
                new AsyncQuery<>(model, (AsyncCallback<QueryReturnValue>) result -> {
                    UserProfileProperty prop = result.getReturnValue();
                    if (prop != null && prop.getType().equals(type)) {
                        onSuccess.accept(prop);
                    } else if (prop == null && result.getSucceeded()) {
                        onMissing.run();
                    } else {
                        onError.accept(result);
                    }
                }));
    }

    private void replaceUserProfileProperty(Params params) {
        frontend.runAction(ActionType.UpdateUserProfileProperty,
                new UserProfilePropertyParameters(params.getTargetProp()),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        UserProfileProperty updated = result.getReturnValue().getActionReturnValue();
                        if (updated != null) {
                            params.getSuccessCallback().accept(updated);
                            return;
                        }
                        logger.severe(
                                "User profile update failed (no option was updated).Property:" //$NON-NLS-1$
                                        + params.getTargetProp());

                    }
                    logger.severe("Failed to replace user profile property.Property:" + params.getTargetProp()); //$NON-NLS-1$
                    handleError(
                            this::replaceUserProfileProperty,
                            // fallback to create
                            this::createUserProfileProperty,
                            params,
                            result);

                },
                params.getModel(),
                params.isShowErrorDialog());
    }

    private void createUserProfileProperty(Params params) {
        frontend.runAction(ActionType.AddUserProfileProperty,
                new UserProfilePropertyParameters(params.getTargetProp()),
                result -> {
                    if (result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        Guid newId = result.getReturnValue().getActionReturnValue();
                        UserProfileProperty prop = UserProfileProperty.builder()
                                .from(params.getTargetProp())
                                .withPropertyId(newId)
                                .build();
                        params.getSuccessCallback().accept(prop);
                        return;
                    }
                    logger.severe("Failed to create user profile property.Property:" + params.getTargetProp()); //$NON-NLS-1$
                    handleError(
                            this::replaceUserProfileProperty,
                            // no fallback: report error
                            args -> params.getErrorCallback().accept(result),
                            params,
                            result);

                },
                params.getModel(),
                params.isShowErrorDialog());
    }

    private Params enhanceWithUserIdAndReplaceCallback(Params params) {
        UserProfileProperty withUser = UserProfileProperty.builder()
                .from(params.getTargetProp())
                .withUserId(frontend.getLoggedInUser().getId())
                .build();
        Consumer<UserProfileProperty> replaceInProfile = this::replaceAllVersionsInProfile;

        return Params.builder()
                .from(params)
                .withTargetProp(withUser)
                .withOnSuccess(replaceInProfile.andThen(params.onSuccess))
                .build();
    }

    /**
     *
     * @param propConflictFallbackAction
     *            action used for @{link {@link BaseConflictResolutionStrategy#OVERWRITE_REMOTE}}
     * @param missingPropFallbackAction
     *            nullable action used when the target prop is not present (and no other prop with that name exists)
     * @param originalParams
     *            parameters used to trigger the calling (failed) action
     * @param result
     *            result of the failed action
     */
    private void handleError(
            Consumer<Params> propConflictFallbackAction,
            Consumer<Params> missingPropFallbackAction,
            Params originalParams,
            FrontendActionAsyncResult result) {
        // currently there is no reliable way of checking why request was rejected
        // however version conflict can be detected by checking if there is a newer version of the prop
        // TODO: pass validation messages from the backend and treat them as error codes
        // note currently original validation messages are overridden during translation
        fetchUserProfileProperty(
                originalParams.getTargetProp().getName(),
                originalParams.getTargetProp().getType(),
                remoteProp -> {
                    if (remoteProp.getPropertyId().equals(originalParams.getTargetProp().getPropertyId())) {
                        // update was rejected for some other reason then merge conflict
                        logger.info("No ID conflict between remote and target property."); //$NON-NLS-1$
                        originalParams.getErrorCallback().accept(result);
                        return;
                    }
                    ConflictResolutionStrategy strategy = Optional.ofNullable(
                            originalParams.getResolveConflict()
                                    .apply(remoteProp, originalParams.getTargetProp()))
                            .orElse(REPORT_ERROR);
                    // update rejected (most likely) due to conflict: the property does not exist on the server
                    logger.info(
                            "Property update rejected (most likely) due to conflict. Apply conflict resolution strategy."); //$NON-NLS-1$

                    strategy.apply(remoteProp, propConflictFallbackAction, originalParams, this, result);
                },
                // report original result to the caller
                checkResult -> originalParams.getErrorCallback().accept(result),
                // the check returned no value - the target prop was removed
                // there is no other prop with this name (no conflict)
                () -> missingPropFallbackAction.accept(originalParams),
                null);
    }

    /**
     * @param targetProp
     *            to be created
     * @param onSuccess
     *            after successful add
     * @param onError
     *            after failure
     * @param model
     *            optional state object
     * @param showErrorDialog
     *            flag to switch off default error handling
     */
    public void uploadUserProfileProperty(UserProfileProperty targetProp,
            Consumer<UserProfileProperty> onSuccess,
            Consumer<FrontendActionAsyncResult> onError,
            BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict,
            Object model,
            boolean showErrorDialog) {

        Params params = enhanceWithUserIdAndReplaceCallback(new Params(
                targetProp,
                onSuccess,
                onError,
                resolveConflict,
                model,
                showErrorDialog));

        if (Guid.Empty.equals(targetProp.getPropertyId())) {
            createUserProfileProperty(params);
        } else {
            replaceUserProfileProperty(params);
        }
    }

    public void deleteProperty(UserProfileProperty targetProp,
            Consumer<UserProfileProperty> onSuccess,
            Consumer<FrontendActionAsyncResult> onError,
            BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict,
            Object model,
            boolean showErrorDialog) {
        deletePropertyAndRemoveFromProfile(new Params(
                targetProp,
                onSuccess,
                onError,
                resolveConflict,
                model,
                showErrorDialog));
    }

    /**
     * Delete the target prop from both server and client. Conflict resolution:
     * {@link BaseConflictResolutionStrategy#OVERWRITE_REMOTE} - remove requested prop from the client (on server it's
     * already gone) and remove the newer conflicting prop on the server(it never reached client).
     * {@link BaseConflictResolutionStrategy#ACCEPT_REMOTE_AS_SUCCESS} - remove requested prop from the client (on
     * server it's already gone), keep the newer prop on the server and store it on the client.
     */
    private void deleteProperty(Params params) {
        frontend.runAction(ActionType.RemoveUserProfileProperty,
                new IdParameters(params.getTargetProp().getPropertyId()),
                result -> {
                    if (result.getReturnValue().getSucceeded()) {
                        params.onSuccess.accept(params.getTargetProp());
                        return;
                    }
                    handleError(
                            this::deletePropertyAndRemoveFromProfile,
                            // report success if item to be deleted no longer exists
                            args -> args.onSuccess.accept(args.getTargetProp()),
                            params,
                            result);
                },
                params.getModel(),
                params.isShowErrorDialog());
    }

    private void deletePropertyAndRemoveFromProfile(Params params) {
        // always remove the original prop (as requested by the user) from the client (local user profile)
        Consumer<UserProfileProperty> removeOriginal = removedProperty -> removeFromProfile(params.getTargetProp());
        deleteProperty(Params.builder()
                .from(params)
                .withOnSuccess(removeOriginal.andThen(params.onSuccess))
                .build());
    }

    private void replaceAllVersionsInProfile(UserProfileProperty property) {
        UserProfile currentProfile = userProfile;
        UserProfile newProfile = UserProfile.builder()
                .from(currentProfile)
                .withProperties(currentProfile.getProperties()
                        .stream()
                        .filter(prop -> !prop.getPropertyId().equals(property.getPropertyId()))
                        .filter(prop -> !prop.getName().equals(property.getName()))
                        .collect(
                                Collectors.toList()))
                .withProp(property)
                .build();
        setUserProfile(newProfile);
    }

    private void removeFromProfile(UserProfileProperty property) {
        UserProfile currentProfile = userProfile;
        UserProfile newProfile = UserProfile.builder()
                .from(currentProfile)
                .withProperties(currentProfile.getProperties()
                        .stream()
                        .filter(prop -> !prop.getPropertyId().equals(property.getPropertyId()))
                        .collect(
                                Collectors.toList()))
                .build();
        setUserProfile(newProfile);
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    private void setUserProfile(UserProfile nextProfile) {
        UserProfile currentProfile = userProfile;
        if (currentProfile.equals(nextProfile)) {
            return;
        }

        userProfile = nextProfile;
    }

    public static class Params {
        private final UserProfileProperty targetProp;
        private final Consumer<UserProfileProperty> onSuccess;
        private final Consumer<FrontendActionAsyncResult> onError;
        private final BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict;
        private final Object model;
        private final boolean showErrorDialog;

        public Params(UserProfileProperty targetProp,
                Consumer<UserProfileProperty> onSuccess,
                Consumer<FrontendActionAsyncResult> onError,
                BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict,
                Object model,
                boolean showErrorDialog) {
            this.targetProp = targetProp;
            this.onSuccess = onSuccess;
            this.onError = onError;
            this.resolveConflict = resolveConflict;
            this.model = model;
            this.showErrorDialog = showErrorDialog;
        }

        public UserProfileProperty getTargetProp() {
            return targetProp;
        }

        public Consumer<UserProfileProperty> getSuccessCallback() {
            return onSuccess;
        }

        public Consumer<FrontendActionAsyncResult> getErrorCallback() {
            return onError;
        }

        public BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> getResolveConflict() {
            return resolveConflict;
        }

        public Object getModel() {
            return model;
        }

        public boolean isShowErrorDialog() {
            return showErrorDialog;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Consumer<UserProfileProperty> onSuccess;
            private Consumer<FrontendActionAsyncResult> onError;
            private boolean showErrorDialog;
            private UserProfileProperty targetProp;
            private BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict;
            private Object model;

            public Builder from(Params params) {
                this.onSuccess = params.getSuccessCallback();
                this.onError = params.getErrorCallback();
                this.showErrorDialog = params.isShowErrorDialog();
                this.targetProp = params.getTargetProp();
                this.resolveConflict = params.getResolveConflict();
                this.model = params.getModel();
                return this;
            }

            public Builder withOnSuccess(Consumer<UserProfileProperty> onSuccess) {
                this.onSuccess = onSuccess;
                return this;
            }

            public Params build() {
                return new Params(
                        targetProp,
                        onSuccess,
                        onError,
                        resolveConflict,
                        model,
                        showErrorDialog);
            }

            public Builder withTargetProp(UserProfileProperty prop) {
                this.targetProp = prop;
                return this;
            }

            public Builder withResolveConflict(
                    BiFunction<UserProfileProperty, UserProfileProperty, ConflictResolutionStrategy> resolveConflict) {
                this.resolveConflict = resolveConflict;
                return this;
            }
        }

    }

}
