package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

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

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected abstract void setUpEntityQueryExpectations(int times, Object failure) throws Exception;

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
            int index) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(isDisplay);
        networkCluster.setMigration(isMigration);
        networkCluster.setRequired(isRequired);
        expect(entity.getCluster()).andReturn(networkCluster).anyTimes();
        return setUpEntityExpectations(entity, index);
     }

    static org.ovirt.engine.core.common.businessentities.network.Network setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.network.Network entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getDataCenterId()).andReturn(GUIDS[1]).anyTimes();
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
