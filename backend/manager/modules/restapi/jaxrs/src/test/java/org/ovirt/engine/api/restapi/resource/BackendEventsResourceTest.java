package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.LogSeverity;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendEventsResourceTest extends AbstractBackendCollectionResourceTest<Event, AuditLog, BackendEventsResource> {

    private static final long[] LOG_IDS = { 1, 2, 3 };
    private static final String[] MESSAGES = {"alert1", "alert2", "alert3"};
    private static final String[] ORIGIN_NAMES = { "plug-in-1", "plug-in-2", "plug-in-3" };
    private static final Integer[] CUSTOMER_EVENT_IDS = { 11, 22, 33 };

    public BackendEventsResourceTest() {
        super(new BackendEventsResource(), SearchType.AuditLog, "Events : ");
    }

    static org.ovirt.engine.api.model.Event getModel(int index) {
        org.ovirt.engine.api.model.Event model = new org.ovirt.engine.api.model.Event();
        model.setId(String.valueOf(LOG_IDS[index]));
        model.setSeverity( LogSeverity.ALERT);
        model.setDescription(MESSAGES[index]);
        model.setOrigin(ORIGIN_NAMES[index]);
        model.setCustomId(CUSTOMER_EVENT_IDS[index]);
        model.setFloodRate(30);
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
        assertTrue(response.getEntity() instanceof Event);
        verifyModel((Event) response.getEntity(), 0);
    }

    protected void verifyModel(Event model, int index) {
        assertNotNull(model);
        assertEquals(model.getId(), String.valueOf(LOG_IDS[index]));
        assertEquals(LogSeverity.ALERT, model.getSeverity());
        assertEquals(model.getDescription(), MESSAGES[index]);
        assertEquals(model.getOrigin(), ORIGIN_NAMES[index]);
        assertEquals(model.getCustomId(), CUSTOMER_EVENT_IDS[index]);
    }


    protected org.ovirt.engine.core.common.businessentities.AuditLog getEntity(int index) {
        AuditLog auditLogMock = control.createMock(org.ovirt.engine.core.common.businessentities.AuditLog.class);
        expect(auditLogMock.getAuditLogId()).andReturn(LOG_IDS[index]).anyTimes();
        expect(auditLogMock.getSeverity()).andReturn(AuditLogSeverity.ALERT).anyTimes();
        expect(auditLogMock.getMessage()).andReturn(MESSAGES[index]).anyTimes();
        expect(auditLogMock.getOrigin()).andReturn(ORIGIN_NAMES[index]).anyTimes();
        expect(auditLogMock.getCustomEventId()).andReturn(CUSTOMER_EVENT_IDS[index]).anyTimes();
        expect(auditLogMock.getLogType()).andReturn(AuditLogType.EXTERNAL_ALERT).anyTimes();
        expect(auditLogMock.getLogTime()).andReturn(new Date()).anyTimes();
        return auditLogMock;
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
    public void testUndelete() throws Exception {

        setUriInfo(setUpActionExpectations(VdcActionType.DisplayAllAuditLogAlerts,
                VdcActionParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        collection.undelete(new Action());
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
        return collection.list().getEvents();
    }
}
