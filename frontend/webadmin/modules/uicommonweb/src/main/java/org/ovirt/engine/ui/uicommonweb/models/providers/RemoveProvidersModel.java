package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("deprecation")
public class RemoveProvidersModel extends ConfirmationModel {

    private static final String CMD_REMOVE = "OnRemove"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final SearchableListModel sourceListModel;
    private final List<Provider> providers;

    @SuppressWarnings("unchecked")
    public RemoveProvidersModel(SearchableListModel sourceListModel) {
        this.sourceListModel = sourceListModel;
        providers = (List<Provider>) sourceListModel.getSelectedItems();

        setTitle(ConstantsManager.getInstance().getConstants().removeProviderTitle());
        setHelpTag(HelpTag.remove_provider);
        setHashName("remove_provider"); //$NON-NLS-1$

        List<String> providerNames = new ArrayList<>();
        for (Provider provider : providers) {
            providerNames.add(provider.getName());
        }
        setItems(providerNames);

        UICommand tempVar = UICommand.createDefaultOkUiCommand(CMD_REMOVE, this);
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand(CMD_CANCEL, this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    private void onRemove() {
        List<VdcActionParametersBase> parameterList = new LinkedList<>();
        for (Provider provider : providers) {
            parameterList.add(new ProviderParameters(provider));
        }

        Frontend.getInstance().runMultipleActions(VdcActionType.RemoveProvider, parameterList, new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                sourceListModel.getSearchCommand().execute();
            }
        });
        cancel();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CMD_REMOVE.equals(command.getName())) {
            onRemove();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        }
    }

}
