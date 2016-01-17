package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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
    private Collection<Network> providedNetworks = new ArrayList<>();

    public EditProviderModel(SearchableListModel sourceListModel, Provider provider) {
        super(sourceListModel, VdcActionType.UpdateProvider, provider);
        setTitle(ConstantsManager.getInstance().getConstants().editProviderTitle());
        setHelpTag(HelpTag.edit_provider);
        setHashName("edit_provider"); //$NON-NLS-1$

        getName().setEntity(provider.getName());
        getDescription().setEntity(provider.getDescription());
        getType().setSelectedItem(provider.getType());
        getType().setIsChangeable(false);
        getUrl().setEntity(provider.getUrl());
        getRequiresAuthentication().setEntity(provider.isRequiringAuthentication());
        getUsername().setEntity(provider.getUsername());
        getPassword().setEntity(provider.getPassword());
        getAuthUrl().setEntity(provider.getAuthUrl());

        if (isTypeOpenStackNetwork()) {
            getNeutronAgentModel().init(provider);
        }

        if (isTypeVmware()) {
            getVmwarePropertiesModel().init(provider);
        }

        oldUrl = provider.getUrl();
    }

    @Override
    protected void preSave() {
        if (!StringHelper.stringsEqualIgnoreCase(getUrl().getEntity(), oldUrl)) {
            ArrayList<VdcQueryType> queryTypes = new ArrayList<>();
            ArrayList<VdcQueryParametersBase> queryParams = new ArrayList<>();
            final Set<VdcObjectType> providedTypes = provider.getType().getProvidedTypes();

            if (providedTypes.contains(VdcObjectType.Network)) {
                queryTypes.add(VdcQueryType.GetAllNetworksForProvider);
                queryParams.add(new IdQueryParameters(provider.getId()));
            }

            if (!queryTypes.isEmpty()) {
                startProgress();
                Frontend.getInstance().runMultipleQueries(queryTypes, queryParams, new IFrontendMultipleQueryAsyncCallback() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void executed(FrontendMultipleQueryAsyncResult result) {
                        stopProgress();
                        Iterator<VdcQueryReturnValue> i = result.getReturnValues().iterator();
                        if (providedTypes.contains(VdcObjectType.Network)) {
                            providedNetworks = i.next().getReturnValue();
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

    @Override
    protected void updateDatacentersForVolumeProvider() {
        getDataCenter().setIsChangeable(false);
        AsyncDataProvider.getInstance().getStorageDomainByName(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                StorageDomainStatic storageDomainStatic = (StorageDomainStatic) returnValue;
                AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(target, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                        if (dataCenters != null && !dataCenters.isEmpty()) {
                            getDataCenter().setSelectedItem(dataCenters.get(0));
                        } else {
                            StoragePool noneStoragePool = new StoragePool();
                            noneStoragePool.setId(Guid.Empty);
                            noneStoragePool.setName("(none)"); //$NON-NLS-1$
                            getDataCenter().setSelectedItem(noneStoragePool);
                        }
                    }
                }), storageDomainStatic.getId());
            }
        }), provider.getName());
    }
}
