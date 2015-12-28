package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VMStatus;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;

public class DbFacadeDaoTest extends BaseDaoTestCase {

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

    private static final Guid ADMIN_ROLE_TYPE_FROM_FIXTURE_ID = new Guid("F5972BFA-7102-4D33-AD22-9DD421BFBA78");
    private static final Guid SYSTEM_OBJECT_ID = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    private static final int NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM = 2;
    private static final int NUM_OF_USERS_IN_FIXTURES = 4;
    private static final Guid VM_STATIC_GUID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");

    /**
     * Ensures that the checkDBConnection method returns true when the connection is up
     */
    @Test
    public void testDBConnectionWithConnection() {
        assertTrue(dbFacade.checkDBConnection());
    }

    /**
     * Ensures that the checkDBConnection method throws an Exception when connection is not valid
     */
    @Test
    @DirtiesContext
    public void testDBConnectionWithoutConnection() {

        InputStream is = null;
        try {
            // setup
            Config.setConfigUtils(new DBConfigUtils(false));
            is = super.getClass().getResourceAsStream("/test-database.properties");
            Properties properties = new Properties();
            properties.load(is);
            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            DataSource dataSource = new SingleConnectionDataSource(
                    properties.getProperty("database.url"),
                    // Deliberately puts a none existing user name, so an
                    // exception will be thrown when trying to check the
                    // connection
                    "no-such-username",
                    properties.getProperty("database.password"),
                    true
                    );
            final DbEngineDialect dbEngineDialect = DbFacadeLocator.loadDbEngineDialect();
            final JdbcTemplate jdbcTemplate = dbEngineDialect.createJdbcTemplate(dataSource);
            final SimpleJdbcCallsHandler callsHandler = new SimpleJdbcCallsHandler(dbEngineDialect, jdbcTemplate);
            final DbFacadeLocator dbFacadeLocator = new DbFacadeLocator() {};
            final DbFacade localDbFacade = new DbFacade(jdbcTemplate, dbEngineDialect, callsHandler, dbFacadeLocator);

            localDbFacade.checkDBConnection();

            fail("Connection should be down since the DataSource has an invalid username");
            // If DataAccessException is thrown - the test has succeeded. Was unable to do
            // with "expected" annotation, presumably since we are using DbUnit
        } catch (DataAccessException desiredException) {
            assertTrue(true);
            // If this exception is thrown we fail the test
        } catch (Exception undesiredException) {
            undesiredException.printStackTrace();
            fail();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Test
    public void testUpdateLastAdminCheckStatus() {

        // Getting a nonAdmin user that is defined in the fixtures
        DbUser nonAdminUser =
                dbFacade.getDbUserDao().getByUsernameAndDomain("userportal2@testportal.redhat.com",
                        "testportal.redhat.com");

        assertNotNull(nonAdminUser);
        assertFalse(nonAdminUser.isAdmin());

        // execute and validate when not admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getId());
        nonAdminUser = dbFacade.getDbUserDao().get(nonAdminUser.getId());

        assertFalse(nonAdminUser.isAdmin());

        Permission perms = new Permission();
        perms.setRoleType(RoleType.ADMIN);

        // An available role from the fixtures
        perms.setRoleId(ADMIN_ROLE_TYPE_FROM_FIXTURE_ID);
        perms.setAdElementId(nonAdminUser.getId());
        perms.setObjectId(SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);

        // Save the permission to the DB and make sure it has been saved
        dbFacade.getPermissionDao().save(perms);
        assertNotNull(dbFacade.getPermissionDao().get(perms.getId()));

        // execute and validate when admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getId());
        nonAdminUser = dbFacade.getDbUserDao().get(nonAdminUser.getId());

        assertTrue(nonAdminUser.isAdmin());
    }

    @Test
    public void testGetSystemStatisticsValueWithSpecifiedStatus() {
        int numOfVmWithStatusMigratingFrom =
                dbFacade.getSystemStatisticsValue("VM", Integer.toString(VMStatus.MigratingFrom.getValue()));
        assertTrue(numOfVmWithStatusMigratingFrom == NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM);
    }

    @Test
    public void testGetSystemStatisticsValueWithoutSpecifiedStatus() {
        int numOfUsers = dbFacade.getSystemStatisticsValue("User", "");
        assertTrue(numOfUsers == NUM_OF_USERS_IN_FIXTURES);
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVM() {
        VmStatic vmStatic = dbFacade.getVmStaticDao().get(VM_ID);
        assertNotNull(vmStatic);
        String name = vmStatic.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(VM_STATIC_GUID, VdcObjectType.VM)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVmTemplate() {
        VmTemplate vmTemplate = dbFacade.getVmTemplateDao().get(VM_TEMPLATE_ID);
        assertNotNull(vmTemplate);
        String name = vmTemplate.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(VM_TEMPLATE_ID, VdcObjectType.VmTemplate)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForHost() {
        VdsStatic vds = dbFacade.getVdsStaticDao().get(VDS_ID);
        assertNotNull(vds);
        String name = vds.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(VDS_ID, VdcObjectType.VDS)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVmPool() {
        VmPool vmPool = dbFacade.getVmPoolDao().get(VM_POOL_ID);
        assertNotNull(vmPool);
        String name = vmPool.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(VM_POOL_ID, VdcObjectType.VmPool)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForTag() {
        Tags tag = dbFacade.getTagDao().get(TAG_ID);
        assertNotNull(tag);
        String name = tag.getTagName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(TAG_ID, VdcObjectType.Tags)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForBookmark() {
        Bookmark bookmark = dbFacade.getBookmarkDao().get(BOOKMARK_ID);
        assertNotNull(bookmark);
        String name = bookmark.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(BOOKMARK_ID, VdcObjectType.Bookmarks)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForCluster() {
        Cluster cluster = dbFacade.getClusterDao().get(CLUSTER_ID);
        assertNotNull(cluster);
        String name = cluster.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(CLUSTER_ID, VdcObjectType.Cluster)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForStorageDomain() {
        StorageDomain storageDomain = dbFacade.getStorageDomainDao().get(STORAGE_DOMAIN_ID);
        assertNotNull(storageDomain);
        String name = storageDomain.getStorageName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(STORAGE_DOMAIN_ID, VdcObjectType.Storage)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDataCenter() {
        StoragePool storagePool = dbFacade.getStoragePoolDao().get(STORAGE_POOL_ID);
        assertNotNull(storagePool);
        String name = storagePool.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(STORAGE_POOL_ID, VdcObjectType.StoragePool)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForUser() {
        DbUser dbUser = dbFacade.getDbUserDao().get(USER_ID);
        assertNotNull(dbUser);
        String name = dbUser.getLoginName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(USER_ID, VdcObjectType.User)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForRole() {
        Role role = dbFacade.getRoleDao().get(ROLE_ID);
        assertNotNull(role);
        String name = role.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(ROLE_ID, VdcObjectType.Role)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForQuota() {
        Quota quota = dbFacade.getQuotaDao().getById(QUOTA_ID);
        assertNotNull(quota);
        String name = quota.getQuotaName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(QUOTA_ID, VdcObjectType.Quota)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDisk() {
        BaseDisk disk = dbFacade.getBaseDiskDao().get(DISK_ID);
        assertNotNull(disk);
        String name = disk.getDiskAlias();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(DISK_ID, VdcObjectType.Disk)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForNetwork() {
        Network network = dbFacade.getNetworkDao().get(NETWORK_ID);
        assertNotNull(network);
        String name = network.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(NETWORK_ID, VdcObjectType.Network)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForVNICProfile() {
        VnicProfile vnicProfile = dbFacade.getVnicProfileDao().get(FixturesTool.VM_NETWORK_INTERFACE_PROFILE);
        assertNotNull(vnicProfile);
        String name = vnicProfile.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(FixturesTool.VM_NETWORK_INTERFACE_PROFILE, VdcObjectType.VnicProfile)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForDiskProfile() {
        DiskProfile diskProfile = dbFacade.getDiskProfileDao().get(FixturesTool.DISK_PROFILE_1);
        assertNotNull(diskProfile);
        String name = diskProfile.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(FixturesTool.DISK_PROFILE_1, VdcObjectType.DiskProfile)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForCpuProfile() {
        CpuProfile cpuProfile = dbFacade.getCpuProfileDao().get(FixturesTool.CPU_PROFILE_1);
        assertNotNull(cpuProfile);
        String name = cpuProfile.getName();
        assertEquals(name, dbFacade.getEntityNameByIdAndType(FixturesTool.CPU_PROFILE_1, VdcObjectType.CpuProfile));
    }
}
