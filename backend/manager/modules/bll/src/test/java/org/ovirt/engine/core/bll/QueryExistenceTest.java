package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class QueryExistenceTest {

    @Test
    public void testQueryClassExistence() {
        List<VdcQueryType> missingQueries = new LinkedList<>();
        for (VdcQueryType vqt : VdcQueryType.values()) {
            if (vqt != VdcQueryType.Unknown) {
                if (CommandsFactory.getQueryClass(vqt.name()) == null) {
                    missingQueries.add(vqt);
                }
            }
        }
        assertTrue("VdcQueryType contains the following values that does not correspond to an existing query class: " + missingQueries,
                missingQueries.isEmpty());
    }
}
