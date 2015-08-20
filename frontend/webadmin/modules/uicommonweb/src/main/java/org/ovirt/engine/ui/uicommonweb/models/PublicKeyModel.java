package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.action.UserProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.ICancelable;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;


public class PublicKeyModel extends Model {

    private EntityModel<String> textInput;

    public EntityModel<String> getTextInput() {
        return textInput;
    }

    public void setTextInput(EntityModel<String> textInput) {
        this.textInput = textInput;
    }

    public PublicKeyModel() {
        setTextInput(new EntityModel<String>());
    }

    public void editConsoleKey(ICommandTarget target) {
        setTitle(ConstantsManager.getInstance().getConstants().consolePublicKeyTitle());
        setHashName("edit_public_key"); //$NON-NLS-1$
        setHelpTag(HelpTag.edit_public_key);

        getCommands().add(UICommand.createDefaultOkUiCommand("OnSetConsoleKey", target)); //$NON-NLS-1$
        getCommands().add(UICommand.createCancelUiCommand("Cancel", target)); //$NON-NLS-1$

        startProgress();

        AsyncDataProvider.getInstance().getUserProfile(new AsyncQuery(target,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UserProfile profile = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (profile != null) {
                            getTextInput().setEntity(profile.getSshPublicKey());
                        }
                        stopProgress();
                    }
                }));
    }

    public void onSetConsoleKey(ICommandTarget target, ICancelable caller) {
        final String publicKey = getTextInput().getEntity();
        final ICancelable cancelable = caller;

        if (getProgress() != null) {
            return;
        }

        startProgress();

        AsyncDataProvider.getInstance().getUserProfile(new AsyncQuery(target,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UserProfile profile = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        VdcActionType action = (profile != null) ? VdcActionType.UpdateUserProfile : VdcActionType.AddUserProfile;

                        Frontend.getInstance().runAction(action, new UserProfileParameters(publicKey),
                                new IFrontendActionAsyncCallback() {
                                    @Override
                                    public void executed(FrontendActionAsyncResult result) {
                                        stopProgress();
                                        cancelable.cancel();
                                    }
                                }, target);
                    }
                }));
    }
}
