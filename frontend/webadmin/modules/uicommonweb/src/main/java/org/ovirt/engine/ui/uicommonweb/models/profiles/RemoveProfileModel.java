package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public abstract class RemoveProfileModel<P extends ProfileBase> extends ConfirmationModel {

    private final List<P> profiles;
    private final ListModel sourceListModel;

    public RemoveProfileModel(ListModel sourceListModel, List<P> profiles) {
        this.sourceListModel = sourceListModel;
        this.profiles = profiles;

        ArrayList<String> items = new ArrayList<>();
        for (P profile : profiles) {
            items.add(profile.getName());
        }
        setItems(items);

        getCommands().add(UICommand.createDefaultOkUiCommand("OnRemove", this)); //$NON-NLS-1$
        getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    protected abstract VdcActionType getRemoveActionType();

    protected abstract VdcActionParametersBase getRemoveProfileParams(P profile);

    private void onRemove() {
        if (getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> vdcActionParametersBaseList = new ArrayList<>();
        for (P profile : getProfiles()) {
            VdcActionParametersBase parameters = getRemoveProfileParams(profile);
            vdcActionParametersBaseList.add(parameters);

        }

        startProgress();

        Frontend.getInstance().runMultipleAction(getRemoveActionType(), vdcActionParametersBaseList,
                new IFrontendMultipleActionAsyncCallback() {

                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        stopProgress();
                        cancel();

                    }
                }, null);
    }

    public List<P> getProfiles() {
        return profiles;
    }

    private void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("Cancel".equals(command.getName())) {//$NON-NLS-1$
            cancel();
        }
        else if ("OnRemove".equals(command.getName())) {//$NON-NLS-1$
            onRemove();
        }
    }

}
