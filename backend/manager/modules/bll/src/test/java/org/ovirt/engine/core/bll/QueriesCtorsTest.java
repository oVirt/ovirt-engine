package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class QueriesCtorsTest extends CtorsTestBase {
    private static Collection<Class<?>> queryClasses;

    @BeforeAll
    public static void initCommandsCollection() {
        queryClasses = commandsFromEnum(QueryType.class, CommandsFactory::getQueryClass);
    }

    @Override
    protected Collection<Class<?>> getClassesToTest() {
        return queryClasses;
    }

    @Override
    protected Stream<MandatoryCtorPredicate> getMandatoryCtorPredicates() {
        return Stream.of(new MandatoryCtorPredicate(QueryParametersBase.class, EngineContext.class));
    }
}
