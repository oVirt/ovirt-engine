package org.ovirt.engine.core.bll;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class QueryExistenceTest {

    @Test
    public void testQueryClassExistence() {
        CommandEnumTestUtils.testCommandsExist(VdcQueryType.class, vct -> CommandsFactory.getQueryClass(vct.name()));
    }

    @Test
    public void testEnumClassExistence() {
        CommandEnumTestUtils.testCommandClassHasEnum(VdcQueryType.class, QueriesCommandBase.class, "Query");
    }
}
