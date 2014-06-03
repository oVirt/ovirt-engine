package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for the {@link GetDomainListQuery} class.
 */
public class GetDomainListQueryTest
        extends AbstractQueryTest<GetDomainListParameters, GetDomainListQuery<GetDomainListParameters>> {

    // The name of the internal authentication profile:
    private static final String INTERNAL = "internal";

    // The list of authentication profile names:
    private String[] NAMES = {
        INTERNAL,
        "zzz",
        "aaa"
    };

    @ClassRule
    public static final MockConfigRule MCR = new MockConfigRule(
        mockConfig(ConfigValues.AdminDomain, INTERNAL)
    );

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        for (String name : NAMES) {
            setUpProfileMock(name);
        }
    }

    /**
     * Prepares a mock for an authentication profile, including the mocks for the dependent authenticator and directory,
     * all of them with the same name.
     *
     * @param name the name for the mocked authenticator, directory and authentication profile
     */
    private void setUpProfileMock(String name) {
        ExtensionProxy authzMock = mock(ExtensionProxy.class);
        ExtMap mockContext = mock(ExtMap.class);
        doReturn(name).when(mockContext).get(Base.ContextKeys.INSTANCE_NAME);
        doReturn(mockContext).when(authzMock).getContext();
        ExtensionProxy authnMock = mock(ExtensionProxy.class);
        doReturn(mockContext).when(authnMock).getContext();
        AuthenticationProfile profileMock = mock(AuthenticationProfile.class);
        doReturn(name).when(profileMock).getName();
        doReturn(authzMock).when(profileMock).getAuthz();
        doReturn(authnMock).when(profileMock).getAuthn();
        AuthenticationProfileRepository.getInstance().registerProfile(profileMock);
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


