package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class EntityDaoImplTest extends BaseDaoTestCase {

    // entity IDs for testing retrieving an entity by id and type
    private static final Guid VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f5001");
    private static final Guid VM_TEMPLATE_ID = new Guid("00000000-0000-0000-0000-000000000000");
    private static final Guid VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");
    private static final Guid TAG_ID = new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c");
    private static final Guid BOOKMARK_ID = new Guid("a4affabf-7b45-4a6c-b0a9-107d0bbe265e");
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid STORAGE_DOMAIN_ID = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");
    private static final Guid STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
    private static final Guid USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    private static final Guid ROLE_ID = new Guid("119caae6-5c1b-4a82-9858-dd9e5d2e1400");
    private static final Guid QUOTA_ID = new Guid("88296e00-0cad-4e5a-9291-008a7b7f4399");
    private static final Guid DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");
    private static final Guid NETWORK_ID = new Guid("58d5c1c6-cb15-4832-b2a4-023770607188");
    private static final Guid VM_STATIC_GUID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");

    @Inject
    private EntityDao underTest;

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private TagDao tagDao;
    @Inject
    private BookmarkDao bookmarkDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private RoleDao roleDao;
    @Inject
    private QuotaDao quotaDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private DiskProfileDao diskProfileDao;
    @Inject
    private CpuProfileDao cpuProfileDao;

    @Test
    public void testGetEntityNameByIdAndTypeForVM() {
        VmStatic vmStatic = vmStaticDao.get(VM_ID);
        assertNotNull(vmStatic);
        String name = vmStatic.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(VM_STATIC_GUID, VdcObjectType.VM));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVmTemplate() {
        VmTemplate vmTemplate = vmTemplateDao.get(VM_TEMPLATE_ID);
        assertNotNull(vmTemplate);
        String name = vmTemplate.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(VM_TEMPLATE_ID, VdcObjectType.VmTemplate));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForHost() {
        VdsStatic vds = vdsStaticDao.get(VDS_ID);
        assertNotNull(vds);
        String name = vds.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(VDS_ID, VdcObjectType.VDS));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVmPool() {
        VmPool vmPool = vmPoolDao.get(VM_POOL_ID);
        assertNotNull(vmPool);
        String name = vmPool.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(VM_POOL_ID, VdcObjectType.VmPool));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForTag() {
        Tags tag = tagDao.get(TAG_ID);
        assertNotNull(tag);
        String name = tag.getTagName();
        assertEquals(name, underTest.getEntityNameByIdAndType(TAG_ID, VdcObjectType.Tags));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForBookmark() {
        Bookmark bookmark = bookmarkDao.get(BOOKMARK_ID);
        assertNotNull(bookmark);
        String name = bookmark.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(BOOKMARK_ID, VdcObjectType.Bookmarks));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForCluster() {
        Cluster cluster = clusterDao.get(CLUSTER_ID);
        assertNotNull(cluster);
        String name = cluster.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(CLUSTER_ID, VdcObjectType.Cluster));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForStorageDomain() {
        StorageDomain storageDomain = storageDomainDao.get(STORAGE_DOMAIN_ID);
        assertNotNull(storageDomain);
        String name = storageDomain.getStorageName();
        assertEquals(name, underTest.getEntityNameByIdAndType(STORAGE_DOMAIN_ID, VdcObjectType.Storage));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDataCenter() {
        StoragePool storagePool = storagePoolDao.get(STORAGE_POOL_ID);
        assertNotNull(storagePool);
        String name = storagePool.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(STORAGE_POOL_ID, VdcObjectType.StoragePool));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForUser() {
        DbUser dbUser = dbUserDao.get(USER_ID);
        assertNotNull(dbUser);
        String name = dbUser.getLoginName();
        assertEquals(name, underTest.getEntityNameByIdAndType(USER_ID, VdcObjectType.User));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForRole() {
        Role role = roleDao.get(ROLE_ID);
        assertNotNull(role);
        String name = role.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(ROLE_ID, VdcObjectType.Role));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForQuota() {
        Quota quota = quotaDao.getById(QUOTA_ID);
        assertNotNull(quota);
        String name = quota.getQuotaName();
        assertEquals(name, underTest.getEntityNameByIdAndType(QUOTA_ID, VdcObjectType.Quota));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDisk() {
        BaseDisk disk = baseDiskDao.get(DISK_ID);
        assertNotNull(disk);
        String name = disk.getDiskAlias();
        assertEquals(name, underTest.getEntityNameByIdAndType(DISK_ID, VdcObjectType.Disk));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForNetwork() {
        Network network = networkDao.get(NETWORK_ID);
        assertNotNull(network);
        String name = network.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(NETWORK_ID, VdcObjectType.Network));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVNICProfile() {
        VnicProfile vnicProfile = vnicProfileDao.get(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        assertNotNull(vnicProfile);
        String name = vnicProfile.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(FixturesTool.VM_NETWORK_INTERFACE_PROFILE, VdcObjectType.VnicProfile));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDiskProfile() {
        DiskProfile diskProfile = diskProfileDao.get(FixturesTool.DISK_PROFILE_1);
        assertNotNull(diskProfile);
        String name = diskProfile.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(FixturesTool.DISK_PROFILE_1, VdcObjectType.DiskProfile));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForCpuProfile() {
        CpuProfile cpuProfile = cpuProfileDao.get(FixturesTool.CPU_PROFILE_1);
        assertNotNull(cpuProfile);
        String name = cpuProfile.getName();
        assertEquals(name, underTest.getEntityNameByIdAndType(FixturesTool.CPU_PROFILE_1, VdcObjectType.CpuProfile));
    }
}
