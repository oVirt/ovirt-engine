package org.ovirt.engine.core.bll.provider.network;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.openstack.ExternalNetworkProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SyncNetworkProviderCommandTest extends BaseCommandTest {
    private final Guid PROVIDER_ID = new Guid("000000000000-0000-0000-0000-00000001");
    private final String EXTERNAL_ID = "000000000000-0000-0000-0000-00000002";
    private final Guid DATACENTER_ID = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid NETWORK_ID = new Guid("000000000000-0000-0000-0000-00000005");
    private final Guid OBSOLETE_EXTERNAL_ID = new Guid("000000000000-0000-0000-0000-00000006");

    @Mock
    private BackendInternal backend;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VnicProfileDao vnicProfileDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private NetworkHelper networkHelper;

    @Mock
    private ExternalNetworkProviderProxy providerProxy;

    @Mock
    private AuditLogDirector auditLogDirector;

    @InjectMocks
    private SyncNetworkProviderCommand<IdParameters> command = new SyncNetworkProviderCommand<>(
            new IdParameters(PROVIDER_ID), CommandContext.createContext("context"));

    private Provider provider;

    @BeforeEach
    public void setUp() {
        when(providerProxyFactory.create(getProvider())).thenReturn(providerProxy);
        when(providerProxy.getAll()).thenReturn(getProviderNetworks());
        when(networkDao.getAllForProvider(PROVIDER_ID)).thenReturn(getDbNetworks());
        when(clusterDao.getAllClustersByDefaultNetworkProviderId(PROVIDER_ID)).thenReturn(getClusterList());
        when(vnicProfileDao.getAllForNetwork(NETWORK_ID)).thenReturn(Collections.emptyList());
        when(vmDao.getAllForNetwork(OBSOLETE_EXTERNAL_ID)).thenReturn(Collections.emptyList());

        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);

        when(backend.runInternalAction(eq(ActionType.RemoveNetwork), any(), any()))
                .thenReturn(returnValue);

        when(backend.runInternalAction(eq(ActionType.InternalImportExternalNetwork), any(), any()))
                .thenReturn(getImportNetworkReturnValue());

        when(networkHelper.attachNetworkToClusters(eq(NETWORK_ID), any())).thenReturn(returnValue);
    }

    private void setupProviderDao(Provider provider) {
        when(providerDao.get(PROVIDER_ID)).thenReturn(provider);
    }

    @Test
    public void testSyncSuccess() {
        setupProviderDao(getProvider());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
        verifyCalls();
    }

    @Test
    public void testInvalidProviderId() {
        setupProviderDao(null);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST);
    }

    @Test
    public void testInvalidProviderType() {
        Provider imageProvider = new Provider();
        imageProvider.setType(ProviderType.OPENSTACK_IMAGE);
        setupProviderDao(imageProvider);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NOT_NETWORK);
    }

    private Provider getProvider() {
        if (provider == null) {
            provider = new Provider();
            provider.setType(ProviderType.EXTERNAL_NETWORK);
            provider.setId(PROVIDER_ID);
        }
         return provider;
    }

    private List<Network> getProviderNetworks() {
        return getNetworkList(EXTERNAL_ID);
    }

    private List<Network> getDbNetworks() {
        List<Network> dbNetworks = getNetworkList(OBSOLETE_EXTERNAL_ID.toString());
        dbNetworks.get(0).setDataCenterId(DATACENTER_ID);
        return dbNetworks;
    }

    private List<Network> getNetworkList(String externalId) {
        ProviderNetwork providerNetwork = new ProviderNetwork();
        providerNetwork.setExternalId(externalId);
        providerNetwork.setProviderId(PROVIDER_ID);
        Network network = new Network();
        network.setProvidedBy(providerNetwork);
        return Collections.singletonList(network);
    }

    private List<Cluster> getClusterList() {
        Cluster cluster = new Cluster();
        cluster.setStoragePoolId(DATACENTER_ID);
        return Collections.singletonList(cluster);
    }

    private ActionReturnValue getImportNetworkReturnValue() {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setActionReturnValue(NETWORK_ID);
        return returnValue;
    }

    private void verifyCalls() {
        verify(backend).runInternalAction(eq(ActionType.RemoveNetwork), any(), any());
        verify(backend).runInternalAction(eq(ActionType.InternalImportExternalNetwork), any(), any());
        verify(networkHelper).attachNetworkToClusters(eq(NETWORK_ID), any());
        verify(auditLogDirector).log(any(), eq(AuditLogType.PROVIDER_SYNCHRONIZATION_STARTED));
        verify(auditLogDirector).log(any(), eq(AuditLogType.PROVIDER_SYNCHRONIZATION_ENDED));
    }
}
