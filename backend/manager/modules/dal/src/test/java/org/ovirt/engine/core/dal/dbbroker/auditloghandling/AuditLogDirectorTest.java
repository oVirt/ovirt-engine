package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AuditLogDirector.class, DbFacade.class })
public class AuditLogDirectorTest {

    @Mock
    DbFacade dbFacade;
    @Mock
    AuditLogDAO auditLogDao;

    @Before
    public void initMocks() {
        initAuditLogDirectorMock();
        initDbFacadeMock();
    }

    private void initDbFacadeMock() {
        when(dbFacade.getAuditLogDAO()).thenReturn(auditLogDao);
    }

    private void initAuditLogDirectorMock() {
        spy(AuditLogDirector.class);
        when(AuditLogDirector.getDbFacadeInstance()).thenReturn(dbFacade);
    }

    @Test
    public void testPropertyLoading() {
        AuditLogDirector.checkSeverities();
    }

    /**
     * The test assures that audit loggable objects with timeout, which were created without an explicit log type, with
     * a common key parts, except of the log type, are treated separately.<br>
     * The test invokes two {@Code AuditLogDirector.log()} calls and verifies that each call insert an entry into
     * the database.<br>
     */
    @Test
    public void testLegalAuditLog() {
        AuditLogableBase logableObject1 = new AuditLogableBase();
        AuditLogDirector.log(logableObject1, AuditLogType.IRS_DISK_SPACE_LOW);

        AuditLogableBase logableObject2 = new AuditLogableBase();
        AuditLogDirector.log(logableObject2, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        Mockito.verify(auditLogDao, Mockito.times(2)).save(Mockito.any(AuditLog.class));
    }

    /**
     * The test assures that audit loggable objects with timeout, which were created without an explicit log type and
     * share the same key are treated in respect to each other by the timeout gaps between events.<br>
     * The test invokes two {@Code AuditLogDirector.log()} calls and verify that only one call inserts an entry
     * into the database.
     */
    @Test
    public void testIllegalAuditLog() {
        AuditLogableBase logableObject1 = new AuditLogableBase();
        AuditLogDirector.log(logableObject1, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        Mockito.verify(auditLogDao, Mockito.times(1)).save(Mockito.any(AuditLog.class));

        AuditLogDirector.log(logableObject1, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        Mockito.verify(auditLogDao, Mockito.times(1)).save(Mockito.any(AuditLog.class));
    }

    @Test
    public void testResolveUnknownVariable() {
        final String message = "This is my ${Variable}";
        final String expectedResolved = "This is my <UNKNOWN>";
        Map<String, String> values = MapUtils.EMPTY_MAP;
        String resolvedMessage = AuditLogDirector.resolveMessage(message, values);
        Assert.assertEquals(expectedResolved, resolvedMessage);
    }

    @Test
    public void testResolveKnownVariable() {
        final String message = "This is my ${Variable}";
        final String expectedResolved = "This is my value";
        Map<String, String> values = new HashMap<String, String>();
        values.put("variable", "value");
        String resolvedMessage = AuditLogDirector.resolveMessage(message, values);
        Assert.assertEquals(expectedResolved, resolvedMessage);
    }

}
