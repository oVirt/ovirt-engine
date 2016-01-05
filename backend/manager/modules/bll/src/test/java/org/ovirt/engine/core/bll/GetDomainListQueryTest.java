package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * A test case for the {@link GetDomainListQuery} class.
 */
public class GetDomainListQueryTest
        extends AbstractQueryTest<VdcQueryParametersBase, GetDomainListQuery<VdcQueryParametersBase>> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        doReturn(getDomainListResult()).when(getQuery()).getDomainList();
    }

    private Map<String, Object> getDomainListResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("result", Arrays.asList("aaa", "internal", "zzz"));
        return result;
    }

    @Test
    public void test() {
        getQuery().executeQueryCommand();
        assertTrue("Wrong filtered domains", CollectionUtils.isEqualCollection(
                getQuery().getQueryReturnValue().getReturnValue(),
                Arrays.asList("aaa", "internal", "zzz")));
    }
}


