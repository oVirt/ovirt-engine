package org.ovirt.engine.core.bll;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.queries.QueryType;

public class QueryExistenceTest {

    @Test
    public void testQueryClassExistence() {
        CommandEnumTestUtils.testCommandsExist(QueryType.class, vct -> CommandsFactory.getQueryClass(vct.name()));
    }

    @Test
    public void testEnumClassExistence() {
        CommandEnumTestUtils.testCommandClassHasEnum(QueryType.class, QueriesCommandBase.class, "Query");
    }
}
