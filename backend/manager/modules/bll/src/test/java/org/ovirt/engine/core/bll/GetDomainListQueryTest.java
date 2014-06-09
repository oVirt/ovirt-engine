package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

/**
 * A test case for the {@link GetDomainListQuery} class.
 */
public class GetDomainListQueryTest
        extends AbstractQueryTest<GetDomainListParameters, GetDomainListQuery<GetDomainListParameters>> {

    private static class MockEngineExtensionsManager extends EngineExtensionsManager {

        private List<ExtensionProxy> extensions = new ArrayList<ExtensionProxy>();

        public void addExtension(ExtensionProxy extension) {
            extensions.add(extension);
        }

        @Override
        public List<ExtensionProxy> getExtensionsByService(String provides) {
            return extensions;
        }

        public void clear() {
            extensions.clear();
        }
    }

    // The name of the internal authentication profile:
    private static final String INTERNAL = "internal";

    // The list of authentication profile names:
    private String[] NAMES = {
            INTERNAL,
            "zzz",
            "aaa"
    };

    private MockEngineExtensionsManager extMgr = new MockEngineExtensionsManager();

    @ClassRule
    public static final MockConfigRule MCR = new MockConfigRule(
            mockConfig(ConfigValues.AdminDomain, INTERNAL)
            );

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        extMgr.clear();
        doReturn(extMgr).when(getQuery()).getExtensionsManager();
        for (String name : NAMES) {
            setUpAuthzMock(name);
        }
    }

    /**
     * Prepares a mock for an authentication profile, including the mocks for the dependent authenticator and directory,
     * all of them with the same name.
     *
     * @param name the name for the mocked authenticator, directory and authentication profile
     */
    private void setUpAuthzMock(String name) {
        ExtensionProxy authzMock = mock(ExtensionProxy.class);
        ExtMap mockContext = mock(ExtMap.class);
        doReturn(name).when(mockContext).get(Base.ContextKeys.INSTANCE_NAME);
        doReturn(Arrays.asList(Authz.class.getName())).when(mockContext).get(Base.ContextKeys.PROVIDES);
        doReturn(mockContext).when(authzMock).getContext();
        extMgr.addExtension(authzMock);
    }

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


