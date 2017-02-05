package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;

@RunWith(Parameterized.class)
public class AddRoleWithActionGroupsCommandTest {

    /**
     * The test's parameters:
     * 1. A list of action groups
     * 2. Whether or not the role should be inheritable
     */
    @Parameters
    public static Object[][] data() {
        return new Object[][]
        { { false, Collections.emptyList() },
                { false, Collections.singletonList(ActionGroup.CREATE_VM) },
                { true, Collections.singletonList(ActionGroup.CONFIGURE_ENGINE) },
                { true, Arrays.asList(ActionGroup.CONFIGURE_ENGINE, ActionGroup.CREATE_VM) },
        };
    }

    private boolean shouldBeInheritable;
    private RoleWithActionGroupsParameters params;
    private AddRoleWithActionGroupsCommand<RoleWithActionGroupsParameters> command;

    public AddRoleWithActionGroupsCommandTest(boolean shouldBeInheritable, List<ActionGroup> groups) {
        this.shouldBeInheritable = shouldBeInheritable;

        params = new RoleWithActionGroupsParameters(new Role(), new ArrayList<>(groups));
        command = new AddRoleWithActionGroupsCommand<>(params, null);
    }

    @Test
    public void testPrepareRoleForCommandNoGroups() {
        command.prepareRoleForCommand();
        assertEquals("Wrong inheritable state for command", shouldBeInheritable, params.getRole()
                .allowsViewingChildren());
    }
}
