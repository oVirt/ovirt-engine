package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.DbUser;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendUsersResourceTest.setUpEntityExpectations;

@Ignore
public class AbstractBackendUserResourceTest<N extends AbstractBackendUserResource> extends
        AbstractBackendSubResourceTest<User, DbUser, N> {

    public AbstractBackendUserResourceTest(N resource) {
        super(resource);
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected DbUser getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DbUser.class), index);
    }

    protected List<DbUser> getEntityList() {
        List<DbUser> entities = new ArrayList<DbUser>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}

