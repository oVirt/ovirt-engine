package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//import static org.mockito.Mockito.when;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.ovirt.engine.core.common.AuditLogType;
//import org.ovirt.engine.core.common.businessentities.AuditLog;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.dao.AuditLogDAO;


//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ AuditLogDirector.class })
//public class AuditLogDirectorTest {
//
//    @Mock
//    DbFacade dbFacade;
//    @Mock
//    AuditLogDAO auditLogDao;
//
//    @Before
//    public void initMocks() {
//        initAuditLogDirectorMock();
//        initDbFacadeMock();
//    }
//
//    private void initDbFacadeMock() {
//        when(dbFacade.getAuditLogDAO()).thenReturn(auditLogDao);
//    }
//
//    private void initAuditLogDirectorMock() {
//        PowerMockito.spy(AuditLogDirector.class);
//        PowerMockito.when(AuditLogDirector.getDbFacadeInstance()).thenReturn(dbFacade);
//    }
//
//    @Test
//    public void testPropertyLoading() {
//        AuditLogDirector.checkSeverities();
//    }
//
//    /**
//     * The test assures that audit loggable objects with timeout, which were created without an explicit log type, with
//     * a common key parts, except of the log type, are treated separately.<br>
//     * The test invokes two {@Code AuditLogDirector.log()} calls and verifies that each call insert an entry into
//     * the database.<br>
//     */
//    @Test
//    public void testLegalAuditLog() {
//        AuditLogableBase logableObject1 = new AuditLogableBase();
//        AuditLogDirector.log(logableObject1, AuditLogType.IRS_DISK_SPACE_LOW);
//
//        AuditLogableBase logableObject2 = new AuditLogableBase();
//        AuditLogDirector.log(logableObject2, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
//        Mockito.verify(auditLogDao, Mockito.times(2)).save(Mockito.any(AuditLog.class));
//    }
//
//    /**
//     * The test assures that audit loggable objects with timeout, which were created without an explicit log type and
//     * share the same key are treated in respect to each other by the timeout gaps between events.<br>
//     * The test invokes two {@Code AuditLogDirector.log()} calls and verify that only one call inserts an entry
//     * into the database.
//     */
//    @Test
//    public void testIllegalAuditLog() {
//        AuditLogableBase logableObject1 = new AuditLogableBase();
//        AuditLogDirector.log(logableObject1, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
//        Mockito.verify(auditLogDao, Mockito.times(1)).save(Mockito.any(AuditLog.class));
//
//        AuditLogDirector.log(logableObject1, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
//        Mockito.verify(auditLogDao, Mockito.times(1)).save(Mockito.any(AuditLog.class));
//    }
//
//    @Test
//    public void testResolveUnknownVariable() {
//        final String message = "This is my ${Variable}";
//        final String expectedResolved = "This is my <UNKNOWN>";
//        Map<String, String> values = Collections.emptyMap();
//        String resolvedMessage = AuditLogDirector.resolveMessage(message, values);
//        Assert.assertEquals(expectedResolved, resolvedMessage);
//    }
//
//    @Test
//    public void testResolveKnownVariable() {
//        final String message = "This is my ${Variable}";
//        final String expectedResolved = "This is my value";
//        Map<String, String> values = new HashMap<String, String>();
//        values.put("variable", "value");
//        String resolvedMessage = AuditLogDirector.resolveMessage(message, values);
//        Assert.assertEquals(expectedResolved, resolvedMessage);
//    }
//
// }
