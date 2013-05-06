package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("deprecation")
public class RemoveProvidersModel extends ConfirmationModel {

    private static final String CMD_REMOVE = "OnRemove"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final ListModel sourceListModel;
    private final List<Provider> providers;

    @SuppressWarnings("unchecked")
    public RemoveProvidersModel(ListModel sourceListModel) {
        this.sourceListModel = sourceListModel;
        providers = (List<Provider>) sourceListModel.getSelectedItems();

        setTitle(ConstantsManager.getInstance().getConstants().removeProviderTitle());
        setHashName("remove_provider"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().providersMsg());

        List<String> providerNames = new ArrayList<String>();
        for (Provider provider : providers) {
            providerNames.add(provider.getName());
        }
        setItems(providerNames);

        UICommand tempVar = new UICommand(CMD_REMOVE, this);
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand(CMD_CANCEL, this);
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    private void onRemove() {
        ArrayList<VdcActionParametersBase> parameterList = new ArrayList<VdcActionParametersBase>();
        for (Provider provider : providers) {
            parameterList.add(new ProviderParameters(provider));
        }

        Frontend.RunMultipleAction(VdcActionType.RemoveProvider, parameterList);
        cancel();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), CMD_REMOVE)) {
            onRemove();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_CANCEL)) {
            cancel();
        }
    }

}
