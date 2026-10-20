package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * Tests for the i440fx migration script (04_05_0370_migrate_i440fx_vms_and_cluster_defaults.sql).
 *
 * This migration pins i440fx VM emulated machines and rewrites cluster defaults to Q35-only,
 * limited to clusters at compatibility level >= 4.8. Steps tested:
 * 1. Running i440fx VMs: copy vm_dynamic.emulated_machine -> vm_static.custom_emulated_machine
 * 2. Down i440fx VMs: extract i440fx from cluster.emulated_machine
 * 2b. i440fx templates: extract i440fx from cluster.emulated_machine (templates are vm_static
 *     rows with entity_type = 'TEMPLATE' — there is no separate vm_templates table)
 * 3. Cluster defaults: rewrite dual-format to Q35-only
 */
public class I440fxMigrationScriptTest extends BaseMigrationScriptTest<VmStaticDao> {

    // Test fixture GUIDs (defined in fixtures.xml)
    private static final Guid TEST_CLUSTER_ID = new Guid("e14f0f1c-0000-0000-0000-000000000001");
    private static final Guid RUNNING_VM_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000a1");
    private static final Guid DOWN_VM_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000a2");
    private static final Guid TEMPLATE_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000a3");

    // The below-4.8 cluster (compatibility_version 4.7): the migration must be a no-op for it
    private static final Guid BELOW48_CLUSTER_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000b1");
    private static final Guid BELOW48_RUNNING_VM_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000b2");
    private static final Guid BELOW48_DOWN_VM_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000b3");
    private static final Guid BELOW48_TEMPLATE_ID = new Guid("e14f0f1c-0000-0000-0000-0000000000b4");

    @Inject
    private ClusterDao clusterDao;

    /**
     * Reads custom_emulated_machine straight from vm_static. Unlike {@code dao.get()},
     * which is backed by GetVmStaticByVmGuid (entity_type = 'VM' only), this also
     * reaches template rows (entity_type = 'TEMPLATE').
     */
    private String getCustomEmulatedMachine(Guid vmGuid) throws SQLException {
        try (Connection con = BaseDaoTestCase.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "select custom_emulated_machine from vm_static where vm_guid = ?")) {
            ps.setObject(1, vmGuid.getUuid());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                // normalise like VmBase.setCustomEmulatedMachine: empty string means unset
                String value = rs.getString(1);
                return value == null || value.isEmpty() ? null : value;
            }
        }
    }

    @Override
    protected String getMigrationScriptName() {
        return "04_05_0370_migrate_i440fx_vms_and_cluster_defaults.sql";
    }

    @Test
    void testMigrationPinsRunningVmFromVmDynamic() throws Exception {
        // Pre-condition: running VM has empty custom_emulated_machine
        VmStatic vmBefore = dao.get(RUNNING_VM_ID);
        assertNotNull(vmBefore, "Running VM should exist in fixtures");
        assertNull(vmBefore.getCustomEmulatedMachine(),
                "Running VM should start with null custom_emulated_machine");
        assertEquals(1, vmBefore.getBiosType().getValue(),
                "Running VM should have bios_type=1 (i440fx)");

        // Execute migration
        executeMigrationScript();

        // Post-condition: running VM should be pinned from vm_dynamic
        VmStatic vmAfter = dao.get(RUNNING_VM_ID);
        assertEquals("pc-i440fx-rhel7.6.0", vmAfter.getCustomEmulatedMachine(),
                "Running VM should have custom_emulated_machine copied from vm_dynamic");
    }

    @Test
    void testMigrationPinsDownVmFromCluster() throws Exception {
        // Pre-condition: down VM has empty custom_emulated_machine
        VmStatic vmBefore = dao.get(DOWN_VM_ID);
        assertNotNull(vmBefore, "Down VM should exist in fixtures");
        assertNull(vmBefore.getCustomEmulatedMachine(),
                "Down VM should start with null custom_emulated_machine");
        assertEquals(1, vmBefore.getBiosType().getValue(),
                "Down VM should have bios_type=1 (i440fx)");

        // Execute migration
        executeMigrationScript();

        // Post-condition: down VM should be pinned from cluster.emulated_machine
        VmStatic vmAfter = dao.get(DOWN_VM_ID);
        assertEquals("pc-i440fx-rhel7.6.0", vmAfter.getCustomEmulatedMachine(),
                "Down VM should have custom_emulated_machine extracted from cluster default");
    }

    @Test
    void testMigrationPinsTemplateFromCluster() throws Exception {
        // Templates are rows in vm_static with entity_type = 'TEMPLATE', so Step 2b of the
        // migration script (which filters vm_static on entity_type = 'TEMPLATE') is directly
        // testable with these fixtures. dao.get() cannot be used here because
        // GetVmStaticByVmGuid only returns entity_type = 'VM' rows.
        assertNull(getCustomEmulatedMachine(TEMPLATE_ID),
                "Template should start with null custom_emulated_machine");

        executeMigrationScript();

        assertEquals("pc-i440fx-rhel7.6.0", getCustomEmulatedMachine(TEMPLATE_ID),
                "Template should have custom_emulated_machine extracted from cluster default");
    }

    @Test
    void testMigrationRewritesClusterDefaultToQ35Only() throws Exception {
        // Pre-condition: cluster has dual-format emulated_machine
        Cluster clusterBefore = clusterDao.get(TEST_CLUSTER_ID);
        assertNotNull(clusterBefore, "Test cluster should exist");
        assertEquals("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0", clusterBefore.getEmulatedMachine(),
                "Cluster should start with dual-format emulated_machine");

        // Execute migration
        executeMigrationScript();

        // Post-condition: cluster should have Q35-only default
        Cluster clusterAfter = clusterDao.get(TEST_CLUSTER_ID);
        assertEquals("pc-q35-rhel9.6.0", clusterAfter.getEmulatedMachine(),
                "Cluster should have Q35-only emulated_machine after migration");
        assertFalse(clusterAfter.getEmulatedMachine().contains(";"),
                "Cluster emulated_machine should not contain semicolon");
        assertFalse(clusterAfter.getEmulatedMachine().contains("i440fx"),
                "Cluster emulated_machine should not contain i440fx");
    }

    @Test
    void testMigrationAllStepsTogether() throws Exception {
        // Execute full migration
        executeMigrationScript();

        // Verify VM and cluster steps completed
        // Step 1: Running VM pinned from vm_dynamic
        VmStatic runningVm = dao.get(RUNNING_VM_ID);
        assertEquals("pc-i440fx-rhel7.6.0", runningVm.getCustomEmulatedMachine(),
                "Running VM should be pinned from vm_dynamic");

        // Step 2: Down VM pinned from cluster
        VmStatic downVm = dao.get(DOWN_VM_ID);
        assertEquals("pc-i440fx-rhel7.6.0", downVm.getCustomEmulatedMachine(),
                "Down VM should be pinned from cluster");

        // Step 2b: Template pinned from cluster (via direct SQL: template rows are
        // invisible to VmStaticDao.get)
        assertEquals("pc-i440fx-rhel7.6.0", getCustomEmulatedMachine(TEMPLATE_ID),
                "Template should be pinned from cluster");

        // Step 3: Cluster default rewritten to Q35-only
        Cluster cluster = clusterDao.get(TEST_CLUSTER_ID);
        assertEquals("pc-q35-rhel9.6.0", cluster.getEmulatedMachine(),
                "Cluster should have Q35-only default");
    }

    @Test
    void testMigrationWritesSetupAuditRow() throws Exception {
        // One CLUSTER_I440FX_SETUP_MIGRATED summary row per migrated cluster,
        // written by the migration itself (the engine is not running during setup).
        // The 4.8 fixture cluster has no hosts, so its pins count as unsupported: the
        // row escalates to WARNING severity and the message names the dead pins.
        executeMigrationScript();

        try (Connection con = BaseDaoTestCase.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "select log_type, severity, message from audit_log"
                     + " where cluster_id = ? and log_type = ?")) {
            ps.setObject(1, TEST_CLUSTER_ID.getUuid());
            ps.setInt(2, AuditLogType.CLUSTER_I440FX_SETUP_MIGRATED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "the migrated cluster must get a setup audit row");
                assertEquals(1, rs.getInt(2),
                        "pins no host supports must escalate the row to WARNING severity");
                assertTrue(rs.getString(3).contains("pinned 3"),
                        "message must state the pinned count, was: " + rs.getString(3));
                assertTrue(rs.getString(3).contains("3 of them to machine types no current host supports"),
                        "message must call out the unsupported pins, was: " + rs.getString(3));
                assertFalse(rs.next(), "exactly one summary row per cluster");
            }
        }

        // The below-4.8 cluster is untouched -> no audit row for it either.
        try (Connection con = BaseDaoTestCase.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "select count(*) from audit_log where cluster_id = ? and log_type = ?")) {
            ps.setObject(1, BELOW48_CLUSTER_ID.getUuid());
            ps.setInt(2, AuditLogType.CLUSTER_I440FX_SETUP_MIGRATED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals(0, rs.getInt(1), "below-4.8 cluster must get no setup audit row");
            }
        }
    }

    @Test
    void testMigrationLeavesBelow48ClusterUntouched() throws Exception {
        // Clusters below 4.8 keep the legacy dual-format default and their unpinned i440fx
        // VMs/templates — they are migrated when an admin raises the level, not at setup.
        executeMigrationScript();

        // Running VM: not pinned (would have been pinned from vm_dynamic at >= 4.8)
        assertNull(getCustomEmulatedMachine(BELOW48_RUNNING_VM_ID),
                "running VM of a below-4.8 cluster must stay unpinned");
        // Down VM: not pinned
        assertNull(getCustomEmulatedMachine(BELOW48_DOWN_VM_ID),
                "down VM of a below-4.8 cluster must stay unpinned");
        // Template: not pinned
        assertNull(getCustomEmulatedMachine(BELOW48_TEMPLATE_ID),
                "template of a below-4.8 cluster must stay unpinned");
        // Cluster default: still the dual format, detection flag untouched
        Cluster below48Cluster = clusterDao.get(BELOW48_CLUSTER_ID);
        assertEquals("pc-i440fx-rhel7.6.0;pc-q35-rhel9.6.0", below48Cluster.getEmulatedMachine(),
                "below-4.8 cluster must keep its dual-format default");
        assertFalse(below48Cluster.isDetectEmulatedMachine(),
                "below-4.8 cluster must keep its detection flag off");
    }
}
