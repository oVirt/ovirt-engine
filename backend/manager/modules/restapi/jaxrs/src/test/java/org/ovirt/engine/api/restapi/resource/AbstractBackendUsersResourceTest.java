package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.DbUser;

import static org.easymock.classextension.EasyMock.expect;

public abstract class AbstractBackendUsersResourceTest
        extends AbstractBackendCollectionResourceTest<User, DbUser, BackendDomainUsersResource> {

    public AbstractBackendUsersResourceTest(BackendDomainUsersResource collection) {
        super(collection, null, "");
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class
    }

    @Test
    public void testRemoveBadGuid() throws Exception {
        control.replay();
        try {
            collection.remove("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected abstract void setUpEntityQueryExpectations(int times, Object failure) throws Exception;

    protected List<DbUser> getEntityList() {
        List<DbUser> entities = new ArrayList<DbUser>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected DbUser getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DbUser.class), index);
    }

    static DbUser setUpEntityExpectations(DbUser entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getFirstName()).andReturn(NAMES[index]).anyTimes();
        return entity;
    }

    protected List<User> getCollection() {
        return collection.list().getUsers();
    }

    static User getModel(int index) {
        User model = new User();
        model.setName(NAMES[index]);
        return model;
    }

    protected void verifyModel(User model, int index) {
        super.verifyModel(model, index);
    }
}
