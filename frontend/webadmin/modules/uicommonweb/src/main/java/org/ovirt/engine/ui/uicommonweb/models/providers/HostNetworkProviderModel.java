package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class HostNetworkProviderModel extends EntityModel {

    private ListModel networkProviders = new ListModel();
    private ListModel networkProviderType = new ListModel();
    private NeutronAgentModel neutronAgentModel = new HostNeutronAgentModel();

    public ListModel getNetworkProviders() {
        return networkProviders;
    }

    public ListModel getNetworkProviderType() {
        return networkProviderType;
    }

    public ListModel getProviderPluginType() {
        return getNeutronAgentModel().getPluginType();
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    public EntityModel getInterfaceMappings() {
        return getNeutronAgentModel().getInterfaceMappings();
    }

    public HostNetworkProviderModel() {
        getNetworkProviders().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Provider provider = (Provider) getNetworkProviders().getSelectedItem();
                getNetworkProviderType().setIsAvailable(provider != null);
                getNetworkProviderType().setSelectedItem(provider == null ? null : provider.getType());
                boolean isNeutron = (getNetworkProviderType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK);
                getNeutronAgentModel().init(isNeutron ? provider : new Provider());
                getNeutronAgentModel().setIsAvailable(isNeutron);
            }
        });

        getNetworkProviderType().setIsChangable(false);
        getNetworkProviderType().setIsAvailable(false);
        getNeutronAgentModel().setIsAvailable(false);

        initNetworkProvidersList();
    }

    private void initNetworkProvidersList() {
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                stopProgress();
                List<Provider> providers = (List<Provider>) result;
                providers.add(0, null);
                getNetworkProviders().setItems(providers);
                getNetworkProviders().setSelectedItem(null);
            }
        };
        startProgress(null);
        AsyncDataProvider.GetAllNetworkProviders(getProvidersQuery);
    }

    public boolean validate() {
        setIsValid(getNeutronAgentModel().validate());
        return getIsValid();
    }

}
