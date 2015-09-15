package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class OptionsModel extends EntityModel<EditOptionsModel> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private UICommand editCommand;

    private UserProfile userProfile;

    public OptionsModel() {
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

        EditOptionsModel model = new EditOptionsModel();

        model.setTitle(constants.editOptionsTitle());

        model.setHashName("edit_options"); //$NON-NLS-1$
        setWindow(model);

        UICommand okCommand = UICommand.createDefaultOkUiCommand(constants.ok(), this);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand(constants.cancel(), this);
        model.getCommands().add(cancelCommand);

        AsyncDataProvider.getInstance().getUserProfile(new AsyncQuery(model, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                Boolean connectAutomatically = Boolean.TRUE;
                UserProfile profile = ((VdcQueryReturnValue) returnValue).getReturnValue();
                if (profile != null) {
                    setUserProfile(profile);
                    connectAutomatically = profile.isUserPortalVmLoginAutomatically();
                    ((EditOptionsModel) model).getPublicKey().setEntity(profile.getSshPublicKey());
                }
                ((EditOptionsModel) model).getEnableConnectAutomatically().setEntity(connectAutomatically);
            }
        }));
    }

    private void onSave() {
        EditOptionsModel model = (EditOptionsModel) getWindow();
        UserProfileParameters params = new UserProfileParameters();
        VdcActionType action = VdcActionType.AddUserProfile;
        if (getUserProfile() != null) {
            action = VdcActionType.UpdateUserProfile;
            params.setUserProfile(getUserProfile());
        }
        params.getUserProfile().setUserPortalVmLoginAutomatically(model.getEnableConnectAutomatically().getEntity().booleanValue());
        params.getUserProfile().setSshPublicKey(model.getPublicKey().getEntity());
        model.startProgress(null);
        Frontend.getInstance().runAction(action, params, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                EditOptionsModel model = (EditOptionsModel) result.getState();
                model.stopProgress();
                cancel();
            }
        }, model);
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
