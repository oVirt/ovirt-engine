package org.ovirt.engine.core.itests;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;

/**
 * Test class for Queries
 *
 */
public class QueriesTest extends AbstractBackendTest {

    /**
     * Add permissions for the current user as a preparation for the next test.
     *
     */
    @Test
    public void addPermissions() {
        // add CLUSTER_ADMIN on SYSTEM to current new user
        runAsSuperAdmin();
        permissions perms = new permissions(getUser().getUserId(), Guid.NewGuid(),
                PredefinedRoles.CLUSTER_ADMIN.getId());
        perms.setObjectId(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);
        PermissionsOperationsParametes parameters = new PermissionsOperationsParametes(perms);

        VdcReturnValueBase runAction = backend.RunAction(VdcActionType.AddPermission, parameters);
        Assert.assertTrue(runAction.getSucceeded());

    }

    /**
     * Retrieve permissions tree for the current user on a cluster.
     *
     * result - success if the permissions list has at least one permission
     */
    @Test
    public void getPermissionsByEntityId() {
        // use basic setup to fetch permission on VM through cluster
        runAsSuperAdmin();
        Guid clusterId = getBasicSetup().getCluster().getID();
        GetPermissionsForObjectParameters parameters = new GetPermissionsForObjectParameters(clusterId);
        parameters.setDirectOnly(false);
        parameters.setObjectId(clusterId);
        parameters.setVdcObjectType(VdcObjectType.VdsGroups);

        VdcQueryReturnValue runQuery = backend.RunQuery(VdcQueryType.GetPermissionsForObject,
                parameters);
        ArrayList<permissions> perms = (ArrayList<permissions>) runQuery.getReturnValue();
        Assert.assertTrue(!perms.isEmpty());
    }

    @Test
    public void getRolesForDelegation() {
        VdcQueryReturnValue runQuery =
            backend.RunQuery(VdcQueryType.GetRolesForDelegationByUser, sessionize(new VdcQueryParametersBase()));
        ArrayList<roles> roles = (ArrayList<org.ovirt.engine.core.common.businessentities.roles>) runQuery.getReturnValue();
        Assert.assertNotNull(roles);

    }

    @Test
    public void getCustomPermissions() {
        VdcQueryReturnValue runQuery =
            backend.RunQuery(VdcQueryType.GetVmCustomProperties, sessionize(new VdcQueryParametersBase()));
        String properties = (String) runQuery.getReturnValue();
        Assert.assertTrue(properties.contains("sap_agent"));

    }
}
