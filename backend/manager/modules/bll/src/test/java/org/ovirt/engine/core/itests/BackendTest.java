package org.ovirt.engine.core.itests;

import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import javax.naming.NamingException;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 11, 2009 Time: 3:52:06 PM To change this template use File |
 * Settings | File Templates.
 */
public class BackendTest extends AbstractBackendTest {

    @Test
    public void createBackend() throws NamingException {
        assertNotNull(backend);
    }

    @Test
    public void runSingleCommand() {
        Role role = new Role();
        role.setId(Guid.NewGuid());
        role.setname("Random_" + new Random().nextInt());
        role.setType(RoleType.USER);
        ArrayList<ActionGroup> groups = new ArrayList<ActionGroup>();
        groups.add(ActionGroup.CHANGE_VM_CD);
        VdcReturnValueBase value = backend.runInternalAction(VdcActionType.AddRoleWithActionGroups,
                new RoleWithActionGroupsParameters(role, groups));
        assertTrue(value.getSucceeded());
        System.out.println(value);
    }

    @Test
    public void runSingleQuery() {
        MultilevelAdministrationsQueriesParameters parameters = new MultilevelAdministrationsQueriesParameters();
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.GetAllRoles, parameters);
        assertTrue(value.getSucceeded());
        Collection<Role> roles = (Collection<Role>) value.getReturnValue();
        for (Role role : roles) {
            System.out.println(role.getname());
        }
    }

}
