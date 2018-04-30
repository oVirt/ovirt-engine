package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class HostSetupNetworksVDSCommandTest {

    private HostSetupNetworksVDSCommand underTest;
    private HostSetupNetworksVdsCommandParameters hostSetupNetworksVdsCommandParameters;

    @Mock
    private IVdsServer mockVdsServer;
    @Mock
    private HostNetwork mockHostNetwork;

    @BeforeEach
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
