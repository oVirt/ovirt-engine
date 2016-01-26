package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.RoleGroupMapDao;

/** An abstract base class for tests for roles commands */
public abstract class AbstractRolesCommandTestBase extends BaseCommandTest {
    private RolesParameterBase params;
    private RolesCommandBase<? extends RolesParameterBase> command;
    private Role role;

    @Mock
    private RoleDao roleDaoMock;

    @Mock
    private RoleGroupMapDao roleGroupMapDaoMock;

    @Before
    public void setUp() {
        params = generateParameters();
        command = spy(generateCommand());
        role = new Role();
        role.setId(params.getRoleId());

        doReturn(roleDaoMock).when(command).getRoleDao();
        when(roleDaoMock.get(params.getRoleId())).thenReturn(role);
        doReturn(roleGroupMapDaoMock).when(command).getRoleGroupMapDao();
    }

    protected RolesParameterBase generateParameters() {
        return new RolesParameterBase(Guid.newGuid());
    }

    protected abstract RolesCommandBase<? extends RolesParameterBase> generateCommand();

    protected RolesParameterBase getParams() {
        return params;
    }

    protected RolesCommandBase<? extends RolesParameterBase> getCommand() {
        return command;
    }

    protected Role getRole() {
        return role;
    }

    protected RoleDao getRoleDaoMock() {
        return roleDaoMock;
    }

    protected RoleGroupMapDao getRoleGroupMapDaoMock() {
        return roleGroupMapDaoMock;
    }
}
