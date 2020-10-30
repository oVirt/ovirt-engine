package org.ovirt.engine.ui.uicommonweb.models;

import static org.ovirt.engine.core.common.action.ActionType.AddUserProfileProperty;
import static org.ovirt.engine.core.common.action.ActionType.RemoveUserProfileProperty;
import static org.ovirt.engine.core.common.action.ActionType.UpdateUserProfileProperty;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.UserSettings;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class OptionsModel extends EntityModel<EditOptionsModel> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private UICommand editCommand;

    private UserProfile userProfile;
    private final LocalStorage localStorage;

    @Inject
    public OptionsModel(LocalStorage localStorage) {
        this.localStorage = localStorage;
        setEditCommand(new UICommand(constants.edit(), this));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (constants.edit().equalsIgnoreCase(command.getName())) {
            onEdit();
        } else if (constants.ok().equalsIgnoreCase(command.getName())) {
            onSave();
        } else if (constants.cancel().equalsIgnoreCase(command.getName())) {
            cancel();
        }
    }

    private void onEdit() {
        if (getWindow() != null) {
            return;
        }

        final EditOptionsModel model = new EditOptionsModel();

        model.setTitle(constants.editOptionsTitle());

        model.setHashName("edit_options"); //$NON-NLS-1$
        setWindow(model);

        UICommand okCommand = UICommand.createDefaultOkUiCommand(constants.ok(), this);
        model.getCommands().add(okCommand);
        // enable if values are edited
        okCommand.setIsExecutionAllowed(false);
        UICommand cancelCommand = UICommand.createCancelUiCommand(constants.cancel(), this);
        model.getCommands().add(cancelCommand);

        AsyncDataProvider.getInstance().getUserProfile(model.asyncQuery(returnValue -> {
            UserProfile profile = returnValue.getReturnValue();
            setUserProfile(profile);
            model.setOriginalPublicKey(getUserProfile().getSshPublicKey());
        }));

        Frontend.getInstance().reloadUser(user -> {
            model.getOriginalStoragePersistedOnServer().setEntity(isLocalStoragePersistedOnServer());
            model.getLocalStoragePersistedOnServer().setEntity(isLocalStoragePersistedOnServer());
        }, model);
    }

    private boolean isLocalStoragePersistedOnServer() {
        return Frontend.getInstance().getUserSettings().isLocalStoragePersistedOnServer();
    }

    private void onSave() {
        EditOptionsModel model = (EditOptionsModel) getWindow();

        ActionType action = chooseUserProfileAction(model);
        ActionParametersBase params = createParamsForAction(action, model.getNewPublicKey());
        if (params != null) {
            Frontend.getInstance().runAction(action, params, result -> {
                model.setSshUploadSucceeded(result.getReturnValue().getSucceeded());
                if (model.isUploadComplete()) {
                    cancel();
                }
            }, model);
        } else {
            model.setSshUploadSucceeded(true);
        }

        Frontend.getInstance().reloadUser(fetchedUser -> {
            Map<String, String> storage = Collections.emptyMap();
            if (model.getLocalStoragePersistedOnServer().getEntity()) {
                storage = localStorage.getAllSupportedMappingsFromLocalStorage();
            }
            fetchedUser.setUserOptions(UserSettings.Builder.create()
                    .fromUser(fetchedUser)
                    .withLocalStoragePersistence(model.getLocalStoragePersistedOnServer().getEntity())
                    // clear the storage on the server when persistence gets disabled
                    // upload local state to the server otherwise
                    .withStorage(storage)
                    .build()
                    .encode());
            Frontend.getInstance().uploadUserSettings(fetchedUser, result -> {
                model.setOptionsUploadSucceeded(result.getReturnValue().getSucceeded());
                if (model.isUploadComplete()) {
                    cancel();
                }
            }, model);
        }, model);
    }

    private ActionParametersBase createParamsForAction(ActionType action, String newPublicKey) {
        if (action == null) {
            return null;
        }
        switch (action) {
        case UpdateUserProfileProperty:
            return buildUserProfilePropertyParams(getUserProfile().getSshPublicKeyId(), newPublicKey);
        case AddUserProfileProperty:
            return buildUserProfilePropertyParams(Guid.newGuid(), newPublicKey);
        case RemoveUserProfileProperty:
            return new IdParameters(getUserProfile().getSshPublicKeyId());
        default:
            return null;
        }
    }

    private ActionType chooseUserProfileAction(EditOptionsModel model) {
        if (!getUserProfile().getSshProperties().isEmpty()) {
            if (model.isSshKeyUpdated()) {
                return UpdateUserProfileProperty;
            } else if (model.isSshKeyRemoved()) {
                return RemoveUserProfileProperty;
            }
        } else if (model.isSshKeyUpdated()) {
            return AddUserProfileProperty;
        }

        return null;
    }

    private ActionParametersBase buildUserProfilePropertyParams(Guid guid,
            String content) {
        return new UserProfilePropertyParameters(UserProfileProperty.builder()
                .withDefaultSshProp()
                .withPropertyId(guid)
                .withContent(content)
                .withUserId(Frontend.getInstance().getLoggedInUser().getId())
                .build());
    }

    public UICommand getEditCommand() {
        return editCommand;
    }

    public void setEditCommand(UICommand editCommand) {
        this.editCommand = editCommand;
        getCommands().add(editCommand);
    }

    protected void cancel() {
        setWindow(null);
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
