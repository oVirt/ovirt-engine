package org.ovirt.engine.core.dao;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class DaoFactoryTest extends Assert {

    private <T extends DAO> void testLoadClass(Class<T> clz) {
        T instance = DaoFactory.get(clz);
        assertNotNull(instance);
        assertTrue(clz.isInstance(instance));
    }

    @Test public void testActionGroup()             { testLoadClass(ActionGroupDAO.class); }
    @Test public void testAdGroup()                 { testLoadClass(DbGroupDAO.class); }
    @Test public void testAsyncTask()               { testLoadClass(AsyncTaskDAO.class); }
    @Test public void testAuditLog()                { testLoadClass(AuditLogDAO.class); }
    @Test public void testBookmark()                { testLoadClass(BookmarkDAO.class); }
    @Test public void testDbUser()                  { testLoadClass(DbUserDAO.class); }
    @Test public void testDiskImage()               { testLoadClass(DiskImageDAO.class); }
    @Test public void testEvent()                   { testLoadClass(EventDAO.class); }
    @Test public void testInterface()               { testLoadClass(InterfaceDao.class); }
    @Test public void testLun()                     { testLoadClass(LunDAO.class); }
    @Test public void testNetwork()                 { testLoadClass(NetworkDao.class); }
    @Test public void testNetworkCluster()          { testLoadClass(NetworkClusterDao.class); }
    @Test public void testPermission()              { testLoadClass(PermissionDAO.class); }
    @Test public void testRole()                    { testLoadClass(RoleDAO.class); }
    @Test public void testRoleGroupMap()            { testLoadClass(RoleGroupMapDAO.class); }
    @Test public void testStorageDomain()           { testLoadClass(StorageDomainDAO.class); }
    @Test public void testStoragePool()             { testLoadClass(StoragePoolDAO.class); }
    @Test public void testStorageServerConnection() { testLoadClass(StorageServerConnectionDAO.class); }
    @Test public void testTag()                     { testLoadClass(TagDAO.class); }
    @Test public void testVdcOption()               { testLoadClass(VdcOptionDAO.class); }
    @Test public void testVds()                     { testLoadClass(VdsDAO.class); }
    @Test public void testVdsGroup()                { testLoadClass(VdsGroupDAO.class); }
    @Test public void testVm()                      { testLoadClass(VmDAO.class); }

    private class NonExistentDAO implements DAO {
    }

    @Test
    public void testException() {
        try {
            DaoFactory.get(NonExistentDAO.class);
            fail("expected DaoFactoryException");
        } catch (DaoFactoryException ex) {
            assertEquals(NonExistentDAO.class, ex.getDaoType());
            assertEquals("engine-daos.properties", ex.getPropsFile());
        }
    }
}
