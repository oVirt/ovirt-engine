package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("deprecation")
public class EditProviderModel extends ProviderModel {

    private static final String CMD_APPROVE = "OnApprove"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "OnCancel"; //$NON-NLS-1$

    private final String oldUrl;
    private Collection<Network> providedNetworks = new ArrayList<Network>();

    public EditProviderModel(SearchableListModel sourceListModel, Provider provider) {
        super(sourceListModel, VdcActionType.UpdateProvider, provider);
        setTitle(ConstantsManager.getInstance().getConstants().editProviderTitle());
        setHelpTag(HelpTag.edit_provider);
        setHashName("edit_provider"); //$NON-NLS-1$

        getName().setEntity(provider.getName());
        getDescription().setEntity(provider.getDescription());
        getType().setSelectedItem(provider.getType());
        getUrl().setEntity(provider.getUrl());
        getRequiresAuthentication().setEntity(provider.isRequiringAuthentication());
        getUsername().setEntity(provider.getUsername());
        getPassword().setEntity(provider.getPassword());
        getAuthUrl().setEntity(provider.getAuthUrl());

        if (isTypeOpenStackNetwork()) {
            getNeutronAgentModel().init(provider);
        }

        oldUrl = provider.getUrl();
    }

    @Override
    protected void preSave() {
        if (!StringHelper.stringsEqualIgnoreCase((String) getUrl().getEntity(), oldUrl)) {
            ArrayList<VdcQueryType> queryTypes = new ArrayList<VdcQueryType>();
            ArrayList<VdcQueryParametersBase> queryParams = new ArrayList<VdcQueryParametersBase>();
            final Set<VdcObjectType> providedTypes = provider.getType().getProvidedTypes();

            if (providedTypes.contains(VdcObjectType.Network)) {
                queryTypes.add(VdcQueryType.GetAllNetworksForProvider);
                queryParams.add(new IdQueryParameters(provider.getId()));
            }

            if (!queryTypes.isEmpty()) {
                startProgress(null);
                Frontend.getInstance().runMultipleQueries(queryTypes, queryParams, new IFrontendMultipleQueryAsyncCallback() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void executed(FrontendMultipleQueryAsyncResult result) {
                        stopProgress();
                        Iterator<VdcQueryReturnValue> i = result.getReturnValues().iterator();
                        if (providedTypes.contains(VdcObjectType.Network)) {
                            providedNetworks = (Collection<Network>) (i.next()).getReturnValue();
                        }
                        showConfirmation();
                    }
                });
                return;
            }
        }
        actualSave();
    }

    private void showConfirmation() {
        if (providedNetworks.isEmpty()) {
            actualSave();
            return;
        }
        StringBuilder networkList = new StringBuilder("Networks:\n"); //$NON-NLS-1$
        for (Network network : providedNetworks) {
            networkList.append("- ").append(network.getName()).append('\n'); //$NON-NLS-1$
        }

        ConfirmationModel confirmationModel = new ConfirmationModel();
        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().providerUrlWarningTitle());
        confirmationModel.setMessage(ConstantsManager.getInstance()
                .getMessages()
                .providerUrlWarningText(networkList.toString()));
        UICommand cmdOk = new UICommand(CMD_APPROVE, this);
        cmdOk.setTitle(ConstantsManager.getInstance().getConstants().ok());
        cmdOk.setIsDefault(true);
        confirmationModel.getCommands().add(cmdOk);
        UICommand cmdCancel = new UICommand(CMD_CANCEL, this);
        cmdCancel.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cmdCancel.setIsCancel(true);
        confirmationModel.getCommands().add(cmdCancel);
        sourceListModel.setConfirmWindow(confirmationModel);
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CMD_APPROVE.equals(command.getName())) {
            cancel();
            actualSave();
        } else if (CMD_CANCEL.equals(command.getName())) {
            cancel();
        }
    }

}
