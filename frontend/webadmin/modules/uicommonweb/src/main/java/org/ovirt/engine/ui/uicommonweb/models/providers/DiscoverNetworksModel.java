package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collections;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("deprecation")
public class DiscoverNetworksModel extends Model {

    private static final String CMD_DISCOVER = "OnDiscover"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final ListModel sourceListModel;
    private final Provider provider;

    private ListModel dataCenters = new ListModel();
    private ListModel networkList = new ListModel();

    public ListModel getDataCenters() {
        return dataCenters;
    }

    public ListModel getNetworkList() {
        return networkList;
    }

    public DiscoverNetworksModel(ListModel sourceListModel, Provider provider) {
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
                List<ExternalNetwork> items = new ArrayList<ExternalNetwork>();
                for (Network network : networks) {
                    ExternalNetwork externalNetwork = new ExternalNetwork();
                    externalNetwork.setNetwork(network);
                    externalNetwork.setAttached(false);
                    externalNetwork.setPublicUse(false);
                    items.add(externalNetwork);
                }
                Collections.sort(items, new Linq.ExternalNetworkComparator());
                getNetworkList().setItems(items);

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
        ArrayList<VdcActionParametersBase> mulipleActionParameters =
                new ArrayList<VdcActionParametersBase>();
        Guid dcId = ((StoragePool) (getDataCenters().getSelectedItem())).getId();

        for (ExternalNetwork externalNetwork : (Iterable<ExternalNetwork>) getNetworkList().getItems()) {
            if (externalNetwork.isAttached()) {
                externalNetwork.getNetwork().setDataCenterId(dcId);
                AddNetworkStoragePoolParameters params =
                        new AddNetworkStoragePoolParameters(dcId, externalNetwork.getNetwork());
                params.setPublicUse(externalNetwork.isPublicUse());
                mulipleActionParameters.add(params);
            }
        }

        Frontend.RunMultipleAction(VdcActionType.AddNetwork, mulipleActionParameters);
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
