package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetworkExclusivenessValidatorResolverTest {
    private NetworkExclusivenessValidatorResolver underTest;

    @Mock
    private NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator;

    @Before
    public void setUp() throws Exception {
        underTest = new NetworkExclusivenessValidatorResolver(vlanUntaggedNetworkExclusivenessValidator);
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorNew() {
        final NetworkExclusivenessValidator actual = underTest.resolveNetworkExclusivenessValidator();

        assertThat(actual, sameInstance(vlanUntaggedNetworkExclusivenessValidator));
    }
}
