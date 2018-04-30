package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigExtension;

/**
 * {@code AuditLogDaoTest} performs tests against the {@link AuditLogDao} type.
 */
@ExtendWith(MockConfigExtension.class)
public class AuditLogDaoTest extends BaseDaoTestCase<AuditLogDao> {
    private static final String VM_NAME = FixturesTool.VM_RHEL5_POOL_50_NAME;
    private static final String VM_TEMPLATE_NAME = "1";
    private static final Guid VM_ID = FixturesTool.VM_RHEL5_POOL_50;
    private static final Guid VM_TEMPLATE_ID = FixturesTool.VM_TEMPLATE_RHEL5;
    private static final Guid GLUSTER_VOLUME_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final long EXISTING_ENTRY_ID = 44291;
    private static final long EXTERNAL_ENTRY_ID = 44297;
    private static final int FILTERED_COUNT = 6;
    private static final int AFTER_DATE_COUNT = 7;
    private static final int TOTAL_COUNT = 8;
    private static final int CUSTOM_BAKUP_EVENT_ID = 9022;

    /** Note that {@link SimpleDateFormat} is inherently not thread-safe, and should not be static */
    private final SimpleDateFormat EXPECTED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AuditLog newAuditLog;
    private AuditLog existingAuditLog;
    private AuditLog externalAuditLog;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // create some test data
        newAuditLog = new AuditLog();
        newAuditLog.setAuditLogId(44000);
        newAuditLog.setUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));
        newAuditLog.setUserName("userportal3");
        newAuditLog.setVmId(VM_ID);
        newAuditLog.setVmName(VM_NAME);
        newAuditLog.setVmTemplateId(VM_TEMPLATE_ID);
        newAuditLog.setVmTemplateName(VM_TEMPLATE_NAME);
        newAuditLog.setVdsId(FixturesTool.VDS_RHEL6_NFS_SPM);
        newAuditLog.setVdsName(FixturesTool.GLUSTER_SERVER_NAME3);
        newAuditLog.setLogTime(EXPECTED_DATE_FORMAT.parse("2010-12-22 14:00:00"));
        newAuditLog.setLogType(AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        newAuditLog.setSeverity(AuditLogSeverity.ERROR);
        newAuditLog.setMessage("Critical, Low disk space.  domain has 1 GB of free space");
        newAuditLog.setStoragePoolId(FixturesTool.DATA_CENTER);
        newAuditLog.setStoragePoolName("rhel6.iscsi");
        newAuditLog.setStorageDomainId(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        newAuditLog.setStorageDomainName("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk");
        newAuditLog.setQuotaId(FixturesTool.DEFAULT_QUOTA_GENERAL);
        newAuditLog.setQuotaName("General Quota");
        newAuditLog.setGlusterVolumeId(new Guid("0e0abdbc-2a0f-4df0-8b99-cc577a7a9bb5"));
        newAuditLog.setGlusterVolumeName("gluster_volume_name-1");

        existingAuditLog = dao.get(EXISTING_ENTRY_ID);
        externalAuditLog = dao.get(EXTERNAL_ENTRY_ID);
    }

    /**
     * Ensures that if the id is invalid then no AuditLog is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        AuditLog result = dao.get(7);

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a AuditLog works as expected.
     */
    @Test
    public void testGet() {
        AuditLog result = dao.get(44291);

        assertNotNull(result);
        assertEquals(existingAuditLog, result);
    }

    /**
     * Ensures that, for External Events, then retrieving a AuditLog works as expected.
     */
    @Test
    public void testGetByOriginAndCustomEventId() {
        AuditLog result = dao.getByOriginAndCustomEventId("EMC", 1);

        assertNotNull(result);
        assertEquals(externalAuditLog, result);
    }

    /**
     * Ensures that finding all AuditLog works as expected.
     */
    @Test
    public void testGetAll() {
        List<AuditLog> result = dao.getAll(null, false);

        assertEquals(TOTAL_COUNT, result.size());
    }

    @Test
    public void testGetAllFiltered() {
        List<AuditLog> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertEquals(FILTERED_COUNT, result.size());
    }


    /**
     * Test date filtering
     */
    @Test
    public void testGetAllAfterDate()
            throws Exception {
        Date cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 13:00:00");

        List<AuditLog> result = dao.getAllAfterDate(cutoff);

        assertNotNull(result);
        assertEquals(AFTER_DATE_COUNT, result.size());

        cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 14:00:00");

        result = dao.getAllAfterDate(cutoff);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /** Tests {@link AuditLogDao#getAllByVMId(Guid)} with a name of a VM that exists */
    @Test
    public void testGetAllByVMId() {
        assertGetByNameValidResults(dao.getAllByVMId(VM_ID));
    }

    /** Tests {@link AuditLogDao#getAllByVMId(Guid)} with an ID of a VM that doesn't exist */
    @Test
    public void testGetAllByVMIdInvalidId() {
        assertGetByNameInvalidResults(dao.getAllByVMId(Guid.newGuid()));
    }

    /** Tests {@link AuditLogDao#getAllByVMId(Guid, Guid, boolean)} with a user that has permissions on that VM */
    @Test
    public void testGetAllByVMIdPrivilegedUser() {
        assertGetByNameValidResults(dao.getAllByVMId(VM_ID, PRIVILEGED_USER_ID, true));
    }

    /** Tests {@link AuditLogDao#getAllByVMId(Guid, Guid, boolean)} with a user that doesn't have permissions on that VM, but with the filtering mechanism disabled */
    @Test
    public void testGetAllByVMNameUnprivilegedUserNoFiltering() {
        assertGetByNameValidResults(dao.getAllByVMId(VM_ID, UNPRIVILEGED_USER_ID, false));
    }

    /** Tests {@link AuditLogDao#getAllByVMId(Guid, Guid, boolean)} with a user that doesn't have permissions on that VM */
    @Test
    public void testGetAllByVMNameUnprivilegedUserFiltering() {
        assertGetByNameInvalidResults(dao.getAllByVMId(VM_ID, UNPRIVILEGED_USER_ID, true));
    }

    /** Tests {@link AuditLogDao#getAllByVMTemplateId(Guid)} with an ID of a VM Template that exists */
    @Test
    public void testGetAllByVMTemplateName() {
        assertGetByNameValidResults(dao.getAllByVMTemplateId(VM_TEMPLATE_ID));
    }

    /** Tests {@link AuditLogDao#getAllByVMTemplateId(Guid)} with a an ID of a VM Template that doesn't exist */
    @Test
    public void testGetAllByVMTemplateIdInvalidId() {
        assertGetByNameInvalidResults(dao.getAllByVMTemplateId(Guid.newGuid()));
    }

    /** Tests {@link AuditLogDao#getAllByVMTemplateId(Guid, Guid, boolean)} with a user that has permissions on that VM Template */
    @Test
    public void testGetAllByVMTemplateIdPrivilegedUser() {
        assertGetByNameValidResults(dao.getAllByVMTemplateId(VM_TEMPLATE_ID, PRIVILEGED_USER_ID, true));
    }

    /** Tests {@link AuditLogDao#getAllByVMTemplateId(Guid, Guid, boolean)} with a user that doesn't have permissions on that VM Template, but with the filtering mechanism disabled */
    @Test
    public void testGetAllByVMTemplateIdUnprivilegedUserNoFiltering() {
        assertGetByNameValidResults(dao.getAllByVMTemplateId(VM_TEMPLATE_ID, UNPRIVILEGED_USER_ID, false));
    }

    /** Tests {@link AuditLogDao#getAllByVMTemplateId(Guid, Guid, boolean)} with a user that doesn't have permissions on that VM Template */
    @Test
    public void testGetAllByVMTemplateIdUnprivilegedUserFiltering() {
        assertGetByNameInvalidResults(dao.getAllByVMTemplateId(VM_TEMPLATE_ID, UNPRIVILEGED_USER_ID, true));
    }

    private static void assertGetByNameValidResults(List<AuditLog> results) {
        assertGetByNameResults(results, FILTERED_COUNT);
    }

    private static void assertGetByNameInvalidResults(List<AuditLog> results) {
        assertGetByNameResults(results, 0);
    }

    private static void assertGetByNameResults(List<AuditLog> results, int expectedResults) {
        assertNotNull(results, "Results object should not be null");
        assertEquals(expectedResults, results.size(), "Wrong number of results");

        for (AuditLog auditLog : results) {
            assertEquals(VM_NAME, auditLog.getVmName(), "Wrong name of VM in result");
            assertEquals(VM_TEMPLATE_NAME, auditLog.getVmTemplateName(), "Wrong template name of VM in result");
        }
    }

    /**
     * Test query
     */
    @Test
    public void testGetAllWithQuery() {
        List<AuditLog> result = dao.getAllWithQuery("SELECT * FROM audit_log WHERE vds_name = 'magenta-vdsc'");

        assertEquals(FILTERED_COUNT, result.size());
    }

    @Test
    public void testRemoveAllBeforeDate()
            throws Exception {
        Date cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 13:11:00");
        dao.removeAllBeforeDate(cutoff);
        List<AuditLog> result = dao.getAll(PRIVILEGED_USER_ID, true);
        assertEquals(1, result.size());
    }

    @Test
    public void testRemoveAllForVds() {
        dao.removeAllForVds(FixturesTool.VDS_RHEL6_NFS_SPM, true);
        List<AuditLog> result = dao.getAll(null, false);
        assertEquals(7, result.size());
    }

    @Test
    public void testRemoveAllOfTypeForVds() {
        dao.removeAllOfTypeForVds(FixturesTool.VDS_RHEL6_NFS_SPM,
                AuditLogType.IRS_DISK_SPACE_LOW_ERROR.getValue());
        // show be 1 left that was in event_notification_hist
        List<AuditLog> result = dao.getAll(PRIVILEGED_USER_ID, true);
        assertEquals(3, result.size());
    }

    /**
     * Ensures that saving a AuditLog works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newAuditLog);
        AuditLog result = dao.get(newAuditLog.getAuditLogId());
        assertNotNull(result);
        assertEquals(newAuditLog, result);
    }

    @Test
    public void testSaveExternalEvent() {
        AuditLog newExternalEvent = new AuditLog();
        newExternalEvent.setLogType(AuditLogType.EXTERNAL_EVENT_NORMAL);
        newExternalEvent.setExternal(true);
        newExternalEvent.setOrigin("XYZ");
        newExternalEvent.setCustomEventId(123123123);
        newExternalEvent.setCustomData("Some text here");
        newExternalEvent.setMessage("And here");
        newExternalEvent.setEventFloodInSec(100);

        dao.save(newExternalEvent);
        AuditLog result = dao.get(newExternalEvent.getAuditLogId());

        assertNotNull(result);
        assertTrue(result.getAuditLogId() > 0);
        assertEquals(newExternalEvent.getOrigin(), result.getOrigin());
        assertEquals(newExternalEvent.getCustomEventId(), result.getCustomEventId());
        assertEquals(newExternalEvent.getCustomData(), result.getCustomData());
        assertEquals(newExternalEvent.getMessage(), result.getMessage());
        assertEquals(newExternalEvent.getEventFloodInSec(), result.getEventFloodInSec());
    }

    /**
     * Ensures that removing an AuditLog works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingAuditLog.getAuditLogId());

        AuditLog result = dao.get(existingAuditLog.getAuditLogId());

        assertTrue(result.isDeleted());
    }


    private long getAlertCount(AuditLog entry, List<AuditLog> results) {
        return results.stream()
                .filter(a -> a.getSeverity() == entry.getSeverity() &&
                        entry.getVdsId().equals(a.getVdsId()) &&
                        a.getLogType() == entry.getLogType())
                .count();
    }

    /**
     * Checks if multiple alerts of the same type for the same host are ignored if repeatable is set to {@code false}
     */
    @Test
    public void testMultipleAlertsWithSameTypeAndHostAreIgnored() {
        AuditLog entry = new AuditLog(AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY, AuditLogSeverity.ALERT);
        entry.setVdsId(FixturesTool.VDS_RHEL6_NFS_SPM);
        entry.setVdsName(FixturesTool.GLUSTER_SERVER_NAME3);
        entry.setMessage("Testing alert");

        // test if no alert of the same type for the same host exists
        assertEquals(0L, getAlertCount(entry, dao.getAll(null, false)));

        dao.save(entry);
        AuditLog savedAlert = dao.get(entry.getAuditLogId());
        assertNotNull(savedAlert);

        // test if 1st alert was stored in db
        assertEquals(1L, getAlertCount(entry, dao.getAll(null, false)));

        // try to store 2nd alert in db
        entry.setLogTime(new Date());
        dao.save(entry);
        savedAlert = dao.get(entry.getAuditLogId());
        assertNotNull(savedAlert);

        // test if 2nd alert was ignored
        assertEquals(1L, getAlertCount(entry, dao.getAll(null, false)));
    }

    /**
     * Checks if multiple alerts of the same type for the same host are saved if repeatable is set to {@code true}
     */
    @Test
    public void testMultipleAlertsWithSameTypeAndHostAreSavedIfRepeatableTrue() {
        AuditLog entry = new AuditLog(AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY, AuditLogSeverity.ALERT);
        entry.setVdsId(FixturesTool.VDS_RHEL6_NFS_SPM);
        entry.setVdsName(FixturesTool.GLUSTER_SERVER_NAME3);
        entry.setMessage("Testing alert");
        entry.setRepeatable(true);

        // test if no alert of the same type for the same host exists
        assertEquals(0L, getAlertCount(entry, dao.getAll(null, false)));

        dao.save(entry);
        AuditLog savedAlert = dao.get(entry.getAuditLogId());
        assertNotNull(savedAlert);

        // test if 1st alert was stored in db
        assertEquals(1L, getAlertCount(entry, dao.getAll(null, false)));

        // try to save 2nd alert
        entry.setLogTime(new Date());
        dao.save(entry);
        savedAlert = dao.get(entry.getAuditLogId());
        assertNotNull(savedAlert);

        // test if 2nd alert was also stored in db
        assertEquals(2L, getAlertCount(entry, dao.getAll(null, false)));
    }

    @Test
    public void testDeleteBackupRelatedAlerts() {
        AuditLog entry = dao.getByOriginAndCustomEventId(AuditLog.OVIRT_ORIGIN, CUSTOM_BAKUP_EVENT_ID);
        assertNotNull(entry);
        assertFalse(entry.isDeleted());
        dao.deleteBackupRelatedAlerts();
        entry = dao.getByOriginAndCustomEventId(AuditLog.OVIRT_ORIGIN, CUSTOM_BAKUP_EVENT_ID);
        assertNotNull(entry);
        assertTrue(entry.isDeleted());
    }

    @Test
    public void testRemoveAllOfTypeForVolume() {
        List<AuditLog> entries =
                dao.getByVolumeIdAndType(GLUSTER_VOLUME_ID,
                        AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED.getValue());

        assertEquals(1, entries.size());

        dao.removeAllOfTypeForVolume(GLUSTER_VOLUME_ID,
                AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED.getValue());

        List<AuditLog> entries1 =
                dao.getByVolumeIdAndType(GLUSTER_VOLUME_ID,
                        AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED.getValue());

        assertEquals(1, entries1.size());
        assertEquals(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED, entries1.get(0).getLogType());
    }

    @Test
    public void testGetByVolumeIdAndType() {
        List<AuditLog> entries =
                dao.getByVolumeIdAndType(GLUSTER_VOLUME_ID,
                        AuditLogType.GLUSTER_VOLUME_SNAPSHOT_SOFT_LIMIT_REACHED.getValue());

        assertEquals(1, entries.size());
    }
}
