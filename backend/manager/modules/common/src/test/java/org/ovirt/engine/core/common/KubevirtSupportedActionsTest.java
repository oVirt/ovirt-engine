package org.ovirt.engine.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Managed;

/**
 * A test case for the {@link KubevirtSupportedActions} class.
 */
public class KubevirtSupportedActionsTest {

    static class UnmanagedEntity implements Managed {
        @Override
        public boolean isManaged() {
            return false;
        }
    }

    static class ManagedEntity implements Managed {
    }

    public static Stream<Arguments> isActionSupported() {
        ManagedEntity managedEntity = new ManagedEntity();
        UnmanagedEntity unmanagedEntity = new UnmanagedEntity();

        return Stream.of(
                Arguments.of(managedEntity, ActionType.RemoveVds, true),
                Arguments.of(managedEntity, ActionType.RunVm, true),
                Arguments.of(unmanagedEntity, ActionType.RunVm, true),
                Arguments.of(unmanagedEntity, ActionType.RemoveVds, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void isActionSupported(Managed toTest, ActionType action, boolean result) {
        assertEquals(result, KubevirtSupportedActions.isActionSupported(toTest, action));
    }
}
