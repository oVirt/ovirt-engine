package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.OpenStackApiVersionType;
import org.ovirt.engine.core.common.businessentities.OpenStackProtocolType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("deprecation")
public class EditProviderModel extends ProviderModel {

    private static final String CMD_APPROVE = "OnApprove"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "OnCancel"; //$NON-NLS-1$

    private final String oldUrl;
    private Collection<Network> providedNetworks = new ArrayList<>();

    public EditProviderModel(SearchableListModel sourceListModel, Provider provider) {
        super(sourceListModel, ActionType.UpdateProvider, provider);
        setTitle(ConstantsManager.getInstance().getConstants().editProviderTitle());
        setHelpTag(HelpTag.edit_provider);
        setHashName("edit_provider"); //$NON-NLS-1$

        getName().setEntity(provider.getName());
        getDescription().setEntity(provider.getDescription());
        getType().setSelectedItem(provider.getType());
        getType().setIsChangeable(false);
        getIsUnmanaged().setEntity(provider.getIsUnmanaged());
        getUrl().setEntity(provider.getUrl());
        getRequiresAuthentication().setEntity(provider.isRequiringAuthentication());
        getUsername().setEntity(provider.getUsername());
        getPassword().setEntity(provider.getPassword());
        if (provider.isRequiringAuthentication() && provider.getType().isAuthUrlAware()) {
            Uri uri = new Uri(provider.getAuthUrl());
            if (uri.isValid()) {
                getAuthProtocol().setSelectedItem(Uri.SCHEME_HTTPS.equals(uri.getScheme()) ?
                        OpenStackProtocolType.https : OpenStackProtocolType.http);

                getAuthHostname().setEntity(uri.getAuthority().getHost());
                getAuthPort().setEntity(uri.getAuthority().getPort());
                getAuthApiVersion().setSelectedItem(getOpenStackApiVersionType(uri));
            }
        }

        if (isTypeNetwork()) {
            getNeutronAgentModel().init(provider, getType().getSelectedItem());
            OpenstackNetworkProviderProperties properties =
                    (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
            getAutoSync().setEntity(properties.getAutoSync());
        }

        if (isTypeVmware()) {
            getVmwarePropertiesModel().init(provider);
        }

        if (isTypeKVM()) {
            getKvmPropertiesModel().init(provider);
        }

        if (isTypeXEN()) {
            getXenPropertiesModel().init(provider);
        }

        if (isTypeKubevirt()) {
            getKubevirtPropertiesModel().init(provider);
        }

        oldUrl = provider.getUrl();
    }

    private OpenStackApiVersionType getOpenStackApiVersionType(Uri uri) {
        String[] segments = uri.getPathSegments();
        if (segments != null && segments.length > 1) {
            if (OpenStackApiVersionType.v2_0.getName().equals(segments[1])) {
                return OpenStackApiVersionType.v2_0;
            }
        }
        return OpenStackApiVersionType.v3;
    }

    @Override
    protected void preSave() {
        if (!stringsEqualIgnoreCase(getUrl().getEntity(), oldUrl)) {
            ArrayList<QueryType> queryTypes = new ArrayList<>();
            ArrayList<QueryParametersBase> queryParams = new ArrayList<>();
            final Set<VdcObjectType> providedTypes = provider.getType().getProvidedTypes();

            if (providedTypes.contains(VdcObjectType.Network)) {
                queryTypes.add(QueryType.GetAllNetworksForProvider);
                queryParams.add(new IdQueryParameters(provider.getId()));
            }

            if (!queryTypes.isEmpty()) {
                startProgress();
                Frontend.getInstance().runMultipleQueries(queryTypes, queryParams, result -> {
                    stopProgress();
                    Iterator<QueryReturnValue> i = result.getReturnValues().iterator();
                    if (providedTypes.contains(VdcObjectType.Network)) {
                        providedNetworks = i.next().getReturnValue();
                    }
                    showConfirmation();
                });
                return;
            }
        }
        actualSave();
    }

    private static boolean stringsEqualIgnoreCase(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else {
            return s1 != null && s1.equalsIgnoreCase(s2);
        }
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
        UICommand cmdOk = UICommand.createDefaultOkUiCommand(CMD_APPROVE, this);
        confirmationModel.getCommands().add(cmdOk);
        UICommand cmdCancel = UICommand.createCancelUiCommand(CMD_CANCEL, this); //$NON-NLS-1$
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
