package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.action.RemoveExternalEventParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendEventsResourceTest extends AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.Event, AuditLog, BackendEventsResource> {

    private static final long[] LOG_IDS = {1};
    private static final String[] ORIGIN_NAMES = {"plug-in-1"};
    private static final AuditLogSeverity[] SEVERITIES = {AuditLogSeverity.ALERT};
    private static final int[] CUSTOMER_EVENT_IDS = {1};
    private static final String[] MESSAGES = {"messsage 1"};

    public BackendEventsResourceTest() {
        super(new BackendEventsResource(), SearchType.AuditLog, "Events : ");
    }

    static org.ovirt.engine.core.common.businessentities.AuditLog setUpEntityExpectations(
            org.ovirt.engine.core.common.businessentities.AuditLog entity, int index) {
        expect(entity.getaudit_log_id()).andReturn(LOG_IDS[index]).anyTimes();
        expect(entity.getseverity()).andReturn(SEVERITIES[index]).anyTimes();
        expect(entity.getmessage()).andReturn(MESSAGES[index]).anyTimes();
        expect(entity.getOrigin()).andReturn(ORIGIN_NAMES[index]).anyTimes();
        expect(entity.getCustomEventId()).andReturn(CUSTOMER_EVENT_IDS[index]).anyTimes();
        return entity;
    }

    static org.ovirt.engine.api.model.Event getModel(int index) {
        org.ovirt.engine.api.model.Event model = new org.ovirt.engine.api.model.Event();
        model.setId(String.valueOf(LOG_IDS[index]));
        model.setSeverity(SEVERITIES[index].name());
        model.setDescription(MESSAGES[index]);
        model.setOrigin(ORIGIN_NAMES[index]);
        model.setCustomId(CUSTOMER_EVENT_IDS[index]);
        return model;
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddExternalEvent,
                                  AddExternalEventParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  LOG_IDS[0],
                                  VdcQueryType.GetAuditLogById,
                                  GetAuditLogByIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { LOG_IDS[0] },
                                  getEntity(0));
        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof AuditLog);
        verifyModel((AuditLog) response.getEntity(), 0);
    }

    protected void verifyModel(AuditLog model, int index) {
        assertNotNull(model.getaudit_log_id());
        assertNotNull(model.getseverity());
        assertNotNull(model.getmessage());
        assertNotNull(model.getOrigin());
        assertNotNull(model.getCustomEventId());
    }


    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        setUpActionExpectations(VdcActionType.RemoveExternalEvent, RemoveExternalEventParameters.class, new String[] {
                "auditLogId" }, new Object[] {LOG_IDS[0]}, true, true);
        verifyRemove(collection.remove(String.valueOf(LOG_IDS[0])));
    }

    protected org.ovirt.engine.core.common.businessentities.AuditLog getEntity(int index) {
        AuditLog auditLog =  new AuditLog();
        auditLog.setaudit_log_id(LOG_IDS[index]);
        auditLog.setseverity(SEVERITIES[index]);
        auditLog.setmessage(MESSAGES[index]);
        auditLog.setOrigin(ORIGIN_NAMES[index]);
        auditLog.setCustomEventId(CUSTOMER_EVENT_IDS[index]);
        return setUpEntityExpectations(auditLog, index);
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAuditLogById,
                GetAuditLogByIdParameters.class,
                new String[] { "AuditLogId" },
                new Object[] { LOG_IDS[0] },
                new org.ovirt.engine.core.common.businessentities.AuditLog());
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    @Override
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }
    @Override
    protected List<org.ovirt.engine.api.model.Event> getCollection() {
        return collection.list().getEvent();
    }
}
