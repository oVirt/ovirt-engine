package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveVnicProfileModel extends ConfirmationModel {

    private final List<VnicProfileView> profiles;
    private final boolean fullMsg;
    private final SearchableListModel<?, VnicProfileView>  sourceListModel;

    public RemoveVnicProfileModel(SearchableListModel<?, VnicProfileView> sourceListModel, List<VnicProfileView> profiles, boolean isFullMsg) {
        setTitle(ConstantsManager.getInstance().getConstants().removeVnicProfileTitle());
        setHelpTag(HelpTag.remove_vnic_prfoile);
        setHashName("remove_vnic_prfoile"); //$NON-NLS-1$

        this.sourceListModel = sourceListModel;
        this.profiles = profiles;
        this.fullMsg = isFullMsg;

        ArrayList<String> items = new ArrayList<>();
        for (VnicProfileView profile : profiles) {
            if (isFullMsg) {
                items.add(getRemoveVnicProfileFullMsg(profile));
            } else {
                items.add(profile.getName());
            }
        }
        setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    private void onRemove() {
        if (getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (VnicProfileView profile : getProfiles()) {
            ActionParametersBase parameters = getRemoveVnicProfileParams(profile);
            list.add(parameters);

        }

        startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVnicProfile, list,
                result -> {

                    stopProgress();
                    cancel();

                }, null);
    }

    protected String getRemoveVnicProfileFullMsg(VnicProfileView profile) {
        return ConstantsManager.getInstance()
                .getMessages()
                .vnicProfileFromNetwork(profile.getName(), profile.getNetworkName());
    }

    protected ActionParametersBase getRemoveVnicProfileParams(VnicProfileView profile) {
        return new VnicProfileParameters(profile);
    }

    public List<VnicProfileView> getProfiles() {
        return profiles;
    }

    public boolean isFullMsg() {
        return fullMsg;
    }

    private void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

}
