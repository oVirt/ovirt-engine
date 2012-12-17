package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.LdapGroup;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendGroupsResourceTest.setUpEntityExpectations;

@Ignore
public class AbstractBackendGroupResourceTest<N extends AbstractBackendGroupResource> extends
        AbstractBackendSubResourceTest<Group, LdapGroup, N> {

    public AbstractBackendGroupResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected LdapGroup getEntity(int index) {
        return setUpEntityExpectations(control.createMock(LdapGroup.class), index);
    }

    protected List<LdapGroup> getEntityList() {
        List<LdapGroup> entities = new ArrayList<LdapGroup>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}

