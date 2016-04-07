package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AuditLogDaoTest</code> performs tests against the {@link AuditLogDao} type.
 */
public class AuditLogDaoTest extends BaseDaoTestCase {
    private static final String VM_NAME = "rhel5-pool-50";
    private static final String VM_TEMPLATE_NAME = "1";
    private static final Guid VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final String VDS_NAME = "magenta-vdsc";
    private static final Guid VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid VM_TEMPLATE_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid GLUSTER_VOLUME_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final long EXISTING_ENTRY_ID = 44291;
    private static final long EXTERNAL_ENTRY_ID = 44297;
    private static final int FILTERED_COUNT = 6;
    private static final int AFTER_DATE_COUNT = 7;
    private static final int TOTAL_COUNT = 8;
    private static final String ORIGIN="oVirt";
    private static final int CUSTOM_BAKUP_EVENT_ID = 9022;
    private AuditLogDao dao;

    /** Note that {@link SimpleDateFormat} is inherently not thread-safe, and should not be static */
    private final SimpleDateFormat EXPECTED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AuditLog newAuditLog;
    private AuditLog existingAuditLog;
    private AuditLog externalAuditLog;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getAuditLogDao();

        // create some test data
        newAuditLog = new AuditLog();
        newAuditLog.setAuditLogId(44000);
        newAuditLog.setUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));
        newAuditLog.setUserName("userportal3");
        newAuditLog.setVmId(VM_ID);
        newAuditLog.setVmName(VM_NAME);
        newAuditLog.setVmTemplateId(VM_TEMPLATE_ID);
        newAuditLog.setVmTemplateName(VM_TEMPLATE_NAME);
        newAuditLog.setVdsId(VDS_ID);
        newAuditLog.setVdsName("magenta-vdsc");
        newAuditLog.setLogTime(EXPECTED_DATE_FORMAT.parse("2010-12-22 14:00:00"));
        newAuditLog.setLogType(AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        newAuditLog.setSeverity(AuditLogSeverity.ERROR);
        newAuditLog.setMessage("Critical, Low disk space.  domain has 1 GB of free space");
        newAuditLog.setStoragePoolId(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        newAuditLog.setStoragePoolName("rhel6.iscsi");
        newAuditLog.setStorageDomainId(new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab"));
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
        assertNotNull("Results object should not be null", results);
        assertEquals("Wrong number of results", expectedResults, results.size());

        for (AuditLog auditLog : results) {
            assertEquals("Wrong name of VM in result", VM_NAME, auditLog.getVmName());
            assertEquals("Wrong template name of VM in result", VM_TEMPLATE_NAME, auditLog.getVmTemplateName());
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
    public void testRemoveAllForVds()
            throws Exception {
        dao.removeAllForVds(VDS_ID, true);
        List<AuditLog> result = dao.getAll(null, false);
        assertEquals(7, result.size());
    }

    @Test
    public void testRemoveAllOfTypeForVds()
            throws Exception {
        dao.removeAllOfTypeForVds(VDS_ID,
                AuditLogType.IRS_DISK_SPACE_LOW_ERROR.getValue());
        // show be 1 left that was in event_notification_hist
        List<AuditLog> result = dao.getAll(PRIVILEGED_USER_ID, true);
        assertEquals(3, result.size());
    }

    /**
     * Ensures that saving a AuditLog works as expected.
     * <strong>Note:</strong> Since inserting a new AuditLog autogenerates its
     * ID, we cannot rely on the {@link AuditLogDao#get(long)} method.
     * Instead, we'll use the {@link AuditLogDao#getAllAfterDate(Date)} method
     * to check that a new one was added.
     */
    @Test
    public void testSave() {
        Date newAuditLogDateCuttoff = newAuditLog.getLogTime();
        newAuditLogDateCuttoff.setTime(newAuditLogDateCuttoff.getTime() - 1);
        int countBefore = dao.getAllAfterDate(newAuditLogDateCuttoff).size();

        dao.save(newAuditLog);

        int countAfter = dao.getAllAfterDate(newAuditLogDateCuttoff).size();
        assertEquals(countBefore + 1, countAfter);
    }

    /**
     * Ensures that saving a AuditLog with long message works as expected.
     * <strong>Note:</strong> Since inserting a new AuditLog autogenerates its
     * ID, we cannot rely on the {@link AuditLogDao#get(long)} method.
     * Instead, we'll use the {@link AuditLogDao#getAllAfterDate(Date)} method
     * to check that a new one was added.
     */
    @Test
    public void testLongMessageSave() {
        Date newAuditLogDateCuttoff = newAuditLog.getLogTime();
        newAuditLogDateCuttoff.setTime(newAuditLogDateCuttoff.getTime() - 1);
        List<AuditLog> before = dao.getAllAfterDate(newAuditLogDateCuttoff);

        // generate a value that is longer than the max configured.
        char[] fill = new char[Config.<Integer> getValue(ConfigValues.MaxAuditLogMessageLength) + 1];
        Arrays.fill(fill, '0');
        newAuditLog.setAuditLogId(45000);
        newAuditLog.setMessage(new String(fill));
        newAuditLog.setExternal(true);
        dao.save(newAuditLog);

        List<AuditLog> after = dao.getAllAfterDate(newAuditLogDateCuttoff);
        after.removeAll(before);

        assertEquals(1, after.size());
        AuditLog result = after.get(0);
        assertNotNull(result);
        assertTrue(result.getMessage().endsWith("..."));
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


    private int getAlertCount(AuditLog entry, List<AuditLog> results) {
        int count = 0;
        if (results != null) {
            for (AuditLog al : results) {
                if (al.getSeverity() == entry.getSeverity()
                        && al.getVdsId() != null
                        && al.getVdsId().equals(entry.getVdsId())
                        && al.getLogType() == entry.getLogType()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if multiple alerts of the same type for the same host are ignored if repeatable is set to {@code false}
     */
    @Test
    public void testMultipleAlertsWithSameTypeAndHostAreIgnored() {
        AuditLog entry = new AuditLog(AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY, AuditLogSeverity.ALERT);
        entry.setVdsId(VDS_ID);
        entry.setVdsName(VDS_NAME);
        entry.setMessage("Testing alert");

        // test if no alert of the same type for the same host exists
        assertEquals(0, getAlertCount(entry, dao.getAll(null, false)));

        dao.save(entry);

        // test if 1st alert was stored in db
        assertEquals(1, getAlertCount(entry, dao.getAll(null, false)));

        // try to store 2nd alert in db
        entry.setLogTime(new Date());
        dao.save(entry);

        // test if 2nd alert was ignored
        assertEquals(1, getAlertCount(entry, dao.getAll(null, false)));
    }

    /**
     * Checks if multiple alerts of the same type for the same host are saved if repeatable is set to {@code true}
     */
    @Test
    public void testMultipleAlertsWithSameTypeAndHostAreSavedIfRepeatableTrue() {
        AuditLog entry = new AuditLog(AuditLogType.VDS_ALERT_FENCE_DISABLED_BY_CLUSTER_POLICY, AuditLogSeverity.ALERT);
        entry.setVdsId(VDS_ID);
        entry.setVdsName(VDS_NAME);
        entry.setMessage("Testing alert");
        entry.setRepeatable(true);

        // test if no alert of the same type for the same host exists
        assertEquals(0, getAlertCount(entry, dao.getAll(null, false)));

        dao.save(entry);

        // test if 1st alert was stored in db
        assertEquals(1, getAlertCount(entry, dao.getAll(null, false)));

        // try to save 2nd alert
        entry.setLogTime(new Date());
        dao.save(entry);

        // test if 2nd alert was also stored in db
        assertEquals(2, getAlertCount(entry, dao.getAll(null, false)));
    }

    @Test
    public void testDeleteBackupRelatedAlerts() {
        AuditLog entry = dao.getByOriginAndCustomEventId(ORIGIN, CUSTOM_BAKUP_EVENT_ID);
        assertNotNull(entry);
        assertFalse(entry.isDeleted());
        dao.deleteBackupRelatedAlerts();
        entry = dao.getByOriginAndCustomEventId(ORIGIN, CUSTOM_BAKUP_EVENT_ID);
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
