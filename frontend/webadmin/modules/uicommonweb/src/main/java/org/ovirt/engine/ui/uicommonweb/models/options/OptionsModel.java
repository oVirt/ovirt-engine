package org.ovirt.engine.ui.uicommonweb.models.options;

import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.OVERWRITE_REMOTE;
import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.REPORT_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.UserProfileManager;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class OptionsModel extends EntityModel<EditOptionsModel> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final String EDIT_SETTINGS = "EditSettings"; //$NON-NLS-1$
    public static String RESET_SETTINGS = "ResetSettings"; //$NON-NLS-1$
    public static String SAVE_SETTINGS = "SaveSettings"; //$NON-NLS-1$
    public static String CANCEL_SETTINGS = "CancelSettings"; //$NON-NLS-1$
    private final UserProfileManager userProfileManager;

    private final UICommand editCommand;

    private final LocalStorage localStorage;
    private final ConfirmationModelSettingsManager confirmationModelSettingsManager;

    @Inject
    public OptionsModel(LocalStorage localStorage,
            ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        this.localStorage = localStorage;
        this.confirmationModelSettingsManager = confirmationModelSettingsManager;
        this.userProfileManager = Frontend.getInstance().getUserProfileManager();
        this.editCommand = new UICommand(EDIT_SETTINGS, this, false, constants.edit());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (EDIT_SETTINGS.equalsIgnoreCase(command.getName())) {
            onEdit();
        } else if (SAVE_SETTINGS.equals(command.getName())) {
            onSave();
        } else if (CANCEL_SETTINGS.equals(command.getName())) {
            cancel();
        } else if (RESET_SETTINGS.equals(command.getName())) {
            resetSettings();
        }
    }

    private void onEdit() {
        if (getWindow() != null) {
            return;
        }

        final EditOptionsModel model = new EditOptionsModel(
                confirmationModelSettingsManager,
                localStorage,
                Frontend.getInstance()
                        .getLoggedInUser(),
                UICommand.createDefaultOkUiCommand(SAVE_SETTINGS, this),
                UICommand.createCancelUiCommand(CANCEL_SETTINGS, this),
                new UICommand(RESET_SETTINGS, this, false, constants.resetSettings()));

        model.setTitle(constants.accountSettings());

        model.setHashName("edit_options"); //$NON-NLS-1$
        setWindow(model);

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
                    // toggles Reset command availability
                    model.updateAvailability();
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
                        true));
    }

    private void resetSettings() {
        EditOptionsModel editModel = (EditOptionsModel) getWindow();

        List<Field<?>> removals = editModel.getFields()
                .stream()
                .filter(Field::isResettable)
                .filter(Field::isOnServer)
                .filter(Field::isCustom)
                .collect(Collectors.toList());

        if (removals.isEmpty()) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setHashName("edit_options_confirmation"); //$NON-NLS-1$
        model.setTitle(constants.resetSettings());
        model.setMessage(constants.areYouSureYouWantToResetTheFollowingSettings());
        model.setItems(removals.stream()
                .map(Field::getLabel)
                .distinct()
                .collect(Collectors.toList()));

        List<Boolean> results = new ArrayList<>();
        Consumer<Boolean> markAsDone = result -> {
            results.add(result);
            if (results.size() >= removals.size()) {
                setConfirmWindow(null);
                cancel();
                onEdit();
            }
        };

        UICommand ok = UICommand.createDefaultOkUiCommand("ConfirmReset", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                removals.stream()
                        .map(Field::toProp)
                        .forEach(prop -> userProfileManager.deleteProperty(
                                prop,
                                removedProp -> markAsDone.accept(true),
                                result -> markAsDone.accept(false),
                                // user has been warned so force remove
                                (remote, target) -> OVERWRITE_REMOTE,
                                model,
                                false));
            }
        });
        UICommand cancel = UICommand.createCancelUiCommand("AbortReset", new BaseCommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                setConfirmWindow(null);
            }
        });

        model.getCommands().add(ok);
        model.getCommands().add(cancel);
    }

    public UICommand getEditCommand() {
        return editCommand;
    }

    protected void cancel() {
        setWindow(null);
    }
}
