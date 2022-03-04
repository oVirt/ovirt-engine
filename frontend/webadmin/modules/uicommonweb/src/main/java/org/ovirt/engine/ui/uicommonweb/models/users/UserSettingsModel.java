package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.NotificationStatus;
import org.ovirt.engine.ui.frontend.UserProfileManager;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UserSettingsModel extends SearchableListModel<DbUser, UserProfileProperty> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private final UICommand removeCommand;

    public UserSettingsModel() {
        setTitle(ConstantsManager.getInstance().getConstants().accountSettings());
        setHashName("user_settings"); //$NON-NLS-1$

        removeCommand = new UICommand("Remove", new BaseCommandTarget() {//$NON-NLS-1$
            @Override
            public void executeCommand(UICommand uiCommand) {
                removeUserProfileProperty();
            }
        });

        updateActionAvailability();
    }

    private void removeUserProfileProperty() {
        List<UserProfileProperty> selected = getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }

        List<Boolean> results = new ArrayList<>();
        Consumer<Boolean> markAsDone = result -> {
            results.add(result);
            if (results.size() >= selected.size()) {
                syncSearch();
            }
        };

        for (UserProfileProperty prop : selected) {
            Frontend.getInstance()
                    .getUserProfileManager()
                    .deleteProperty(
                            prop,
                            property -> markAsDone.accept(true),
                            error -> {
                                String validationMessage =
                                        String.join("\n", error.getReturnValue().getValidationMessages()); //$NON-NLS-1$
                                String faultMessage = error.getReturnValue().getFault().getMessage();
                                String toastMessage = error.getReturnValue().isValid() ? faultMessage
                                        : validationMessage.isEmpty() ? constants.noValidateMessage()
                                                : validationMessage;
                                Frontend.getInstance()
                                        .showToast(
                                                toastMessage,
                                                NotificationStatus.DANGER);
                                markAsDone.accept(false);
                            },
                            (remote, local) -> UserProfileManager.BaseConflictResolutionStrategy.REPORT_ERROR,
                            this,
                            false);
        }
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    @Override
    protected String getListName() {
        return "UserSettingsModel"; //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    protected void syncSearch() {
        DbUser user = getEntity();
        if (user != null) {
            super.syncSearch(QueryType.GetUserProfilePropertiesByUserId,
                    new UserProfilePropertyIdQueryParameters(user.getId(), null));
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

}
