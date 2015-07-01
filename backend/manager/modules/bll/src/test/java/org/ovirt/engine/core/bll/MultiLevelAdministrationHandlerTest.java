/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//package org.ovirt.engine.core.bll;
//
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.assertFalse;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//import static org.powermock.api.mockito.PowerMockito.when;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.ovirt.engine.core.common.businessentities.RoleType;
//import org.ovirt.engine.core.common.businessentities.Permissions;
//import org.ovirt.engine.core.common.businessentities.Role;
//import org.ovirt.engine.core.common.users.VdcUser;
//import org.ovirt.engine.core.compat.Guid;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.dao.PermissionDao;
//import org.ovirt.engine.core.dao.RoleDao;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({DbFacade.class, MultiLevelAdministrationHandler.class})
//@PowerMockIgnore("org.apache.log4j.*")
//public class MultiLevelAdministrationHandlerTest {
//
//    private DbFacade dbFacade = mock(DbFacade.class);
//    private Guid adElementId = Guid.NewGuid();
//    private Guid objectId = new Guid("aaa00000-0000-0000-0000-123456789aaa");
//
//    @Mock
//    PermissionDao permissionDao;
//
//    @Mock
//    RoleDao roleDao;
//
//    private DbFacade getDbFacadeMock() {
//        return dbFacade;
//    }
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        mockStatic(DbFacade.class);
//        when(DbFacade.getInstance()).thenReturn(getDbFacadeMock());
//        when(getDbFacadeMock().getPermissionDao()).thenReturn(permissionDao);
//        when(getDbFacadeMock().getRoleDao()).thenReturn(roleDao);
//    }
//
//    // Tests for isAdminUser Method
//    @Test
//    public void isAdminUserTrue() throws Exception {
//        List<Role> roles = new ArrayList<Role>();
//        Role role = new Role("admin role", adElementId, "admin");
//        role.setType(RoleType.ADMIN);
//        roles.add(role);
//        when(DbFacade.getInstance().getRoleDao().getAllForUserAndGroups(adElementId, "")).thenReturn(roles);
//        VdcUser user = new VdcUser();
//        user.setGroupIds("");
//        user.setUserId(adElementId);
//        assertTrue(MultiLevelAdministrationHandler.isAdminUser(user));
//    }
//
//    @Test
//    public void isAdminUserFalse() throws Exception {
//        List<Role> roles = new ArrayList<Role>();
//        Role role = new Role("user role", adElementId, "user");
//        role.setType(RoleType.USER);
//        roles.add(role);
//        when(DbFacade.getInstance().getRoleDao().getAllForUserAndGroups(adElementId, "")).thenReturn(roles);
//        VdcUser user = new VdcUser();
//        user.setGroupIds("");
//        user.setUserId(adElementId);
//        assertFalse(MultiLevelAdministrationHandler.isAdminUser(user));
//    }
//
//    // Tests for isLastSuperUserGroup Method
//    @Test
//    public void isLastSuperUserGroupTrue() throws Exception {
//        List<permissions> permissions = new ArrayList<permissions>();
//        permissions permisson = new permissions(adElementId,  objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions.add(permisson);
//        when(DbFacade.getInstance().getPermissionDao().getAllForAdElement(adElementId)).thenReturn(permissions);
//        when(DbFacade.getInstance().getPermissionDao().getAllForRole(PredefinedRoles.SUPER_USER.getId())).thenReturn(permissions);
//        assertTrue(MultiLevelAdministrationHandler.isLastSuperUserGroup(adElementId));
//    }
//
//    @Test
//    public void isLastSuperUserGroupFalse() throws Exception {
//        List<permissions> permissions1 = new ArrayList<permissions>();
//        List<permissions> permissions2 = new ArrayList<permissions>();
//        permissions permisson1 = new permissions(adElementId, objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions permisson2 = new permissions(Guid.NewGuid(), objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions1.add(permisson1);
//        permissions2.add(permisson1);
//        permissions2.add(permisson2);
//        when(DbFacade.getInstance().getPermissionDao().getAllForAdElement(adElementId)).thenReturn(permissions1);
//        when(DbFacade.getInstance().getPermissionDao().getAllForRole(PredefinedRoles.SUPER_USER.getId())).thenReturn(permissions2);
//        assertFalse(MultiLevelAdministrationHandler.isLastSuperUserGroup(adElementId));
//    }
//
//    // Tests for isLastSuperUserPermission Method
//
//    @Test
//    public void isLastSuperUserPermissionTrue() throws Exception {
//        List<permissions> permissions = new ArrayList<permissions>();
//        permissions permisson = new permissions(adElementId,  objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions.add(permisson);
//        when(DbFacade.getInstance().getPermissionDao().getAllForRole(PredefinedRoles.SUPER_USER.getId())).thenReturn(permissions);
//        assertTrue(MultiLevelAdministrationHandler.isLastSuperUserPermission(permisson.getrole_id()));
//    }
//
//    @Test
//    public void isLastSuperUserPermissionFalse() throws Exception {
//        List<permissions> permissions = new ArrayList<permissions>();
//        permissions permisson1 = new permissions(adElementId, objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions permisson2 = new permissions(Guid.NewGuid(), objectId, PredefinedRoles.SUPER_USER.getId());
//        permissions.add(permisson1);
//        permissions.add(permisson2);
//        when(DbFacade.getInstance().getPermissionDao().getAllForRole(PredefinedRoles.SUPER_USER.getId())).thenReturn(permissions);
//        assertFalse(MultiLevelAdministrationHandler.isLastSuperUserPermission(permisson1.getrole_id()));
//    }
//}
