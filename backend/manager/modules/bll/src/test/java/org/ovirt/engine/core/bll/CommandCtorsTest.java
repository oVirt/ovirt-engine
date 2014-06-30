package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public class CommandCtorsTest {

    @Test
    public void testInternalAnnotationCtors() {

        for (VdcActionType commandType : VdcActionType.values()) {
            if (!commandType.equals(VdcActionType.Unknown)) {
                Class<CommandBase<? extends VdcActionParametersBase>> commandClass = CommandsFactory.getCommandClass(commandType.toString());
                if (commandClass != null) {
                    if (commandClass.isAnnotationPresent(InternalCommandAttribute.class)) {
                        boolean foundContextCtor = false;
                        for (Constructor<?> ctor : commandClass.getConstructors()) {
                          if (ctor.getParameterTypes()[ctor.getParameterTypes().length-1].equals(CommandContext.class)) {
                              foundContextCtor = true;
                          }
                        }
                        assertTrue("no context ctor for class " + commandClass.getSimpleName(), foundContextCtor);
                    }
                }
            }
        }
    }

}
