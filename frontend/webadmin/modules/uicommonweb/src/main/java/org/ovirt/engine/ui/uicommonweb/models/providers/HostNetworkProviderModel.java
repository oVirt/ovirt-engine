package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
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

    private ListModel<Provider<OpenstackNetworkProviderProperties>> networkProviders = new ListModel<>();
    private ListModel<ProviderType> networkProviderType = new ListModel<>();
    private NeutronAgentModel neutronAgentModel = new HostNeutronAgentModel();

    public ListModel<Provider<OpenstackNetworkProviderProperties>> getNetworkProviders() {
        return networkProviders;
    }

    public ListModel<ProviderType> getNetworkProviderType() {
        return networkProviderType;
    }

    public ListModel<String> getProviderPluginType() {
        return getNeutronAgentModel().getPluginType();
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    public EntityModel<String> getInterfaceMappings() {
        return getNeutronAgentModel().getInterfaceMappings();
    }

    public HostNetworkProviderModel() {
        getNetworkProviders().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Provider<OpenstackNetworkProviderProperties> provider = getNetworkProviders().getSelectedItem();
                getNetworkProviderType().setIsAvailable(provider != null);
                getNetworkProviderType().setSelectedItem(provider == null ? null : provider.getType());
                boolean isNeutron = getNetworkProviderType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
                getNeutronAgentModel().init(isNeutron ? provider : new Provider<OpenstackNetworkProviderProperties>());
                getNeutronAgentModel().setIsAvailable(isNeutron);
            }
        });

        getNetworkProviderType().setIsChangeable(false);
        getNetworkProviderType().setIsAvailable(false);
        getNeutronAgentModel().setIsAvailable(false);

        initNetworkProvidersList();
    }

    private void initNetworkProvidersList() {
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                stopProgress();
                List<Provider<OpenstackNetworkProviderProperties>> providers = (List<Provider<OpenstackNetworkProviderProperties>>) result;
                providers.add(0, null);
                getNetworkProviders().setItems(providers);
                getNetworkProviders().setSelectedItem(null);
            }
        };
        startProgress();
        AsyncDataProvider.getInstance().getAllProvidersByType(getProvidersQuery, ProviderType.OPENSTACK_NETWORK);
    }

    public boolean validate() {
        setIsValid(getNeutronAgentModel().validate());
        return getIsValid();
    }

}
