package org.ovirt.engine.core.itests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Test class for specific permissions commands.
 *
 */
public class PermissionTest extends AbstractBackendTest {

    /**
     * Test adding permission for a random user Steps are: - choose a random user from the active directory - activate
     * the test's basic setup to create entities to work with. Choose the newly created DataCenter ID - create
     * permissions with user Id with role DATA_CENTER_ADMIN on the created DataCenter
     *
     * result - test succeeded if the AddPermissionCommand returned true
     */
    @Test
    public void addPermission() {
        runAsSuperAdmin();

        DbUser user = new DbUser(new AdUser("jnuit-testUser", "123345", Guid.NewGuid(), "example.com"));
        DbFacade.getInstance().getDbUserDao().save(user);
        permissions perms = new permissions(user.getuser_id(), Guid.NewGuid(), PredefinedRoles.DATA_CENTER_ADMIN
                .getId());
        perms.setObjectId(getBasicSetup().getDataCenter().getId());
        perms.setObjectType(VdcObjectType.StoragePool);
        PermissionsOperationsParametes parameters = new PermissionsOperationsParametes(perms);
        vdcUserFrom(user, parameters);

        VdcReturnValueBase runAction = backend.RunAction(VdcActionType.AddPermission, parameters);

        assertTrue(runAction.getSucceeded());
        VdcReturnValueBase removeUserAction =
                backend.runInternalAction(VdcActionType.RemoveUser,
                                          sessionize(new AdElementParametersBase(user.getuser_id())));
        assertTrue(removeUserAction.getSucceeded());
    }

    /**
     * Test adding system permission for a random user Steps are: - choose a random user from the active directory -
     * activate the test's basic setup to create entities to work with. Choose the newly created DataCenter ID - create
     * permissions with user Id with role DATA_CENTER_ADMIN using AddSystemPermissionsCommand
     *
     * result - test succeeded if the AddSystemPermissionsCommand returned true
     */
    @Test
    public void addSystemPermission() {
        runAsSuperAdmin();

        DbUser user = new DbUser(new AdUser("jnuit-testUser1", "123345", Guid.NewGuid(), "example.com"));
        DbFacade.getInstance().getDbUserDao().save(user);

        permissions perms = new permissions(user.getuser_id(), Guid.NewGuid(), PredefinedRoles.DATA_CENTER_ADMIN
                .getId());

        PermissionsOperationsParametes parameters = new PermissionsOperationsParametes(perms);
        vdcUserFrom(user, parameters);

        VdcReturnValueBase runAction = backend.RunAction(VdcActionType.AddSystemPermission, parameters);

        assertTrue(runAction.getSucceeded());
        VdcReturnValueBase removeUserAction =
                backend.runInternalAction(VdcActionType.RemoveUser,
                                          sessionize(new AdElementParametersBase(user.getuser_id())));
        assertTrue(removeUserAction.getSucceeded());
    }

    private VdcUser vdcUserFrom(DbUser user, PermissionsOperationsParametes parameters) {
        VdcUser vdcUser = new VdcUser();
        vdcUser.setDomainControler(user.getdomain());
        vdcUser.setUserName(user.getusername());
        vdcUser.setUserId(user.getuser_id());
        parameters.setVdcUser(vdcUser);
        return vdcUser;
    }

    /**
     * Test getSystemPermissionsQuery
     *
     * result - success if the query returned a list with records. If the former test {@link #addSystemPermission()}
     * failed we should expect this to fail as well if there no system permissions in the system already
     */
    @Test
    public void getSystemPermissions() {
        runAsSuperAdmin();
        VdcQueryReturnValue runQuery = backend
                .RunQuery(VdcQueryType.GetSystemPermissions, sessionize(new VdcQueryParametersBase()));
        Assert.assertTrue(((ArrayList<permissions>) runQuery.getReturnValue()).size() > 0);
    }
}
