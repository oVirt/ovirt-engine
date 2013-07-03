package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class DbFacadeDAOTest extends BaseDAOTestCase {

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
    private static final String STATIC_VM_NAME = "rhel5-pool-50";
    private static final int NUM_OF_VM_STATIC_IN_FIXTURES = 3;
    private static final int NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM = 2;
    private static final int NUM_OF_USERS_IN_FIXTURES = 2;
    private static final Guid STORAGE_POOL_WITH_MASTER_UP = new Guid("386BFFD1-E7ED-4B08-BCE9-D7DF10F8C9A0");
    private static final Guid STORAGE_POOL_WITH_MASTER_DOWN = new Guid("72B9E200-F48B-4687-83F2-62828F249A47");
    private static final Guid VM_STATIC_GUID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS =
            new Guid("88D4301A-17AF-496C-A793-584640853D4B");
    private static final Guid VMT_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

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
    public void testDBConnectionWithoutConnection() {
        // setup
        DataSource result = null;
        Properties properties = new Properties();
        Config.setConfigUtils(new DBConfigUtils(false));

        InputStream is = null;
        try {
            is = super.getClass().getResourceAsStream(
                    "/test-database.properties");
            properties.load(is);
            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            result = new SingleConnectionDataSource(
                    properties.getProperty("database.url"),
                    // Deliberately puts a none existing user name, so an
                    // exception will be thrown when trying to check the
                    // connection
                    "no-such-username",
                    properties.getProperty("database.password"),
                    true
                    );
            DbFacade localDbFacade = new DbFacade();
            localDbFacade.setDbEngineDialect(DbFacadeLocator.loadDbEngineDialect());
            localDbFacade.setTemplate(localDbFacade.getDbEngineDialect().createJdbcTemplate(result));
            localDbFacade.checkDBConnection();
            fail("Connection should be down since the DataSource has an invalid username");
            // If DataAccessException is thrown - the test has succeeded. Was unable to do
            // with "expected" annotation, presumably since we are using DbUnit
        } catch (DataAccessException desiredException) {
            assertTrue(true);
            // If this exception is thrown we fail the test
        } catch (Exception undesiredException) {
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
        DbUser nonAdminUser = dbFacade.getDbUserDao().getByUsername("userportal2@testportal.redhat.com");

        assertNotNull(nonAdminUser);
        assertFalse(nonAdminUser.getLastAdminCheckStatus());

        // execute and validate when not admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getuser_id());
        nonAdminUser = dbFacade.getDbUserDao().get(nonAdminUser.getuser_id());

        assertFalse(nonAdminUser.getLastAdminCheckStatus());

        permissions perms = new permissions();
        perms.setRoleType(RoleType.ADMIN);

        // An available role from the fixtures
        perms.setrole_id(ADMIN_ROLE_TYPE_FROM_FIXTURE_ID);
        perms.setad_element_id(nonAdminUser.getuser_id());
        perms.setObjectId(SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);

        // Save the permission to the DB and make sure it has been saved
        dbFacade.getPermissionDao().save(perms);
        assertNotNull(dbFacade.getPermissionDao().get(perms.getId()));

        // execute and validate when admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getuser_id());
        nonAdminUser = dbFacade.getDbUserDao().get(nonAdminUser.getuser_id());

        assertTrue(nonAdminUser.getLastAdminCheckStatus());
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
    public void testIsStoragePoolMasterUpWhenDown() {
        StoragePool storagePoolToCheck = dbFacade.getStoragePoolDao().get(STORAGE_POOL_WITH_MASTER_DOWN);
        assertNotNull(storagePoolToCheck);

        Guid masterStorageDomainGuid =
                dbFacade.getStorageDomainDao().getMasterStorageDomainIdForPool(STORAGE_POOL_WITH_MASTER_DOWN);
        assertNotNull(masterStorageDomainGuid);

        StoragePoolIsoMap storagePoolIsoMapToCheck = dbFacade.getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                masterStorageDomainGuid, storagePoolToCheck.getId()));
        assertNotNull(storagePoolIsoMapToCheck);

        storagePoolIsoMapToCheck.setstatus(StorageDomainStatus.InActive);
        dbFacade.getStoragePoolIsoMapDao().update(storagePoolIsoMapToCheck);
        assertFalse(dbFacade.isStoragePoolMasterUp(STORAGE_POOL_WITH_MASTER_DOWN));
    }

    @Test
    public void testIsStoragePoolMasterUpWhenUp() {
        assertTrue(dbFacade.isStoragePoolMasterUp(STORAGE_POOL_WITH_MASTER_UP));
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
        tags tag = dbFacade.getTagDao().get(TAG_ID);
        assertNotNull(tag);
        String name = tag.gettag_name();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(TAG_ID, VdcObjectType.Tags)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForBookmark() {
        Bookmark bookmark = dbFacade.getBookmarkDao().get(BOOKMARK_ID);
        assertNotNull(bookmark);
        String name = bookmark.getbookmark_name();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(BOOKMARK_ID, VdcObjectType.Bookmarks)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForCluster() {
        VDSGroup vdsGroup = dbFacade.getVdsGroupDao().get(CLUSTER_ID);
        assertNotNull(vdsGroup);
        String name = vdsGroup.getName();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(CLUSTER_ID, VdcObjectType.VdsGroups)));
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
        String name = dbUser.getusername();
        assertTrue(name.equals(dbFacade.getEntityNameByIdAndType(USER_ID, VdcObjectType.User)));
    }

    @Test
    public void testGetEntityNameByIdAndTypeForRole() {
        Role role = dbFacade.getRoleDao().get(ROLE_ID);
        assertNotNull(role);
        String name = role.getname();
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
    public void testGetEntityPermissions() {
        // Should not return null since the user has the relevant permission
        assertNotNull(dbFacade.getEntityPermissions(DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS,
                ActionGroup.VM_BASIC_OPERATIONS,
                VMT_ID,
                VdcObjectType.VM));

        // Should return null since the user does not has the relevant permission
        assertNull(dbFacade.getEntityPermissions(DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS,
                ActionGroup.CREATE_TEMPLATE,
                VMT_ID,
                VdcObjectType.VM));
    }

    @Test
    public void testGetEntityPermissionsByUserAndGroups() {
        // Should not return null since the user has the relevant permission
        assertNotNull(dbFacade.getEntityPermissionsForUserAndGroups(Guid.newGuid(),
                DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS.toString(),
                ActionGroup.VM_BASIC_OPERATIONS,
                VMT_ID,
                VdcObjectType.VM,
                false));

        // Should return null since the user does not has the relevant permission
        assertNull(dbFacade.getEntityPermissionsForUserAndGroups(Guid.newGuid(),
                DIRECTORY_ELEMENT_ID_WITH_BASIC_PERMISSIONS.toString(),
                ActionGroup.CREATE_TEMPLATE,
                VMT_ID,
                VdcObjectType.VM,
                false));
    }

}
