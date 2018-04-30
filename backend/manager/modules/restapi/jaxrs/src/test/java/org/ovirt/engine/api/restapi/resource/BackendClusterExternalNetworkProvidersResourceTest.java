package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendClusterExternalNetworkProvidersResourceTest
        extends AbstractBackendCollectionResourceTest<ExternalProvider, Provider,
        BackendClusterExternalNetworkProvidersResource> {

    private static final int PROVIDER_INDEX = 0;
    private static final Guid PROVIDER_ID = GUIDS[PROVIDER_INDEX];
    private static final Guid CLUSTER_ID = GUIDS[PROVIDER_INDEX +1];

    public BackendClusterExternalNetworkProvidersResourceTest() {
        super(new BackendClusterExternalNetworkProvidersResource(CLUSTER_ID), null, null);
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
                QueryType.GetClusterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { CLUSTER_ID },
                getCluster(),
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
        }
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
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
        when(entity.getType()).thenReturn(ProviderType.EXTERNAL_NETWORK);
        return entity;
    }

    @Override
    protected List<ExternalProvider> getCollection() {
        return collection.list().getExternalProviders();
    }


    @Override
    protected void verifyCollection(List<ExternalProvider> collection) {
        assertNotNull(collection);
        assertEquals(1, collection.size());
        verifyModel(collection.get(0), 0);
    }

    @Override
    protected void verifyModel(ExternalProvider model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        verifyLinks(model);
    }
}
