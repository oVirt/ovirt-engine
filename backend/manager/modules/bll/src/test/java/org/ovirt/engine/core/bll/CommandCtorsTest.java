package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.compat.Guid;

public class CommandCtorsTest extends CtorsTestBase {

    private static Collection<Class<?>> commandClasses;

    @BeforeAll
    public static void initCommandsCollection() {
        commandClasses = commandsFromEnum(ActionType.class, CommandsFactory::getCommandClass);
    }

    @Override
    protected Collection<Class<?>> getClassesToTest() {
        return commandClasses;
    }

    @Override
    protected Stream<MandatoryCtorPredicate> getMandatoryCtorPredicates() {
        return Stream.of(new MandatoryCtorPredicate(ActionParametersBase.class, CommandContext.class),
                new MandatoryCtorPredicate(Guid.class) {
                    @Override
                    public boolean test(Class<?> commandClass) {
                        return !commandClass.isAnnotationPresent(NonTransactiveCommandAttribute.class) ||
                                !commandClass.getAnnotation(NonTransactiveCommandAttribute.class).forceCompensation() ||
                                super.test(commandClass);
                    }

                    @Override
                    protected String additionalInfo() {
                        return "its class is annotated with '" + NonTransactiveCommandAttribute.class.getSimpleName() +
                                "' and the annotation's 'forceCompensation' attribute is set to true.";
                    }
                });
    }
}
