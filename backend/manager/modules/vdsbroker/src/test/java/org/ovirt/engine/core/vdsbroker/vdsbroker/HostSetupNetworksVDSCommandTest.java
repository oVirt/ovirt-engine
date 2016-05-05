package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class HostSetupNetworksVDSCommandTest {

    private HostSetupNetworksVDSCommand underTest;
    private HostSetupNetworksVdsCommandParameters hostSetupNetworksVdsCommandParameters;

    @Mock
    private IVdsServer mockVdsServer;
    @Mock
    private HostNetwork mockHostNetwork;

    @Before
    public void setUp() {
        hostSetupNetworksVdsCommandParameters = new HostSetupNetworksVdsCommandParameters();
        underTest = new TestableHostSetupNetworksVDSCommand(hostSetupNetworksVdsCommandParameters);
    }

    @Test
    public void testAddIpv4BootProtocolWithNoAddressSet() {
        final Map<String, Object> opts = new HashMap<>();

        underTest.addIpv4BootProtocol(opts, mockHostNetwork);

        assertTrue(opts.isEmpty());
    }

    private class TestableHostSetupNetworksVDSCommand extends HostSetupNetworksVDSCommand {
        public TestableHostSetupNetworksVDSCommand(HostSetupNetworksVdsCommandParameters parameters) {
            super(parameters);
        }

        @Override
        protected IVdsServer initializeVdsBroker(Guid vdsId) {
            return mockVdsServer;
        }
    }
}
