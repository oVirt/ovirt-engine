package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;

public class AddRoleWithActionGroupsCommandTest {

    /**
     * The test's parameters:
     * 1. A list of action groups
     * 2. Whether or not the role should be inheritable
     */
    public static Stream<Arguments> prepareRoleForCommandNoGroups() {
        return Stream.of(
                Arguments.of(false, Collections.emptyList()),
                Arguments.of(false, Collections.singletonList(ActionGroup.CREATE_VM)),
                Arguments.of(true, Collections.singletonList(ActionGroup.CONFIGURE_ENGINE)),
                Arguments.of(true, Arrays.asList(ActionGroup.CONFIGURE_ENGINE, ActionGroup.CREATE_VM))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void prepareRoleForCommandNoGroups(boolean shouldBeInheritable, List<ActionGroup> groups) {
        RoleWithActionGroupsParameters params =
                new RoleWithActionGroupsParameters(new Role(), new ArrayList<>(groups));
        AddRoleWithActionGroupsCommand<RoleWithActionGroupsParameters> command =
                new AddRoleWithActionGroupsCommand<>(params, null);

        command.prepareRoleForCommand();
        assertEquals
                (shouldBeInheritable, params.getRole().allowsViewingChildren(), "Wrong inheritable state for command");
    }
}
