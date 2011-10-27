package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.businessentities.network;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNetworksResourceTest.setUpEntityExpectations;

@Ignore
public class AbstractBackendNetworkResourceTest<N extends AbstractBackendNetworkResource> extends
        AbstractBackendSubResourceTest<Network, network, N> {

    public AbstractBackendNetworkResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected network getEntity(int index) {
        return setUpEntityExpectations(control.createMock(network.class), index);
    }

    protected network getEntity(int index, boolean isDisplay) {
            return setUpEntityExpectations(control.createMock(network.class), isDisplay, index);
    }

    protected List<network> getEntityList() {
        return getEntities(false);
    }

    protected List<network> getEntityList(boolean isDisplay) {
        return getEntities(isDisplay);
    }

    private List<network> getEntities(boolean isDisplay) {
        List<network> entities = new ArrayList<network>();
        for (int i = 0; i < NAMES.length; i++) {
            if (isDisplay) {
                entities.add(getEntity(i, isDisplay));
            } else {
                entities.add(getEntity(i));
            }
        }
        return entities;
    }
}

