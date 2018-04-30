package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkExclusivenessValidatorResolverTest {
    private NetworkExclusivenessValidatorResolver underTest;

    @Mock
    private NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator;

    @BeforeEach
    public void setUp() {
        underTest = new NetworkExclusivenessValidatorResolver(vlanUntaggedNetworkExclusivenessValidator);
    }

    @Test
    public void testResolveNetworkExclusivenessValidatorNew() {
        final NetworkExclusivenessValidator actual = underTest.resolveNetworkExclusivenessValidator();

        assertThat(actual, sameInstance(vlanUntaggedNetworkExclusivenessValidator));
    }
}
