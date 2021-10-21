package org.ovirt.engine.ui.uicommonweb.models.options;

import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.REPORT_ERROR;

import java.util.function.Consumer;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.UserProfileManager;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class OptionsModel extends EntityModel<EditOptionsModel> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final UserProfileManager userProfileManager;

    private UICommand editCommand;

    private final LocalStorage localStorage;
    private final ConfirmationModelSettingsManager confirmationModelSettingsManager;

    @Inject
    public OptionsModel(LocalStorage localStorage,
            ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        this.localStorage = localStorage;
        this.confirmationModelSettingsManager = confirmationModelSettingsManager;
        this.userProfileManager = Frontend.getInstance().getUserProfileManager();
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

        final EditOptionsModel model = new EditOptionsModel(confirmationModelSettingsManager, localStorage);

        model.setTitle(constants.editOptionsTitle());

        model.setHashName("edit_options"); //$NON-NLS-1$
        setWindow(model);

        UICommand okCommand = UICommand.createDefaultOkUiCommand(constants.ok(), this);
        model.getCommands().add(okCommand);
        // enable if values are edited
        okCommand.setIsExecutionAllowed(false);
        UICommand cancelCommand = UICommand.createCancelUiCommand(constants.cancel(), this);
        model.getCommands().add(cancelCommand);

        Frontend.getInstance()
                .getUserProfileManager()
                .reload(userProfile -> {
                    for (Field<?> field : model.getFields()) {
                        userProfile.getProperties()
                                .stream()
                                .filter(field::isSupported)
                                .findFirst()
                                .ifPresent(field::fromProp);
                    }
                });
    }

    private void onSave() {
        EditOptionsModel model = (EditOptionsModel) getWindow();
        Consumer<UserProfileProperty> closeIfNoChanges = prop -> {
            if (!model.hasChangedValues()) {
                cancel();
            }
        };

        Consumer<UserProfileProperty> updateAvailability = prop -> model.updateAvailability();

        model.getUpdates()
                .forEach(field -> userProfileManager.uploadUserProfileProperty(
                        field.toProp(),
                        ((Consumer<UserProfileProperty>) field::fromProp)
                                .andThen(updateAvailability)
                                .andThen(closeIfNoChanges),
                        result -> {
                        },
                        (remote, target) -> REPORT_ERROR,
                        model,
                        true));

        model.getRemovals()
                .forEach(field -> userProfileManager.deleteProperty(
                        field.toProp(),
                        ((Consumer<UserProfileProperty>) field::fromProp)
                                .andThen(updateAvailability)
                                .andThen(closeIfNoChanges),
                        result -> {
                        },
                        (remote, target) -> REPORT_ERROR,
                        model,
                        true)

                );
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
