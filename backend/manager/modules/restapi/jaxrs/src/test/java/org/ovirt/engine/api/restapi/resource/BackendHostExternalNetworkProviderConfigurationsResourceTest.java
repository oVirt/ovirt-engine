package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostExternalNetworkProviderConfigurationsResourceTest
        extends AbstractBackendCollectionResourceTest<ExternalNetworkProviderConfiguration, Provider,
        BackendHostExternalNetworkProviderConfigurationsResource> {

    private static final int PROVIDER_INDEX = 0;
    private static final Guid PROVIDER_ID = GUIDS[PROVIDER_INDEX];
    private static final Guid HOST_ID = GUIDS[PROVIDER_INDEX +1];
    private static final Guid CLUSTER_ID = GUIDS[PROVIDER_INDEX +1];

    public BackendHostExternalNetworkProviderConfigurationsResourceTest() {
        super(new BackendHostExternalNetworkProviderConfigurationsResource(HOST_ID), null, null);
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }

    protected void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) {
        setUpEntityQueryExpectations(
                QueryType.GetVdsByVdsId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { HOST_ID },
                getHost(),
                failure
        );

        if (failure == null) {
            while (times-- > 0) {
                setUpEntityQueryExpectations(
                        QueryType.GetProviderById,
                        IdQueryParameters.class,
                        new String[]{"Id"},
                        new Object[]{PROVIDER_ID},
                        getEntityList(),
                        failure
                );
            }
            setUpEntityQueryExpectations(
                    QueryType.GetClusterById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { CLUSTER_ID },
                    getCluster(),
                    failure
            );
        }
    }

    private VDS getHost() {
        VDS host = new VDS();
        host.setId(HOST_ID);
        host.setClusterId(CLUSTER_ID);
        return host;
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setDefaultNetworkProviderId(PROVIDER_ID);
        return cluster;
    }

    protected List<Provider> getEntityList() {
        List<Provider> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected Provider getEntity(int index) {
        return setUpEntityExpectations(mock(Provider.class), index);
    }


    static Provider setUpEntityExpectations(
            Provider entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        return entity;
    }

    @Override
    protected List<ExternalNetworkProviderConfiguration> getCollection() {
        return collection.list().getExternalNetworkProviderConfigurations();
    }

    @Override
    protected void verifyCollection(List<ExternalNetworkProviderConfiguration> collection) {
        assertNotNull(collection);
        assertEquals(1, collection.size());
        verifyModel(collection.get(0), 0);
    }

    @Override
    protected void verifyModel(ExternalNetworkProviderConfiguration model, int index) {
        assertEquals(HexUtils.string2hex(GUIDS[index].toString()), model.getId());
        assertEquals(GUIDS[index+1].toString(), model.getHost().getId());
        assertEquals(GUIDS[index].toString(), model.getExternalNetworkProvider().getId());
        verifyLinks(model);
    }

    @Override
    protected String getSubResourceId() {
        return HexUtils.string2hex(GUIDS[3].toString());
    }
}
