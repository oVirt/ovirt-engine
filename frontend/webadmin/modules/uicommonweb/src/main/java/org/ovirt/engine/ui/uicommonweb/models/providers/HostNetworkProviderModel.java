package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostNetworkProviderModel extends EntityModel {

    private ListModel<Provider<OpenstackNetworkProviderProperties>> networkProviders = new ListModel<>();
    private ListModel<ProviderType> networkProviderType = new ListModel<>();
    private NeutronAgentModel neutronAgentModel = new HostNeutronAgentModel();
    private EntityModel<Boolean> useClusterDefaultNetworkProvider = new EntityModel<>(true);
    private Guid defaultProviderId = null;

    public ListModel<Provider<OpenstackNetworkProviderProperties>> getNetworkProviders() {
        return networkProviders;
    }

    public ListModel<ProviderType> getNetworkProviderType() {
        return networkProviderType;
    }

    public ListModel<String> getProviderPluginType() {
        return getNeutronAgentModel().getPluginType();
    }

    public boolean providerPluginTypeIsOpenstack() {
        return getNeutronAgentModel().pluginTypeIsOpenstack();
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    public EntityModel<String> getInterfaceMappings() {
        return getNeutronAgentModel().getInterfaceMappings();
    }

    public EntityModel<Boolean> getUseClusterDefaultNetworkProvider() {
        return useClusterDefaultNetworkProvider;
    }

    public void setDefaultProviderId(Guid defaultProviderId) {
        this.defaultProviderId = defaultProviderId;
        selectDefaultProvider();
    }

    public HostNetworkProviderModel() {
        getNetworkProviders().setIsChangeable(!getUseClusterDefaultNetworkProvider().getEntity());
        getUseClusterDefaultNetworkProvider().getEntityChangedEvent().addListener((ev, sender, args) -> {
                    getNetworkProviders().setIsChangeable(!getUseClusterDefaultNetworkProvider().getEntity());
                    selectDefaultProvider();
                });

        getNetworkProviders().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            Provider<OpenstackNetworkProviderProperties> provider = getNetworkProviders().getSelectedItem();
            boolean providerIsAvailable = provider != null;
            ProviderType providerType = providerIsAvailable ? provider.getType() : null;
            getNetworkProviderType().setIsAvailable(providerIsAvailable);
            getNetworkProviderType().setSelectedItem(providerType);
            getNeutronAgentModel().init(providerIsAvailable ? provider : new Provider<>(), providerType);
            getNeutronAgentModel().setIsAvailable(true);
        });

        getNetworkProviderType().setIsChangeable(false);
        getNetworkProviderType().setIsAvailable(false);
        getNeutronAgentModel().setIsAvailable(false);

        initNetworkProvidersList();
    }

    private void initNetworkProvidersList() {
        startProgress();
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery<>(result -> {
            stopProgress();
            List<Provider<OpenstackNetworkProviderProperties>> providers = (List) result;
            providers.add(0, getNoExternalNetworkProvider());
            getNetworkProviders().setItems(providers);
            selectDefaultProvider();
        }), ProviderType.OPENSTACK_NETWORK, ProviderType.EXTERNAL_NETWORK);
    }

    private Provider getNoExternalNetworkProvider() {
        Provider provider = new Provider();
        provider.setName(ConstantsManager.getInstance().getConstants().hostNoExternalNetworkProvider());
        return provider;
    }

    private void selectDefaultProvider() {
        if (getUseClusterDefaultNetworkProvider().getEntity()) {
            selectProviderById(defaultProviderId);
        }
    }

    public void selectProviderById(Guid providerId) {
        if (getNetworkProviders().getItems() != null) {
            Provider provider = getNetworkProviders().getItems().stream()
                    .filter(candidate -> Objects.equals(candidate.getId(), providerId))
                    .findFirst().orElse(getNoExternalNetworkProvider());
            getNetworkProviders().setSelectedItem(provider);
        }
    }

    public boolean validate() {
        setIsValid(getNeutronAgentModel().validate());
        return getIsValid();
    }

}
