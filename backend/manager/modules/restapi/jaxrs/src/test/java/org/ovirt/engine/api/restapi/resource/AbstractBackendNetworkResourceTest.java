package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNetworksResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.ovirt.engine.api.model.Network;

@Ignore
public class AbstractBackendNetworkResourceTest<N extends AbstractBackendNetworkResource> extends
        AbstractBackendSubResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, N> {

    public AbstractBackendNetworkResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.network.Network.class), index);
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index,
            boolean isDisplay,
            boolean isMigration,
            boolean isRequired) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.network.Network.class),
                isDisplay,
                isMigration,
                isRequired,
                index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getEntityList() {
        return getEntities(false, false, false);
    }

    protected List<org.ovirt.engine.core.common.businessentities.network.Network> getEntityList(boolean isDisplay,
            boolean isMigration,
            boolean isRequired) {
        return getEntities(isDisplay, isMigration, isRequired);
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getEntities(boolean isDisplay,
            boolean isMigration,
            boolean isRequired) {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            if (isDisplay) {
                entities.add(getEntity(i, isDisplay, isMigration, isRequired));
            } else {
                entities.add(getEntity(i));
            }
        }
        return entities;
    }
}

