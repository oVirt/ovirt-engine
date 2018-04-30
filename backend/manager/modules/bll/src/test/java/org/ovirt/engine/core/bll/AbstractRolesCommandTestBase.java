package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.RoleGroupMapDao;

/** An abstract base class for tests for roles commands */
public abstract class AbstractRolesCommandTestBase extends BaseCommandTest {
    private RolesParameterBase params = generateParameters();

    @InjectMocks
    private RolesCommandBase<? extends RolesParameterBase> command = generateCommand();
    private Role role;

    @Mock
    private RoleDao roleDaoMock;

    @Mock
    private RoleGroupMapDao roleGroupMapDaoMock;

    @BeforeEach
    public void setUp() {
        role = new Role();
        role.setId(params.getRoleId());

        when(roleDaoMock.get(params.getRoleId())).thenReturn(role);
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
