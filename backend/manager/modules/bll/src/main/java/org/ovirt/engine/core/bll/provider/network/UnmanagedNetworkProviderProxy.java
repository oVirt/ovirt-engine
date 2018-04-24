package org.ovirt.engine.core.bll.provider.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnmanagedNetworkProviderProxy implements NetworkProviderProxy {

    private Provider<OpenstackNetworkProviderProperties> provider;
    private ProviderValidator providerValidator;
    private static final Logger log = LoggerFactory.getLogger(UnmanagedNetworkProviderProxy.class);

    public UnmanagedNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public String add(Network network) {
        return Guid.newGuid().toString();
    }

    @Override
    public void remove(String id) {}

    @Override
    public List<Network> getAll() {
        return Injector.get(NetworkDao.class).getAllForProvider(provider.getId());
    }

    @Override
    public Network get(String id) {
        return getAll().stream()
                .filter(network -> Objects.equals(network.getProvidedBy().getExternalId(), id))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<ExternalSubnet> getAllSubnets(ProviderNetwork network) {
        return Collections.emptyList();
    }

    @Override
    public void addSubnet(ExternalSubnet subnet) {
        String err = String.format("Cannot add subnet to an unmanaged external network provider %s", provider.getName());
        log.error(err);
        throw new EngineException(EngineError.PROVIDER_FAILURE, err);
    }

    @Override
    public void removeSubnet(String id) {
        String err = String.format("Cannot removed subnet from an unmanaged external network provider %s", provider.getName());
        log.error(err);
        throw new EngineException(EngineError.PROVIDER_FAILURE, err);
    }

    @Override
    public Map<String, String> allocate(Network network, VnicProfile vnicProfile, VmNic nic, VDS host,
                                        boolean ignoreSecurityGroups, String hostBindingId) {
        Map<String, String> runtimeProperties = new HashMap<>();
        runtimeProperties.put("provider_type", "unmanaged");
        runtimeProperties.put("plugin_type", StringUtils.defaultString(
                provider.getAdditionalProperties().getPluginType()));
        return runtimeProperties;
    }

    @Override
    public void deallocate(VmNic nic) {}

    @Override
    public void testConnection() {}

    @Override
    public ProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = new ProviderValidator(provider);
        }
        return providerValidator;
    }
}
