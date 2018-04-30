package org.ovirt.engine.core.bll.quota;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.ActionType;

public class QuotaDependencyTest {
    public static Stream<ActionType> quotaDependency() {
        return Arrays.stream(ActionType.values()).filter(a -> a.getQuotaDependency() != ActionType.QuotaDependency.NONE);
    }

    @ParameterizedTest
    @MethodSource
    public void quotaDependency(ActionType actionType) {
        Class commandClass = CommandsFactory.getCommandClass(actionType.name());

        // if command is deprecated or internal - skip it
        if (commandClass.getAnnotation(Deprecated.class) != null
                || commandClass.getAnnotation(InternalCommandAttribute.class) != null) {
            return;
        }

        switch (actionType.getQuotaDependency()) {
        case CLUSTER:
            assertCommandIsQuotaVdsDependent(commandClass);
            break;
        case STORAGE:
            assertCommandIsQuotaStorageDependent(commandClass);
            break;
        case BOTH:
            assertCommandIsQuotaVdsDependent(commandClass);
            assertCommandIsQuotaStorageDependent(commandClass);
            break;
        default:
            break;
        }
    }

    private void assertCommandIsQuotaStorageDependent(Class commandClass) {
        assertTrue(isImplementingRecursive(commandClass, QuotaStorageDependent.class),
                String.format("The command %s was expected to implement QuotaStorageDependent interface",
                        commandClass.getName()));
    }

    private void assertCommandIsQuotaVdsDependent(Class commandClass) {
        assertTrue(isImplementingRecursive(commandClass, QuotaVdsDependent.class),
                String.format("The command %s was expected to implement QuotaVdsDependent interface",
                        commandClass.getName()));
    }

    private boolean isImplementingRecursive(Class commandClass, Class interfaceClass) {
        if (Arrays.asList(commandClass.getInterfaces()).contains(interfaceClass)) {
            return true;
        } else {
            return !commandClass.getSuperclass().equals(CommandBase.class)
                    && isImplementingRecursive(commandClass.getSuperclass(), interfaceClass);
        }
    }
}
