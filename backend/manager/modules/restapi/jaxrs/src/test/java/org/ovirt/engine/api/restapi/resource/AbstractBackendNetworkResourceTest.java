package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.ovirt.engine.api.model.Network;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNetworksResourceTest.setUpEntityExpectations;

@Ignore
public class AbstractBackendNetworkResourceTest<N extends AbstractBackendNetworkResource> extends
        AbstractBackendSubResourceTest<Network, org.ovirt.engine.core.common.businessentities.Network, N> {

    public AbstractBackendNetworkResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Network getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.Network.class), index);
    }

    protected org.ovirt.engine.core.common.businessentities.Network getEntity(int index, boolean isDisplay) {
            return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.Network.class), isDisplay, index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.Network> getEntityList() {
        return getEntities(false);
    }

    protected List<org.ovirt.engine.core.common.businessentities.Network> getEntityList(boolean isDisplay) {
        return getEntities(isDisplay);
    }

    private List<org.ovirt.engine.core.common.businessentities.Network> getEntities(boolean isDisplay) {
        List<org.ovirt.engine.core.common.businessentities.Network> entities = new ArrayList<org.ovirt.engine.core.common.businessentities.Network>();
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

