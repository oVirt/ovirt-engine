package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.ad_groups;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendGroupsResourceTest.setUpEntityExpectations;

@Ignore
public class AbstractBackendGroupResourceTest<N extends AbstractBackendGroupResource> extends
        AbstractBackendSubResourceTest<Group, ad_groups, N> {

    public AbstractBackendGroupResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected ad_groups getEntity(int index) {
        return setUpEntityExpectations(control.createMock(ad_groups.class), index);
    }

    protected List<ad_groups> getEntityList() {
        List<ad_groups> entities = new ArrayList<ad_groups>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}

