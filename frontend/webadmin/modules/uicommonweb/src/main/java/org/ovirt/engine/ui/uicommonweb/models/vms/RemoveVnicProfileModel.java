package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class RemoveVnicProfileModel extends ConfirmationModel {

    private final List<VnicProfile> profiles;
    private final boolean fullMsg;
    private final ListModel sourceListModel;

    public RemoveVnicProfileModel(ListModel sourceListModel, List<VnicProfile> profiles, boolean isFullMsg) {
        setTitle(ConstantsManager.getInstance().getConstants().removeVnicProfileTitle());
        setHashName("remove_vnic_prfoile"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().vnicProfilesMsg());

        this.sourceListModel = sourceListModel;
        this.profiles = profiles;
        this.fullMsg = isFullMsg;

        ArrayList<String> items = new ArrayList<String>();
        for (VnicProfile profile : profiles)
        {
            if (isFullMsg) {
                items.add(getRemoveVnicProfileFullMsg(profile));
            } else {
                items.add(profile.getName());
            }
        }
        setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    private void onRemove()
    {
        if (getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (VnicProfile profile : getProfiles())
        {
            VdcActionParametersBase parameters = getRemoveVnicProfileParams(profile);
            list.add(parameters);

        }

        startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVnicProfile, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        stopProgress();
                        cancel();

                    }
                }, null);
    }

    protected String getRemoveVnicProfileFullMsg(VnicProfile profile) {
        return ConstantsManager.getInstance()
                .getMessages()
                .vnicProfileFromNetwork(profile.getName(), profile.getNetworkId().toString());
    }

    protected VdcActionParametersBase getRemoveVnicProfileParams(VnicProfile profile) {
        return new VnicProfileParameters(profile);
    }

    public List<VnicProfile> getProfiles() {
        return profiles;
    }

    public boolean isFullMsg() {
        return fullMsg;
    }

    private void cancel()
    {
        sourceListModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
    }

}
