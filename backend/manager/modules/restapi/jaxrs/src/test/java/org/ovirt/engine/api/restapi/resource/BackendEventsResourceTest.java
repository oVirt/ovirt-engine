package org.ovirt.engine.api.restapi.resource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.LogSeverity;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalEventParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddExternalEvent,
                                  AddExternalEventParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  LOG_IDS[0],
                                  QueryType.GetAuditLogById,
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
        AuditLog auditLogMock = mock(org.ovirt.engine.core.common.businessentities.AuditLog.class);
        when(auditLogMock.getAuditLogId()).thenReturn(LOG_IDS[index]);
        when(auditLogMock.getSeverity()).thenReturn(AuditLogSeverity.ALERT);
        when(auditLogMock.getMessage()).thenReturn(MESSAGES[index]);
        when(auditLogMock.getOrigin()).thenReturn(ORIGIN_NAMES[index]);
        when(auditLogMock.getCustomEventId()).thenReturn(CUSTOMER_EVENT_IDS[index]);
        when(auditLogMock.getLogType()).thenReturn(AuditLogType.EXTERNAL_ALERT);
        when(auditLogMock.getLogTime()).thenReturn(new Date());
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
    public void testUndelete() {

        setUriInfo(setUpActionExpectations(ActionType.DisplayAllAuditLogAlerts,
                ActionParametersBase.class,
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
