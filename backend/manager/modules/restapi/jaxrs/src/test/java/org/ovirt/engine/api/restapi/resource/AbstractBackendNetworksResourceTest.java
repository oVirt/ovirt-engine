package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.interfaces.SearchType;

public abstract class AbstractBackendNetworksResourceTest<R extends AbstractBackendNetworksResource>
        extends AbstractBackendCollectionResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, R> {

    public AbstractBackendNetworksResourceTest(R collection) {
        super(collection, null, "");
    }

    public AbstractBackendNetworksResourceTest(R collection, SearchType searchType, String searchPrefix) {
        super(collection, searchType, searchPrefix);
    }

    protected void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    protected abstract void setUpEntityQueryExpectations(int times, Object failure);

    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.Network network = new org.ovirt.engine.core.common.businessentities.network.Network();
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(false);
        networkCluster.setMigration(false);
        networkCluster.setRequired(false);
        networkCluster.setDefaultRoute(false);
        network.setCluster(networkCluster);
        network.setId(GUIDS[index]);
        network.setName(NAMES[index]);
        network.setDescription(DESCRIPTIONS[index]);
        network.setDataCenterId(GUIDS[1]);
        return network;
    }

    static org.ovirt.engine.core.common.businessentities.network.Network setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.Network entity,
            boolean isDisplay,
            boolean isMigration,
            boolean isRequired,
            boolean isDefaultRoute,
            int index) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(isDisplay);
        networkCluster.setMigration(isMigration);
        networkCluster.setRequired(isRequired);
        networkCluster.setDefaultRoute(isDefaultRoute);
        when(entity.getCluster()).thenReturn(networkCluster);
        return setUpEntityExpectations(entity, index);
     }

    static org.ovirt.engine.core.common.businessentities.network.Network setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.Network entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getDataCenterId()).thenReturn(GUIDS[1]);
        return entity;
    }

    protected List<Network> getCollection() {
        return collection.list().getNetworks();
    }

    static Network getModel(int index) {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }
}
