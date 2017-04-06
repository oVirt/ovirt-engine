package org.ovirt.engine.core.bll.network;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.InjectorRule;

@RunWith(MockitoJUnitRunner.class)
public class ExternalNetworkManagerTest {

    private static final Guid NIC_ID = Guid.newGuid();
    private static final String NIC_NAME = "nic name";
    private static final Guid PROVIDER_ID = Guid.newGuid();
    private static final String PROVIDER_NAME = "provider name";

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private NetworkProviderProxy networkProviderProxy;

    @Captor
    private ArgumentCaptor<AuditLogableBase> auditLogableBaseCaptor;

    private ExternalNetworkManager underTest;

    private VmNic nic;
    private Network network;
    private ProviderNetwork providerNetwork;
    private Provider provider;

    @Before
    public void setUp() {
        nic = new VmNic();
        network = createNetwork();

        underTest = spy(new ExternalNetworkManager(nic, network));

        injectorRule.bind(ProviderDao.class, providerDao);
        injectorRule.bind(AuditLogDirector.class, auditLogDirector);

        provider = new Provider<>();
        when(providerDao.get(PROVIDER_ID)).thenReturn(provider);
        doReturn(providerProxyFactory).when(underTest).getProviderProxyFactory();
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
                auditLogableBaseCaptor.capture(),
                same(AuditLogType.REMOVE_PORT_FROM_EXTERNAL_PROVIDER_FAILED));

        final Map<String, String> capturedCustomValues = auditLogableBaseCaptor.getValue().getCustomValues();
        assertThat(capturedCustomValues, hasEntry("nicname", NIC_NAME));
        assertThat(capturedCustomValues, hasEntry("nicid", NIC_ID.toString()));
        assertThat(capturedCustomValues, hasEntry("providername", PROVIDER_NAME));
    }
}
