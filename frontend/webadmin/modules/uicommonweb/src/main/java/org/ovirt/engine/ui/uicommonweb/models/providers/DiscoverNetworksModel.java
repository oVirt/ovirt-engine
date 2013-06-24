package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("deprecation")
public class DiscoverNetworksModel extends Model {

    private static final String CMD_DISCOVER = "OnDiscover"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final SearchableListModel sourceListModel;
    private final Provider provider;

    private ListModel dataCenters = new ListModel();
    private final ListModel providerNetworks = new ListModel();
    private final ListModel importedNetworks = new ListModel();

    public ListModel getDataCenters() {
        return dataCenters;
    }

    public ListModel getProviderNetworks() {
        return providerNetworks;
    }

    public ListModel getImportedNetworks() {
        return importedNetworks;
    }

    public DiscoverNetworksModel(SearchableListModel sourceListModel, Provider provider) {
        this.sourceListModel = sourceListModel;
        this.provider = provider;

        setTitle(ConstantsManager.getInstance().getConstants().discoverNetworksTitle());
        setHashName("discover_networks"); //$NON-NLS-1$

        UICommand tempVar = new UICommand(CMD_DISCOVER, this);
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().importNetworksTitle());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand(CMD_CANCEL, this);
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    public void initBackendData() {
        final AsyncQuery networkQuery = new AsyncQuery();
        networkQuery.asyncCallback = new INewAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object returnValue) {
                Iterable<Network> networks = (Iterable<Network>) returnValue;
                List<ExternalNetwork> items = new LinkedList<ExternalNetwork>();
                for (Network network : networks) {
                    ExternalNetwork externalNetwork = new ExternalNetwork();
                    externalNetwork.setNetwork(network);
                    Iterable<StoragePool> dcList = getDataCenters().getItems();
                    externalNetwork.getDataCenters().setItems(dcList);
                    externalNetwork.getDataCenters().setSelectedItem(Linq.firstOrDefault(dcList));
                    externalNetwork.setPublicUse(false);
                    items.add(externalNetwork);
                }
                Collections.sort(items, new Linq.ExternalNetworkComparator());
                providerNetworks.setItems(items);

                stopProgress();
            }
        };

        final AsyncQuery dcQuery = new AsyncQuery();
        dcQuery.asyncCallback = new INewAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<StoragePool> dataCenters = (List<StoragePool>) returnValue;
                Collections.sort(dataCenters, new NameableComparator());
                getDataCenters().setItems(dataCenters);
                getDataCenters().setSelectedItem(Linq.firstOrDefault(dataCenters));

                AsyncDataProvider.GetExternalNetworkList(networkQuery, provider.getId());
            }
        };

        startProgress(null);
        AsyncDataProvider.getDataCenterList(dcQuery);
    }

    public void cancel() {
        sourceListModel.setWindow(null);
    }

    @SuppressWarnings("unchecked")
    public void onDiscover() {
        List<VdcActionParametersBase> mulipleActionParameters =
                new LinkedList<VdcActionParametersBase>();

        for (ExternalNetwork externalNetwork : (Iterable<ExternalNetwork>) importedNetworks.getItems()) {
            Guid dcId = ((StoragePool) externalNetwork.getDataCenters().getSelectedItem()).getId();
            externalNetwork.getNetwork().setDataCenterId(dcId);
            AddNetworkStoragePoolParameters params =
                    new AddNetworkStoragePoolParameters(dcId, externalNetwork.getNetwork());
            params.setPublicUse(externalNetwork.isPublicUse());
            mulipleActionParameters.add(params);
        }

        Frontend.RunMultipleActions(VdcActionType.AddNetwork, mulipleActionParameters, new IFrontendActionAsyncCallback() {

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

        if (StringHelper.stringsEqual(command.getName(), CMD_DISCOVER)) {
            onDiscover();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_CANCEL)) {
            cancel();
        }
    }

}
