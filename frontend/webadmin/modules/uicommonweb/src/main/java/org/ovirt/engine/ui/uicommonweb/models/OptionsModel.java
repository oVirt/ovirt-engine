package org.ovirt.engine.ui.uicommonweb.models;

import static org.ovirt.engine.core.common.action.ActionType.RemoveUserProfileProperty;
import static org.ovirt.engine.core.common.businessentities.UserProfileProperty.PropertyType.SSH_PUBLIC_KEY;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.WebAdminSettings;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class OptionsModel extends EntityModel<EditOptionsModel> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private UICommand editCommand;

    private final LocalStorage localStorage;
    private UserProfileProperty sshPublicKeyProp;

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

        Frontend.getInstance().getUserProfileManager()
                .getUserProfileProperty(SSH_PUBLIC_KEY.name(), SSH_PUBLIC_KEY, prop -> {
                    setSshPublicKeyProp(prop);
                    model.setOriginalPublicKey(prop);
                }, model);

        Frontend.getInstance().getUserProfileManager().reloadWebAdminSettings(settings -> {
            model.getOriginalStoragePersistedOnServer().setEntity(settings.isLocalStoragePersistedOnServer());
            model.getLocalStoragePersistedOnServer().setEntity(settings.isLocalStoragePersistedOnServer());
        }, model);
    }

    private void setSshPublicKeyProp(UserProfileProperty prop) {
        this.sshPublicKeyProp = prop;
    }

    private void confirmSsh(EditOptionsModel model, UserProfileProperty update) {
        model.setOriginalPublicKey(model.getNewPublicKey());
        model.setSshUploadSucceeded(true);
        setSshPublicKeyProp(update);

        if (model.isUploadComplete()) {
            cancel();
        }
    }

    private void onSave() {
        EditOptionsModel model = (EditOptionsModel) getWindow();

        if (model.isSshKeyUpdated()) {
            Frontend.getInstance().getUserProfileManager().uploadUserProfileProperty(
                    toProp(model.getNewPublicKey()),
                    (result, update) -> confirmSsh(model, update),
                    model,
                    true);
        } else if (model.isSshKeyRemoved()) {
            Frontend.getInstance().runAction(RemoveUserProfileProperty,
                    new IdParameters(sshPublicKeyProp.getPropertyId()),
                    result -> confirmSsh(model, null),
                    model);
        } else {
            model.setSshUploadSucceeded(true);
        }

        if (model.getLocalStoragePersistedOnServer().getEntity() == model.getOriginalStoragePersistedOnServer()
                .getEntity()) {
            // no change
            model.setOptionsUploadSucceeded(true);
            return;
        }

        Map<String, String> storage = Collections.emptyMap();
        if (model.getLocalStoragePersistedOnServer().getEntity()) {
            storage = localStorage.getAllSupportedMappingsFromLocalStorage();
        }
        UserProfileProperty update = WebAdminSettings.Builder.create()
                .fromSettings(Frontend.getInstance().getWebAdminSettings())
                .withLocalStoragePersistence(model.getLocalStoragePersistedOnServer().getEntity())
                // clear the storage on the server when persistence gets disabled
                // upload local state to the server otherwise
                .withStorage(storage)
                .build()
                .encode();
        Frontend.getInstance().getUserProfileManager().uploadWebAdminSettings(
                update,
                result -> {
                    model.setOptionsUploadSucceeded(true);
                    model.getOriginalStoragePersistedOnServer()
                            .setEntity(model.getLocalStoragePersistedOnServer().getEntity());
                    if (model.isUploadComplete()) {
                        cancel();
                    }
                },
                model,
                true);
    }

    private UserProfileProperty toProp(String newPublicKey) {
        if (sshPublicKeyProp != null) {
            return UserProfileProperty.builder()
                    .from(sshPublicKeyProp)
                    .withContent(newPublicKey).build();
        }
        return UserProfileProperty.builder()
                .withDefaultSshProp()
                .withContent(newPublicKey)
                .withUserId(Frontend.getInstance().getLoggedInUser().getId())
                .build();
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
}
