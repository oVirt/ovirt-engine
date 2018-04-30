package org.ovirt.engine.core.bll.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
public class ExternalNetworkManagerTest {

    private static final Guid NIC_ID = Guid.newGuid();
    private static final String NIC_NAME = "nic name";
    private static final Guid PROVIDER_ID = Guid.newGuid();
    private static final String PROVIDER_NAME = "provider name";

    @Mock
    @InjectedMock
    public AuditLogDirector auditLogDirector;

    @Mock
    @InjectedMock
    public ProviderDao providerDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private NetworkProviderProxy networkProviderProxy;

    @Captor
    private ArgumentCaptor<AuditLogable> auditLogableCaptor;

    private VmNic nic = new VmNic();

    private Network network = createNetwork();

    @InjectMocks
    private ExternalNetworkManager underTest = new ExternalNetworkManager(nic, network);

    private ProviderNetwork providerNetwork;
    private Provider provider;

    @BeforeEach
    public void setUp() {
        provider = new Provider<>();
        when(providerDao.get(PROVIDER_ID)).thenReturn(provider);
        when(providerProxyFactory.create(provider)).thenReturn(networkProviderProxy);
    }

    private Network createNetwork() {
        final Network network = new Network();
        providerNetwork = new ProviderNetwork();
        providerNetwork.setProviderId(PROVIDER_ID);
        network.setProvidedBy(providerNetwork);
        return network;
    }

    @Test
    public void testDeallocateIfExternalPositive() {
        underTest.deallocateIfExternal();

        verify(networkProviderProxy).deallocate(nic);
    }

    @Test
    public void testDeallocateIfExternalThrowException() {
        nic.setName(NIC_NAME);
        nic.setId(NIC_ID);
        provider.setName(PROVIDER_NAME);
        doThrow(new EngineException()).when(networkProviderProxy).deallocate(nic);

        underTest.deallocateIfExternal();

        verify(auditLogDirector).log(
                auditLogableCaptor.capture(),
                same(AuditLogType.REMOVE_PORT_FROM_EXTERNAL_PROVIDER_FAILED));

        final Map<String, String> capturedCustomValues = auditLogableCaptor.getValue().getCustomValues();
        assertThat(capturedCustomValues, hasEntry("nicname", NIC_NAME));
        assertThat(capturedCustomValues, hasEntry("nicid", NIC_ID.toString()));
        assertThat(capturedCustomValues, hasEntry("providername", PROVIDER_NAME));
    }
}
