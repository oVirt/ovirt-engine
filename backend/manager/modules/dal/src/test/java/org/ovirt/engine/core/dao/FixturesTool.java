package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.compat.Guid;

/**
 * A util class for DAO testing which maps the fixtures entities to constants, for easy testing.
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
}
