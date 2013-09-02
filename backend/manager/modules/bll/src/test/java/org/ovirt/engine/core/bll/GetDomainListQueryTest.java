package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for the {@link GetDomainListQuery} class.
 */
public class GetDomainListQueryTest extends AbstractQueryTest<GetDomainListParameters, GetDomainListQuery<GetDomainListParameters>> {

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.AdminDomain, "internal"),
            mockConfig(ConfigValues.DomainName, "zzz,aaa")
            );

    @Test
    public void testImplicitNoFilter() {
        getQuery().executeQueryCommand();
        assertTrue("Wrong filtered domains", CollectionUtils.isEqualCollection(
                (Collection<String>) getQuery().getQueryReturnValue().getReturnValue(),
                Arrays.asList("aaa", "internal", "zzz")));
    }

    @Test
    public void testFilter() {
        doReturn(true).when(getQueryParameters()).getFilterInternalDomain();
        getQuery().executeQueryCommand();
        assertTrue("Wrong filtered domains", CollectionUtils.isEqualCollection(
                (Collection<String>) getQuery().getQueryReturnValue().getReturnValue(),
                Arrays.asList("aaa", "zzz")));
    }

    @Test
    public void testExplicitNoFilter() {
        doReturn(false).when(getQueryParameters()).getFilterInternalDomain();
        getQuery().executeQueryCommand();
        assertTrue("Wrong filtered domains", CollectionUtils.isEqualCollection(
                (Collection<String>) getQuery().getQueryReturnValue().getReturnValue(),
                Arrays.asList("aaa", "internal", "zzz")));
    }
}


