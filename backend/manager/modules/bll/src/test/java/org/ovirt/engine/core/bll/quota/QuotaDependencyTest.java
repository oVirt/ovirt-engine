package org.ovirt.engine.core.bll.quota;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.VdcActionType;

@RunWith(Theories.class)
public class QuotaDependencyTest {

    @DataPoints
    public static VdcActionType[] data() {
        return VdcActionType.values();
    }

    @Theory
    public void quotaDependencyTest(VdcActionType vdcActionType) {
        if (vdcActionType.getQuotaDependency() != VdcActionType.QuotaDependency.NONE) {
            Class commandClass = CommandsFactory.getCommandClass(vdcActionType.name());

            // if command is deprecated or internal - skip it
            if (commandClass.getAnnotation(Deprecated.class) != null
                    || commandClass.getAnnotation(InternalCommandAttribute.class) != null) {
                return;
            }

            switch (vdcActionType.getQuotaDependency()) {
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
    }

    private void assertCommandIsQuotaStorageDependent(Class commandClass) {
        assertTrue(String.format("The command %s was expected to implement QuotaStorageDependent interface",
                commandClass.getName()),
                isImplementingRecursive(commandClass, QuotaStorageDependent.class));
    }

    private void assertCommandIsQuotaVdsDependent(Class commandClass) {
        assertTrue(String.format("The command %s was expected to implement QuotaVdsDependent interface",
                commandClass.getName()),
                isImplementingRecursive(commandClass, QuotaVdsDependent.class));
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
