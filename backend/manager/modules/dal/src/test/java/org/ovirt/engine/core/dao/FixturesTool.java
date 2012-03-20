package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.compat.Guid;

/**
 * A utility class for DAO testing which maps the fixtures entities to constants, for easy testing.
 */
public class FixturesTool {
    /**
     * Predefined NFS storage pool.
     */
    protected static final Guid STORAGE_POOL_NFS = new Guid("72b9e200-f48b-4687-83f2-62828f249a47");

    /**
     * Predefined ISCSI storage pool.
     */
    protected static final Guid STORAGE_POOL_RHEL6_ISCSI = new Guid("6d849ebf-755f-4552-ad09-9a090cda105");

    /**
     * Another predefined ISCSI storage pool.
     */
    protected static final Guid STORAGE_POOL_RHEL6_ISCSI_OTHER = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    /**
     * Predefined NFS master storage domain.
     */
    protected static final Guid STORAGE_DOAMIN_NFS_MASTER = new Guid("c2211b56-8869-41cd-84e1-78d7cb96f31d");

    /**
     * Predefined scale storage domain.
     */
    protected static final Guid STORAGE_DOAMIN_SCALE_SD5 = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");

    /**
     * Predefined vds group.
     */
    protected static final Guid VDS_GROUP_RHEL6_ISCSI = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    /**
     * Predefined vds group.
     */
    protected static final Guid VDS_GROUP_RHEL6_NFS = new Guid("0e57070e-2469-4b38-84a2-f111aaabd49d");

    /**
     * Predefined vds group with no running VMs
     */
    protected static final Guid VDS_GROUP_NO_RUNNING_VMS = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3");

    /**
     * Predefined NFS storage pool.
     */
    protected static final Guid VDS_RHEL6_NFS_SPM = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");

    /**
     * Predefined quota with general limitations. Its GUID is 88296e00-0cad-4e5a-9291-008a7b7f4399.<BR/>
     * The <code>Quota</code> has the following limitations:
     * <ul>Global limitation:
     * <li>virtual_cpu = 100</li>
     * <li>mem_size_mb = 10000</li>
     * <li>storage_size_gb = 1000000</li></ul>
     * Specific cluster ID c2211b56-8869-41cd-84e1-78d7cb96f31d(STORAGE_DOAMIN_NFS_MASTER) with no limitations
     */
    protected static final Guid QUOTA_GENERAL = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4399");

    /**
     * The default unlimited quota with quota id 88296e00-0cad-4e5a-9291-008a7b7f4404 fir storage pool rhel6.NFS
     * (72b9e200-f48b-4687-83f2-62828f249a47)
     */
    protected static final Guid DEFAULT_QUOTA_GENERAL = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4404");

    /**
     * Predefined quota with specific limitations, Its GUID is 88296e00-0cad-4e5a-9291-008a7b7f4400.
     * <ul>Global limitation:
     * <li>virtual_cpu = 100</li></ul>
     * Specific storage ID c2211b56-8869-41cd-84e1-78d7cb96f31d(STORAGE_DOAMIN_NFS_MASTER) <ul><li>storage_size_gb = 1000</li></ul>
     * Specific vdsGroup ID b399944a-81ab-4ec5-8266-e19ba7c3c9d1(VDS_GROUP_RHEL6_ISCSI) <ul><li>virtual_cpu = 10</li><li>mem_size_mb = -1</li></ul>
     * Specific vdsGroup ID 0e57070e-2469-4b38-84a2-f111aaabd49d(VDS_GROUP_RHEL6_NFS) <ul><li>virtual_cpu = null</li><li>mem_size_mb = -1</li></ul>
     */
    protected static final Guid QUOTA_SPECIFIC = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4400");

    /**
     * Predefined quota with general and specific limitations.
     */
    protected static final Guid QUOTA_SPECIFIC_AND_GENERAL = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4401");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-57</li>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li></ul>
     */
    protected static final Guid VM_RHEL5_POOL_57 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    /**
     * Predefined user for testing with the following properties :
     * <ul>
     * <li>Ad group id : 9bf7c640-b620-456f-a550-0348f366544b</li>
     * <li>Group name : philosophers</li>
     * <li>Status : 1</li>
     * <li>Domain : rhel</li>
     * <li>Role : jUnitTestRole</li>
     * <li>Action group ids : 4, 901</li></ul>
     */
    protected static final Guid USER_EXISTING_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");

    /**
     * Predefined image for testing.
     */
    protected static final Guid IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");

    /**
     * Predefined image group for testing.
     */
    protected static final Guid IMAGE_GROUP_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a35");
}
