package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfigurations;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.model.ExternalProviders;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExternalProviderHelperTest extends AbstractBackendBaseTest {

    private final int PROVIDER_COUNT = NAMES.length;
    private final int LAST_PROVIDER_INDEX = PROVIDER_COUNT - 1;

    private BackendClusterResource resource =
            new BackendClusterResource(GUIDS[3].toString(), new BackendClustersResource());

    @Override
    protected void init() {
        initBackendResource(resource);
    }

    @Test
    public void completeExternalNetworkProviderConfigurationsTest() {
        setUpQueryExpectations();

        ExternalNetworkProviderConfigurations configurations =
                getExternalNetworkProviderConfigurations(PROVIDER_COUNT);

        BackendExternalProviderHelper.completeExternalNetworkProviderConfigurations(resource, configurations);

        List<ExternalNetworkProviderConfiguration> configurationList =
                configurations.getExternalNetworkProviderConfigurations();

        for(int i = 0; i< PROVIDER_COUNT; i++) {
            verifyModel(configurationList.get(i).getExternalNetworkProvider(), i);
        }
    }

    @Test
    public void completeExternalNetworkProviderConfigurationsNoNameTest() {

        ExternalNetworkProviderConfigurations configurations =
                getExternalNetworkProviderConfigurations(1);
        configurations.getExternalNetworkProviderConfigurations().get(0).getExternalNetworkProvider().setName(null);

        verifyFault(
                assertThrows(
                        WebApplicationException.class,
                        () -> BackendExternalProviderHelper.completeExternalNetworkProviderConfigurations(resource, configurations)),
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void completeExternalProvidersTest() {
        setUpQueryExpectations();
        ExternalProviders providers = getExternalProviders(PROVIDER_COUNT);
        BackendExternalProviderHelper.completeExternalProviders(resource, providers);
        List<ExternalProvider> providerList = providers.getExternalProviders();

        for(int i = 0; i< PROVIDER_COUNT; i++) {
            verifyModel(providerList.get(i), i);
        }
    }

    @Test
    public void completeExternalProvidersNoNameTest() {
        ExternalProviders providers = getExternalProviders(1);
        providers.getExternalProviders().get(0).setName(null);

        verifyFault(
                assertThrows(
                        WebApplicationException.class,
                        () -> BackendExternalProviderHelper.completeExternalProviders(resource, providers)),
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    private void setUpQueryExpectations() {
        for(int i = 0; i < LAST_PROVIDER_INDEX; i++) {
            setUpEntityQueryExpectations(QueryType.GetProviderByName,
                    NameQueryParameters.class,
                    new String[]{"Name"},
                    new Object[]{NAMES[i]},
                    setUpProvider(GUIDS[i]));
        }
    }

    private Provider setUpProvider(Guid id) {
        Provider provider = mock(Provider.class);
        when(provider.getId()).thenReturn(id);
        return provider;
    }

    private ExternalNetworkProviderConfigurations getExternalNetworkProviderConfigurations(int count) {
        ExternalNetworkProviderConfigurations configurations = new ExternalNetworkProviderConfigurations();
        for(int i = 0; i< PROVIDER_COUNT; i++) {
            configurations.getExternalNetworkProviderConfigurations().add(getExternalNetworkProviderConfiguration(i));
        }
        return configurations;
    }

    private ExternalNetworkProviderConfiguration getExternalNetworkProviderConfiguration(int index) {
        ExternalNetworkProviderConfiguration externalNetworkProviderConfiguration =
                new ExternalNetworkProviderConfiguration();
        externalNetworkProviderConfiguration.setExternalNetworkProvider(getExternalProvider(index));
        return externalNetworkProviderConfiguration;
    }

    private ExternalProviders getExternalProviders(int cont) {
        ExternalProviders providers = new ExternalProviders();
        for(int i = 0; i < PROVIDER_COUNT; i++) {
            providers.getExternalProviders().add(getExternalProvider(i));
        }
        return providers;
    }

    private ExternalProvider getExternalProvider(int index) {
        ExternalProvider externalProvider = new ExternalProvider();
        externalProvider.setName(NAMES[index]);
        if (index == LAST_PROVIDER_INDEX) {
            externalProvider.setId(GUIDS[index].toString());
        }
        return externalProvider;
    }

    private void verifyModel(ExternalProvider model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
    }
}
