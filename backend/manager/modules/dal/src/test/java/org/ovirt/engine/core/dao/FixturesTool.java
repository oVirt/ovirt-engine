package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

/**
 * A utility class for Dao testing which maps the fixtures entities to constants, for easy testing.
 */
public class FixturesTool {
    /**
     * Predefined NFS storage pool.
     */
    public static final Guid STORAGE_POOL_NFS = new Guid("72b9e200-f48b-4687-83f2-62828f249a47");

    /**
     * Predefined NFS storage pool with a single inactive ISO domain.
     */
    public static final Guid STORAGE_POOL_NFS_INACTIVE_ISO = new Guid("6d849ebf-755f-4552-ad09-9a090cda105c");

    /**
     * Predefined mixed types storage pool.
     */
    public static final Guid STORAGE_POOL_MIXED_TYPES = new Guid("386bffd1-e7ed-4b08-bce9-d7df10f8c9a0");

    /**
     * Predefined storage pool with no domains.
     */
    public static final Guid STORAGE_POOL_NO_DOMAINS = new Guid("d9220003-8ad8-41d2-8c9e-5ea30b2e88e7");

    /**
     * Predefined ISCSI storage pool.
     */
    protected static final Guid STORAGE_POOL_RHEL6_ISCSI = new Guid("6d849ebf-755f-4552-ad09-9a090cda105");

    /**
     * Another predefined ISCSI storage pool.
     */
    protected static final Guid STORAGE_POOL_RHEL6_ISCSI_OTHER = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    /**
     * Predefined LocalFS storage pool.
     */
    protected static final Guid STORAGE_POOL_RHEL6_LOCALFS = new Guid("386bffd1-e7ed-4b08-bce9-d7df10f8c9a2");

    /**
     * Predefined storage pool serving Fedora VMs.
     */
    protected static final Guid STORAGE_POOL_FEDORA = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");

    /**
     * Predefined NFS master storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS2_1 = new Guid("d9ede37f-e6c3-4bf9-a984-19174070aa31");

    /**
     * Predefined NFS master storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS2_2 = new Guid("d9ede37f-e6c3-4bf9-a984-19174070aa32");

    /**
     * Predefined NFS  storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS2_3 = new Guid("d9ede37f-e6c3-4bf9-a984-19174070aa41");

    /**
     * Predefined NFS master storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS_MASTER = new Guid("c2211b56-8869-41cd-84e1-78d7cb96f31d");

    /**
     * Predefined NFS iso storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS_ISO = new Guid("17e7489d-d490-4681-a322-073ca19bd33d");

    /**
     * Predefined inactive NFS iso storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_NFS_INACTIVE_ISO = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9aa");

    /**
     * Predefined shared iso storage domain for both the storgae pools
     */
    protected static final Guid SHARED_ISO_STORAGE_DOMAIN_FOR_SP2_AND_SP3 =
            new Guid("d034f3b2-fb9c-414a-b1be-1e642cfe57ae");

    /**
     * Predefined scale storage domain.
     */
    public static final Guid STORAGE_DOMAIN_SCALE_SD5 = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");

    /**
     * Predefined scale storage domain.
     */
    protected static final Guid STORAGE_DOMAIN_SCALE_SD6 = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ac");

    /**
     * Predefined vds group.
     */
    public static final Guid CLUSTER_RHEL6_ISCSI = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    /**
     * Predefined vds group.
     */
    protected static final Guid CLUSTER_RHEL6_NFS = new Guid("0e57070e-2469-4b38-84a2-f111aaabd49d");

    /**
     * Predefined vds group.
     */
    protected static final Guid CLUSTER_RHEL6_NFS_2 = new Guid("eba797fb-8e3b-4777-b63c-92e7a5957d7c");

    /**
     * Predefined vds group for LocalFS storage pool
     */
    protected static final Guid CLUSTER_RHEL6_LOCALFS = new Guid("eba797fb-8e3b-4777-b63c-92e7a5957d7f");

    /**
     * Predefined vds group, with no specific quotas associated to it.
     */
    protected static final Guid CLUSTER_RHEL6_NFS_NO_SPECIFIC_QUOTAS =
            new Guid("eba797fb-8e3b-4777-b63c-92e7a5957d7e");

    /**
     * Predefined vds group with no running VMs
     */
    public static final Guid CLUSTER_NO_RUNNING_VMS = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3");

    /**
     * Predefined vds group with cluster upgrade running
     */
    public static final Guid CLUSTER_UPGRADE_RUNNING = new Guid("0e57070e-2469-4b38-84a2-f111aaabd49d");

    /**
     * Predefined NFS storage pool.
     */
    public static final Guid VDS_RHEL6_NFS_SPM = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");

    /**
     * Predefined VDS gluster2-vdsc.redhat.com
     */
    public static final Guid VDS_GLUSTER_SERVER2 = new Guid("2001751e-549b-4e7a-aff6-32d36856c125");

    /**
     * Predefined VdsStatic record that doesn't have a corresponding VdsDynamic record
     */
    public static final Guid VDS_JUST_STATIC_ID = new Guid("09617c59-cd31-4878-9c23-5ac17d8e1e3a");

    /**
     * Predefined vds
     */
    public static final Guid HOST_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    public static final Guid HOST_WITH_NO_VFS_CONFIGS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e8");

    /**
     * Predefined quota with general limitations. Its GUID is 88296e00-0cad-4e5a-9291-008a7b7f4399.<BR/>
     * The {@code Quota} has the following limitations:
     * <ul>Global limitation:
     * <li>virtual_cpu = 100</li>
     * <li>mem_size_mb = 10000</li>
     * <li>storage_size_gb = 1000000</li></ul>
     * Specific cluster ID c2211b56-8869-41cd-84e1-78d7cb96f31d(STORAGE_DOMAIN_NFS_MASTER) with no limitations
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
     * Specific storage ID c2211b56-8869-41cd-84e1-78d7cb96f31d(STORAGE_DOMAIN_NFS_MASTER) <ul><li>storage_size_gb = 1000</li></ul>
     * Specific cluster ID b399944a-81ab-4ec5-8266-e19ba7c3c9d1(CLUSTER_RHEL6_ISCSI) <ul><li>virtual_cpu = 10</li><li>mem_size_mb = -1</li></ul>
     * Specific cluster ID 0e57070e-2469-4b38-84a2-f111aaabd49d(CLUSTER_RHEL6_NFS) <ul><li>virtual_cpu = null</li><li>mem_size_mb = -1</li></ul>
     */
    protected static final Guid QUOTA_SPECIFIC = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4400");

    /**
     * Predefined quota with general and specific limitations.
     */
    protected static final Guid QUOTA_SPECIFIC_AND_GENERAL = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4401");

    /**
     * Predefined quota with no limitations.
     */
    protected static final Guid QUOTA_EMPTY = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4405");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-50</li>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li></ul>
     */
    public static final Guid VM_RHEL5_POOL_50 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    public static final Guid VM_RHEL5_POOL_50_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5001");
    public static final String VM_RHEL5_POOL_50_NAME = "rhel5-pool-50";

    /**
     * Predefined VM with no attached disks for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-51</li>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    protected static final Guid VM_RHEL5_POOL_51 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-52</li>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    protected static final Guid VM_RHEL5_POOL_52 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5002");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-57</li>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    public static final Guid VM_RHEL5_POOL_57 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-59</li>
     * <li>Vds group: rhel6.nfs (0e57070e-2469-4b38-84a2-f111aaabd49d)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    public static final Guid VM_RHEL5_POOL_59 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4359");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: rhel5-pool-60</li>
     * <li>Vds group: rhel6.nfs (0e57070e-2469-4b38-84a2-f111aaabd49d)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    protected static final Guid VM_RHEL5_POOL_60 = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4360");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: vm1-shared-bootable-disk</li>
     * <li>Vds group: rhel6.nfs2 (eba797fb-8e3b-4777-b63c-92e7a5957d7c)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    public static final Guid VM_VM1_SHARED_BOOTABLE_DISK = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4361");

    /**
     * Predefined VM for testing with the following properties :
     * <ul>
     * <li>VM name: vm2-shared-nonbootable-disk</li>
     * <li>Vds group: rhel6.nfs2 (eba797fb-8e3b-4777-b63c-92e7a5957d7c)</li>
     * <li>Based on template: 1 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * </ul>
     */
    public static final Guid VM_VM2_SHARED_NONBOOTABLE_DISK = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4362");

    /**
     * Predefined template for testing with the following properties :
     * <ul>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * </ul>
     */
    public static final Guid VM_TEMPLATE_RHEL5 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    /**
     * Predefined template with no attached disks for testing with the following properties :
     * <ul>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * </ul>
     */
    protected static final Guid VM_TEMPLATE_RHEL5_2 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b80");

    /**
     * Predefined template for testing with the following properties :
     * <ul>
     * <li>Vds group: rhel6.nfs (0e57070e-2469-4b38-84a2-f111aaabd49d)</li>
     * </ul>
     */
    protected static final Guid VM_TEMPLATE_RHEL6_1 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b81");

    /**
     * Predefined template for testing with the following properties :
     * <ul>
     * <li>Vds group: rhel6.nfs (0e57070e-2469-4b38-84a2-f111aaabd49d)</li>
     * </ul>
     */
    protected static final Guid VM_TEMPLATE_RHEL6_2 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b82");

    /**
     * Predefined template version for testing with the following properties :
     * <ul>
     * <li>Vds group: rhel6.iscsi (b399944a-81ab-4ec5-8266-e19ba7c3c9d1)</li>
     * <li>Base template: VM_TEMPLATE_RHEL5 (1b85420c-b84c-4f29-997e-0eb674b40b79)</li>
     * <li>template version: 2</li>
     * </ul>
     */
    public static final Guid VM_TEMPLATE_RHEL5_V2 = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b83");

    public static final Guid INSTANCE_TYPE = new Guid("99408929-82cf-4dc7-a532-9d998063fa95");

    /**
     * Predefined unregistered Template related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31)
     */
    protected static final Guid UNREGISTERED_TEMPLATE = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b94");

    /**
     * Predefined unregistered VM related to Storage Domain STORAGE_DOMAIN_NFS2_1 (d9ede37f-e6c3-4bf9-a984-19174070aa31)
     */
    public static final Guid UNREGISTERED_VM = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4359");

    /**
     * Predefined unregistered disk related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31) with disk alias and description.
     */
    protected static final Guid UNREGISTERED_DISK = new Guid("88296e11-0cad-4e5a-9299-008a7b6f4388");

    /**
     * Predefined unregistered disk related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31) with disk alias and description, attached to vm_id
     * 77296e00-0cad-4e5a-9299-008a7b6f4355.
     */
    protected static final Guid UNREGISTERED_DISK2 = new Guid("98296e11-0cad-4e5a-9299-008a7b6f4389");

    /**
     * Predefined unregistered disk related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31) with disk alias and description, attached to the following vms_id:
     * 77296e00-0cad-4e5a-9299-008a7b6f4355,77296e00-0cad-4e5a-9299-008a7b6f4356.
     */
    protected static final Guid UNREGISTERED_DISK3 = new Guid("10296e11-0cad-4e5a-9299-008a7b6f4310");

    /**
     * Predefined unregistered disk related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31) with disk alias and without description, attached to the following
     * vms_id: 77296e00-0cad-4e5a-9299-008a7b6f4355,77296e00-0cad-4e5a-9299-008a7b6f4356.
     */
    protected static final Guid UNREGISTERED_DISK4 = new Guid("10296e11-0cad-4e5a-9299-008a7b6f4311");

    /**
     * Predefined unregistered disk related to Storage Domain STORAGE_DOMAIN_NFS2_1
     * (d9ede37f-e6c3-4bf9-a984-19174070aa31) without disk alias nor description, attached to the following vms_id:
     * 77296e00-0cad-4e5a-9299-008a7b6f4355,77296e00-0cad-4e5a-9299-008a7b6f4356.
     */
    protected static final Guid UNREGISTERED_DISK5 = new Guid("10296e11-0cad-4e5a-9299-008a7b6f4312");

    /**
     * Predefined VM with no attached disks
     */
    public static final Guid VM_WITH_NO_ATTACHED_DISKS = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4357");

    /**
     * Predefined vm job for testing with the following properties :
     * <ul>
     * <li>vm_id: VM_RHEL5_POOL_57 (77296e00-0cad-4e5a-9299-008a7b6f4355)</li>
     * </ul>
     */
    public static final Guid EXISTING_VM_JOB = new Guid("f062f24a-f24c-11e3-94a8-8bd00dba5830");

    /**
     * Predefined vm block job for testing with the following properties :
     * <ul>
     * <li>vm_id: VM_RHEL5_POOL_59 (77296e00-0cad-4e5a-9299-008a7b6f4359)</li>
     * <li>image_group_id: IMAGE_GROUP_ID (1b26a52b-b60f-44cb-9f46-3ef333b04a35)</li>
     * </ul>
     */
    public static final Guid EXISTING_VM_BLOCK_JOB = new Guid("59c24eb6-f24d-11e3-b030-df3b57f3e299");

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
     * Predefined role for testing
     * <ul>
     * <li>name: jUnitTestRole</li>
     * </ul>
     */
    protected static final Guid ROLE_ID = new Guid("f5972bfa-7102-4d33-ad22-9dd421bfba78");

    /**
     * Predefined image with the following properties :
     * <ul>
     * <li>disk id: 1b26a52b-b60f-44cb-9f46-3ef333b04a35</li>
     * <li>vm_snapshot_id: a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89</li>
     * </ul>
     */
    protected static final Guid IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");

    /**
     * Predefined image with the following properties :
     * <ul>
     * <li>disk id: 1b26a52b-b60f-44cb-9f46-3ef333b04a38</li>
     * </ul>
     */
    protected static final Guid IMAGE_ID_2 = new Guid("c9a559d9-8666-40d1-9967-759502b19f0c");

    /**
     * Predefined floating cinder disk with the following properties :
     * <ul>
     * <li>disk id: 1b26a52b-b60f-44cb-9f46-3ef333b04a45</li>
     * </ul>
     */
    protected static final Guid CINDER_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f580");

    /**
     * Predefined disk for testing.
     */
    protected static final Guid DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");

    /**
     * Predefined disk for testing.
     */
    protected static final Guid DISK_ID_2 = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a36");

    /**
     * Predefined disk for testing with non-existing vm_snapshot_id and the following properties:
     * <ul>
     * <li>disk_id: 1b26a52b-b60f-44cb-9f46-3ef333b04a49</li>
     * <li>image_guid: c9a559d9-8666-40d1-9967-759502b19f0e</li>
     * <li>vm_id: 77296e00-0cad-4e5a-9299-008a7b6f4360</li>
     * <li>vm_snapshot_id: a7bb24df-9fdf-4bd6-b7a9-f5ce52da8888</li>
     * </ul>
     */
    protected static final Guid DISK_ID_3 = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a49");

    /**
     * Predefined Job with Steps
     */
    protected static final Guid JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5889");

    /**
    * Predefined step for testing.
    */
    protected static final Guid STEP_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5229");

    /**
     * Predefined step for testing.
     */
    protected static final Guid STEP_ID_2 = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5228");

    /**
     * Predefined image on a VM template for testing. <BR/>
     * The image is defined on storage domain STORAGE_DOMAIN_SCALE_SD5.
     */
    protected static final Guid TEMPLATE_IMAGE_ID = new Guid("52058975-3d5e-484a-80c1-01c31207f578");

    /**
     * Predefined image group for testing with the following properties :
     * <ul>
     * <li>contains the following images: 42058975-3d5e-484a-80c1-01c31207f578, 42058975-3d5e-484a-80c1-01c31207f579,
     * c9a559d9-8666-40d1-9967-759502b19f0b, c9a559d9-8666-40d1-9967-759502b19f0d</li>
     * <li>vm_snapshot_id: a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89</li>
     * </ul>
     */
    protected static final Guid IMAGE_GROUP_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a35");

    protected static final Guid IMAGE_GROUP_ID_2 = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a38");

    /**
     * Predefined floating disk for testing.
     */
    protected static final Guid FLOATING_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a36");

    /**
     * Predefined floating lun for testing.
     */
    protected static final Guid FLOATING_LUN_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a37");

    /**
     * Predefined floating Cinder disk for testing.
     */
    protected static final Guid FLOATING_CINDER_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a45");

    /**
     * Predefined disk id based on LUN for testing.<BR/>
     * LUN ID: 1IET_00180002
     */
    protected static final Guid LUN_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a41");

    /**
     * Predefined LUN ID
     */
    protected static final String LUN_ID_FOR_DISK = "1IET_00180002";

    /**
     * Predefined LUN ID
     */
    protected static final String LUN_ID_FOR_DISK2 = "1IET_00180004";

    /**
     * Predefined LUN ID that contains domain's 72e3a666-89e1-4005-a7ca-f7548004a9ab first extant
     * of the metadata lv
     */
    protected static final String LUN_ID_OF_DOMAIN_METADATA = "DOMAIN_METADATA_LUN-1";

    /**
     * Predefined LUN ID that contains domain's 72e3a666-89e1-4005-a7ca-f7548004a9ab vg metadata
     */
    protected static final String LUN_ID_OF_DOMAIN_VG_METADATA = "VG_DOMAIN_METADATA_LUN-1";

    protected static final String LUN_ID1= "1IET_00180001";

    protected static final String LUN_ID2= "1IET_00180003";

    protected static final String ORPHAN_LUN_ID1= "1IET_00180005";

    protected static final String ORPHAN_LUN_ID2= "1IET_00180006";

    /**
     * Predefined boot LUN disk attached to VM_RHEL5_POOL_57
     */
    protected static final Guid BOOTABLE_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a40");
    /**
     * Predefined shared boot IMAGE disk attached to VM_VM1_SHARED_BOOTABLE_DISK & VM_VM2_SHARED_NONBOOTABLE_DISK
     */
    protected static final Guid BOOTABLE_SHARED_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a47");

    /**
     * Predefined entity that has associated tasks.
     */
    protected static final Guid ENTITY_WITH_TASKS_ID = STORAGE_DOMAIN_SCALE_SD5;

    /**
     * Predefined async task
     */
    protected static final Guid EXISTING_TASK_ID = new Guid("340fd52b-3400-4cdd-8d3f-C9d03704b0aa");

    /**
     * Predefined engine session id
     */
    protected static final Guid EXISTING_SESSION_ID = new Guid("c61a1dec-6b5f-11e4-80d9-3c970e14c386");

    /**
     * An unprivileged session
     */
    protected static final long UNPRIVILEGED_SESSION_ID = -1L;
    /**
     * A privileged session id
     */
    protected static final long PRIVILEGED_SESSION_ID = -2L;

    /**
     * Predefined engine group id
     */
    protected static final Guid EXISTING_GROUP_ID = new Guid("cd854d52-b6bf-4d74-81cd-0345d8266939");

    /**
     * ID of an existing snapshot
     */
    protected static final Guid EXISTING_SNAPSHOT_ID = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89");

    /**
     * ID of an existing snapshot
     */
    protected static final Guid EXISTING_SNAPSHOT_ID2 = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f11");

    /**
     * ID of an existing storage connection
     */
    protected static final Guid EXISTING_STORAGE_CONNECTION_ID = new Guid("7fe9a439-a68e-4c15-8885-29d34eb6cabf");

    /**
     * ID of an existing NFS storage connection with version 'auto'
     */
    protected static final Guid EXISTING_STORAGE_CONNECTION_NFS_AUTO_ID = new Guid("0cc146e8-e5ed-482c-8814-270bc48c2981");

    /**
     * ID of an existing storage domain with the storage connection which ID is defined in
     * EXISTING_STORAGE_CONNECTION_ID
     */
    protected static final Guid EXISTING_DOMAIN_ID_FOR_CONNECTION_ID = new Guid("c2211b56-8869-41cd-84e1-78d7cb96f31d");

     /** Predefined Network for testing with the following properties :
     * <ul>
     * <li>name: engine2</li>
     * <li>description: Management Network</li>
     * <li>stp: 0</li>
     * <li>storage_pool_id: rhel6.iscsi (6d849ebf-755f-4552-ad09-9a090cda105d)</li>
     * </ul>
     */
    public static final Guid NETWORK_ENGINE_2 = new Guid("58d5c1c6-cb15-4832-b2a4-023770607189");

    /**
     * Predefined Network for testing with the following properties :
     * <ul>
     * <li>name: engine</li>
     * <li>description: Management Network</li>
     * <li>stp: 0</li>
     * <li>storage_pool_id: rhel6.iscsi (6d849ebf-755f-4552-ad09-9a090cda105d)</li>
     * </ul>
     */
    public static final Guid NETWORK_ENGINE = new Guid("58d5c1c6-cb15-4832-b2a4-023770607188");

    public static final String NETWORK_ENGINE_VDSM_NAME = "engine";

    /**
     * Predefined Network without entries in network_cluster with the following properties :
     * <ul>
     * <li>name: engine3</li>
     * <li>description: Management Network</li>
     * <li>stp: 0</li>
     * <li>storage_pool_id: rhel6.iscsi (6d849ebf-755f-4552-ad09-9a090cda105d)</li>
     * </ul>
     */
    public static final Guid NETWORK_NO_CLUSTERS_ATTACHED = new Guid("58d5c1c6-cb15-4832-b2a4-023770607190");

    /**
     * Predefined VmNetworkInterface with the following properties :
     * <ul>
     * <li>network_name: engine</li>
     * <li>vm_guid: 77296e00-0cad-4e5a-9299-008a7b6f4355</li>
     * <li>vmt_guid: 1b85420c-b84c-4f29-997e-0eb674b40b79</li>
     * <li>mac_addr: 00:1a:4a:16:87:d9</li>
     * <li>name: nic1</li>
     * </ul>
     */
    public static final Guid VM_NETWORK_INTERFACE = new Guid("e2817b12-f873-4046-b0da-0098293c14fd");

    /**
     * Predefined VmNetworkInterface with the following properties:
     * <ul>
     * <li>network_name: engine</li>
     * <li>vmt_guid: 1b85420c-b84c-4f29-997e-0eb674b40b79</li>
     * <li>mac_addr: 00:1a:4a:16:87:d9</li>
     * <li>name: nic1</li>
     * </ul>
     */
    public static final Guid TEMPLATE_NETWORK_INTERFACE = new Guid("e2817b12-f873-4046-b0da-0098293c0000");

    /**
     * Predefined  VmNicFilterParameter with the following properties :
     * <ul>
     * <li>id: b2652c30-069c-4b42-a645-dde2e0c20f23</li>
     * <li>name: IP</li>
     * <li>value: 10.0.0.1<</li>
     * <li>vm_interface_id: VM_NETWORK_INTERFACE</li>
     * </ul>
     */
    public static final Guid VM_NETWORK_FILTER_PARAMETER = new Guid("b2652c30-069c-4b42-a645-dde2e0c20f23");

    /**
     * Predefined VdsNetworkInterface with the following properties :
     * <ul>
     * <li>name: eth0</li>
     * <li>network_name: engine</li>
     * <li>vds_id: afce7a39-8e8c-4819-ba9c-796d316592e6</li>
     * <li>mac_addr: 78:E7:D1:E4:8C:70</li>
     * </ul>
     */
    public static final Guid VDS_NETWORK_INTERFACE = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9db");

    /**
     * Predefined VdsNetworkInterface with the following properties :
     * <ul>
     * <li>name: eth0</li>
     * <li>network_name: engine2</li>
     * <li>vds_id: afce7a39-8e8c-4819-ba9c-796d316592e7</li>
     * <li>mac_addr: 78:E7:D1:E4:8C:71</li>
     * </ul>
     */
    public static final Guid VDS_NETWORK_INTERFACE2 = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9dd");

    /**
     * Predefined VdsNetworkInterface with the following properties :
     * <ul>
     * <li>name: eth1</li>
     * <li>network_name: engine2</li>
     * <li>vds_id: afce7a39-8e8c-4819-ba9c-796d316592e6</li>
     * <li>mac_addr: 78:E7:D1:E4:8C:73</li>
     * </ul>
     */
    public static final Guid VDS_NETWORK_INTERFACE_WITHOUT_QOS = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9df");

    /**
     * Predefined VnicProfile with the following properties :
     * <ul>
     * <li>name: engine_profile</li>
     * <li>network_id: 58d5c1c6-cb15-4832-b2a4-023770607188</li>
     * <li>port_mirroring: false</li>
     * <li>passthrough: false</li>
     * </ul>
     */
    public static final Guid VM_NETWORK_INTERFACE_PROFILE = new Guid("fd81f1e1-785b-4579-ab75-1419ebb87052");

    /**
     * Predefined VnicProfile with the following properties :
     * <ul>
     * <li>name: engine_profile_pm</li>
     * <li>network_id: 58d5c1c6-cb15-4832-b2a4-023770607188</li>
     * <li>port_mirroring: true</li>
     * </ul>
     */
    public static final Guid VM_NETWORK_INTERFACE_PM_PROFILE = new Guid("a667da39-27b0-47ec-a5fa-d4293a62b222");

    public static final Guid VM_NETWORK_INTERFACE_PASSTHROUGH_PROFILE =
            new Guid("d0f2ca62-f564-447c-aa55-ce2aa12ea798");

    public static final Guid VM_NETWORK_INTERFACE_PROFILE_NOT_USED = new Guid("2b75e023-a1fb-4dcb-9738-0ec7fe2d51c6");

    public static final Guid NETWORK_QOS = new Guid("de956031-6be2-43d6-bb90-5191c9253314");

    /**
     * Predefined HostNetworkQos with the following properties:
     * <ul>
     * <li>id: de956031-6be2-43d6-bb90-5191c9253318</li>
     * <li>name: host_network_qos_a</li>
     * <li>storage_pool_id: {@link #STORAGE_POOL_MIXED_TYPES}</li>
     * <li>out_average_linkshare: 1000</li>
     * <li>out_average_upperlimit: 2000</li>
     * <li>out_average_realtime: 500</li>
     * </ul>
     */
    public static final Guid HOST_NETWORK_QOS_A = new Guid("de956031-6be2-43d6-bb90-5191c9253318");

    /**
     * Predefined HostNetworkQos with the following properties:
     * <ul>
     * <li>id: de956031-6be2-43d6-bb90-5191c9253319</li>
     * <li>name: host_network_qos_b</li>
     * <li>storage_pool_id: {@link #STORAGE_POOL_MIXED_TYPES}</li>
     * <li>out_average_linkshare: 1200</li>
     * <li>out_average_upperlimit: 2400</li>
     * <li>out_average_realtime: 500</li>
     * </ul>
     */
    public static final Guid HOST_NETWORK_QOS_B = new Guid("de956031-6be2-43d6-bb90-5191c9253319");

    /**
     * Predefined HostNetworkQos with the following properties:
     * <ul>
     * <li>id: de956031-6be2-43d6-bb90-5191c9253320</li>
     * <li>name: host_network_qos_c</li>
     * <li>storage_pool_id: {@link #STORAGE_POOL_MIXED_TYPES}</li>
     * <li>out_average_linkshare: 700</li>
     * <li>out_average_upperlimit: 1400</li>
     * <li>out_average_realtime: 500</li>
     * </ul>
     */
    public static final Guid HOST_NETWORK_QOS_C = new Guid("de956031-6be2-43d6-bb90-5191c9253320");

    public static final String MAC_ADDRESS = "00:1a:4a:16:87:db";

    public static final int NUMBER_OF_VM_INTERFACE_VIEWS = 3;

    public static final int NUMBER_OF_VM_INTERFACES = 4;

    public static final int NUMBER_OF_VM_NETWORK_FILTER_PARAMETERS = 4;

    public static final int NUMBER_OF_VM_NETWORK_FILTER_PARAMETERS_OF_VM_NETWORK_INTERFACE = 3;

    public static final Guid NETWORK_ATTACHMENT = new Guid("fd81f1f1-785b-4579-ab75-1419ebb87051");

    public static final Guid NETWORK_ATTACHMENT_NIC = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9dd");

    public static final Guid NETWORK_ATTACHMENT_NIC2 = new Guid("ba31682e-6ae7-4f9d-8c6f-04c93acca9df");

    public static final Guid NETWORK_ATTACHMENT_HOST = HOST_ID;

    public static final Guid NETWORK_FILTER = new Guid("c477a4cc-f00e-45be-b61f-224766437091");

    public static final String NETWORK_FILTER_NAME = "TEST_NETWORK_FILTER";

    public static final Version NETWORK_FILTER_VERSION = Version.v4_4;

    public static final Guid OVIRT_NO_FILTER_NETWORK_FILTER = new Guid("22510576-a243-468e-bc37-d68f16130b02");

    public static final String OVIRT_NO_FILTER_NETWORK_FILTER_NAME = "ovirt-no-filter";

    public static final Version OVIRT_NO_FILTER_MINIMAL_SUPPORTED_VERSION = Version.v4_2;

    public static final Guid VNIC_PROFILE_NETWORK_FILTER = new Guid("3c6f56c2-bd3a-4575-969b-b6cda2d594ad");

    /**
     * Gluster Hook ID(s)
     */
    public static final Guid HOOK_ID = new Guid("d2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    public static final Guid HOOK_ID2 = new Guid("da9e2f09-2835-4530-9bf5-576c52b11941");

    public static final Guid NEW_HOOK_ID = new Guid("d2cb2f73-fab3-4a42-93f0-d5e4c069a43f");

    public static final Guid GLUSTER_CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");

    /**
     * Gluster Server UUID(s)
     */

    public static final Guid GLUSTER_SERVER_UUID1 = new Guid("da9e2f09-2835-4530-9bf5-576c52b11941");

    public static final Guid GLUSTER_SERVER_UUID2 = new Guid("da9e2f09-2835-4530-9bf5-576c52b11942");

    public static final Guid GLUSTER_SERVER_UUID3 = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");

    public static final String GLUSTER_SERVER_NAME3 = "magenta-vdsc";

    public static final Guid GLUSTER_BRICK_SERVER1 = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");

    public static final Guid GLUSTER_SERVER_UUID_NEW = new Guid("da9e2f09-2835-4530-9bf5-576c52b11943");

    public static final Guid EXISTING_VDSM_TASK_ID = new Guid("140fd52b-3400-4cdd-8d3f-C9d03704b0aa");

    /**
     * Gluster volume UUID(s)
     */
    public static final Guid GLUSTER_VOLUME_UUID1 = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");

    /**
     * Gluster brick UUID(s)
     */
    public static final Guid GLUSTER_BRICK_UUID1 = new Guid("6ccdc294-d77b-4929-809d-8afe7634b47d");

    public static final Guid GLUSTER_BRICK_UUID2 = new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a754");

    public static final Guid GLUSTER_BRICK_UUID3 = new Guid("2ab384da-6190-4668-aa2e-37bc5c5ff014");

    /**
     * Gluster brick directories
     */
    public static final String GLUSTER_BRICK_DIR1 = "/export/test-vol-distribute-1/dir1";

    /**
     * Gluster Async task UUID(s)
     */
    public static final Guid GLUSTER_ASYNC_TASK_ID1 = new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a763");

    /**
     * Gluster Geo rep session
     */
    public static final Guid GLUSTER_GEOREP_SESSION_ID = new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a755");
    public static final Guid GLUSTER_GEOREP_SESSION_ID2 = new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a777");

    public static final Guid GLUSTER_GEOREP_SESSION_SLAVE_VOLUME_ID = new Guid("44f645f6-3fe9-4b35-a30c-11111a835ea8");

    /**
     * A test provider that exists.
     */
    public static final String PROVIDER_NAME = "provider";

    public static final Guid PROVIDER_ID = new Guid("1115c1c6-cb15-4832-b2a4-023770607111");

    public static final ProviderType PROVIDER_TYPE = ProviderType.OPENSTACK_NETWORK;

    public static final String EXTERNAL_NETWORK_ID = "52d5c1c6-cb15-4832-b2a4-023770607200";

    public static final Guid CINDER_PROVIDER_ID = new Guid("983fcfe8-ca56-4a11-8d0e-840c988d67af");

    public static final Guid OVN_NETWORK_PROVIDER_ID = new Guid("47f310ad-2175-4464-89bb-5ca09062b685");

    public static final Guid EXISTING_LIBVIRT_SECRET_ID = new Guid("77569427-9fbe-41db-ae91-fb96fab17141");

    /**
     * Scheduling Policy
     */
    public static final Guid CLUSTER_POLICY_EVEN_DISTRIBUTION = new Guid("20d25257-b4bd-4589-92a6-c4c5c5d3fd1a");
    public static final Guid POLICY_UNIT_MIGRATION = new Guid("84e6ddee-ab0d-42dd-82f0-c297779db5e5");

    /**
     * For vnic profile views
     */
    public static final Guid DATA_CENTER = STORAGE_POOL_RHEL6_ISCSI_OTHER;
    public static final Guid CLUSTER = CLUSTER_RHEL6_ISCSI;

    public static final String DATA_CENTER_NAME = "rhel6.iscsi";

    public static final Guid EXISTING_AFFINITY_GROUP_ID = new Guid("6d849ebf-0ccc-4552-ad09-ccc90cda105d");
    public static final Guid AFFINITY_GROUP_2 = new Guid("6d849ebf-0ccc-4552-ad09-ccc90cda105e");
    public static final Guid AFFINITY_GROUP_3 = new Guid("6d849ebf-0ccc-4552-ad09-ccc90cda105f");

    /**
     * Id of predefined iscsi bond with the following properties:
     *
     * <ul>
     * <li>name: IscsiBond1</li>
     * <li>storage_pool_id_id: 6d849ebf-755f-4552-ad09-9a090cda105d</li>
     * </ul>
     */
    public static final Guid ISCSI_BOND_ID = new Guid("7368f2be-1263-41f8-b95e-70cdaf23b80d");

    /**
     * Id of predefined iscsi storage connection id with the following properties:
     *
     * <ul>
     * <li>connection: 10.35.64.25</li>
     * <li>iqn: iqn.2008-09.com.hateya:server.targeta</li>
     * <li>port: 3260</li>
     * <li>portal: 1</li>
     * </ul>
     */
    public static final String STORAGE_CONNECTION_ID = "0cc146e8-e5ed-482c-8814-270bc48c297e";

    public static final Guid EXISTING_COMMAND_ENTITY_ID = new Guid("340fd52b-3400-4cdd-8d3f-c9d03704b0a1");

    /**
     * UUIDs for QoS objects
     */
    public static final Guid QOS_ID_1 = new Guid("ae956031-6be2-43d6-bb90-5191c9253314");

    public static final Guid QOS_ID_2 = new Guid("ae956031-6be2-43d6-bb90-5191c9253315");

    public static final Guid QOS_ID_3 = new Guid("ae956031-6be2-43d6-bb90-5191c9253316");

    public static final Guid QOS_ID_4 = new Guid("ae956031-6be2-43d6-bb90-5191c9253317");

    public static final Guid QOS_ID_5 = new Guid("ae956031-6be2-43d6-bb90-5191c9253318");

    public static final Guid QOS_ID_6 = new Guid("ae956031-6be2-43d6-bb90-5191c9253319");
    /**
     * Number of VMs on clusters
     */
    public static final int NUMBER_OF_VMS_IN_CLUSTER_RHEL6_NFS_CLUSTER = 0;
    public static final int NUMBER_OF_VMS_IN_CLUSTER_RHEL6_ISCSI = 7;

    public static final Guid NON_DEFAULT_MAC_POOL = new Guid("c248552d-64d4-4a77-ab1d-4c5eea00be6b");
    public static final Guid NOT_USED_MAC_POOL_ID = new Guid("17baf4e3-4347-4f81-915f-7762a108b81a");
    public static final Guid DEFAULT_MAC_POOL_ID = new Guid("8c3a60e9-6a66-40db-84e0-c74077991775");

    public static final Guid DISK_PROFILE_1 = new Guid("fd81f1e1-785b-4579-ab75-1419ebb87052");
    public static final Guid DISK_PROFILE_2 = new Guid("fd81f1e1-785b-4579-ab75-1419ebb87053");

    public static final Guid CPU_PROFILE_1 = new Guid("fd81f1e1-785b-4579-ab75-1419ebb87055");
    public static final Guid CPU_PROFILE_2 = new Guid("fd81f1e1-785b-4579-ab75-1419ebb87056");

    public static final Guid NETWORK_HOST_DEVICE_HOST_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    public static final String NETWORK_HOST_DEVICE_NAME = "net_enp5s16f4_3e_fa_b0_f6_48_a3";

    public static final Guid HOST_NIC_VFS_CONFIG = new Guid("6b31bcc8-c6c3-4884-9a10-5f1f076f20c1");
    public static final Guid HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_1 = new Guid("6b31bcc8-c6c3-4884-9a10-5f1f076f20c2");
    public static final Guid HOST_NIC_VFS_CONFIG_NOT_ALL_NETWORKS_ALLOWED_2 = new Guid("6b31bcc8-c6c3-4884-9a10-5f1f076f20c3");

    public static final Guid POSIX_STORAGE_DOMAIN_ID = new Guid("bee623f3-9174-4ffd-aa30-4fb0dc0aa2f5");
    public static final Guid EXISTING_DNS_RESOLVER_CONFIGURATION =
            Guid.createGuidFromString("6de58dc3-171d-426d-99fc-295c25c091d3");
    public static final Guid EXISTING_DNS_RESOLVER_CONFIGURATION_TO_REMOVE =
                    Guid.createGuidFromString("d1083ae5-df27-44dc-a3b1-a818844db9e4");

    public static final Guid SMALL_ICON_ID = new Guid("38fc5e1a-f96b-339b-9894-def6f366daf5");
    public static final Guid LARGE_ICON_ID = new Guid("a3b954f0-31ff-3166-b7a1-28b23202b198");


    public static final Guid IMAGE_TRANSFER_ID = new Guid("340fd52b-3400-4cdd-8d3f-c9d03704b0a1");
    public static final Guid EXISTING_IMAGE_TRANSFER_ID = new Guid("340fd52b-3400-4cdd-8d3f-c9d03704b0a2");

    public static final Guid PROVIDER_BINDING_HOST_ID_HOST_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e8");
    public static final String PROVIDER_BINDING_HOST_ID_PLUGIN_TYPE = "OVIRT_PROVIDER_OVN";
    public static final String PROVIDER_BINDING_HOST_PLUGIN_ID = "1ce-c01d-bee2-15-a5-900d-a5-11fe";

    public static final Guid PROVIDER_BINDING_HOST_ID_HOST_ID2 = new Guid("3001751e-549b-4e7a-aff6-12336856ce45");

    public static final String IPV6_ADDR_EXISTS = "1::2";

    public static final String IPV6_ADDR_NOT_EXIST = "2::3";

    /**
     * Predefined storage domain with ISO
     */
    protected static final Guid STORAGE_DOMAIN_WITH_ISO = new Guid("bee623f3-9174-4ffd-aa30-4fb0dc0aa2f6");

    /**
     * Predefined VM with mounted ISO
     */
    protected static final String VM_NAME_WITH_MOUNTED_ISO = "vm1-with-iso";
}
