package org.ovirt.engine.core.bll.network.dc;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.openstack.ExternalNetworkProviderProxy;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportExternalNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class ImportExternalNetworkCommandTest extends BaseCommandTest {
    private final Guid PROVIDER_ID = new Guid("000000000000-0000-0000-0000-00000001");
    private final String EXTERNAL_ID = "000000000000-0000-0000-0000-00000002";
    private final Guid DATACENTER_ID = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid CLUSTER_ID = new Guid("000000000000-0000-0000-0000-00000004");
    private final Guid NETWORK_ID = new Guid("000000000000-0000-0000-0000-00000005");

    @Mock
    private BackendInternal backend;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private NetworkHelper networkHelper;

    @Mock
    private ExternalNetworkProviderProxy providerProxy;

    @InjectMocks
    private ImportExternalNetworkCommand<ImportExternalNetworkParameters> commandNoCluster =
            new ImportExternalNetworkCommand<>(new ImportExternalNetworkParameters(PROVIDER_ID, EXTERNAL_ID,
                    DATACENTER_ID, true, false), CommandContext.createContext("context"));

    @InjectMocks
    private ImportExternalNetworkCommand<ImportExternalNetworkParameters> commandCluster =
            new ImportExternalNetworkCommand<>(new ImportExternalNetworkParameters(PROVIDER_ID, EXTERNAL_ID,
                    DATACENTER_ID, true, true), CommandContext.createContext("context"));

    private Provider provider = new Provider();

    @Before
    public void setUp() {
        provider.setType(ProviderType.EXTERNAL_NETWORK);

        when(providerDao.get(PROVIDER_ID)).thenReturn(provider);
        when(providerProxyFactory.create(provider)).thenReturn(providerProxy);
        when(providerProxy.getAll()).thenReturn(getProviderNetworks());

        when(backend.runInternalAction(eq(ActionType.AddNetwork), any(), any()))
                .thenReturn(getAddNetworkReturnValue());

        when(networkHelper.createVnicProfile(any())).thenReturn(new VnicProfile());

        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);
        when(backend.runInternalAction(eq(ActionType.AddVnicProfile), any(), any())).thenReturn(returnValue);
        when(networkHelper.attachNetworkToClusters(eq(NETWORK_ID), any())).thenReturn(returnValue);

        QueryReturnValue queryReturnValue = new QueryReturnValue();
        queryReturnValue.setReturnValue(getClusters());
        queryReturnValue.setSucceeded(true);
        when(backend.runInternalQuery(eq(QueryType.GetClustersByStoragePoolId), any(), any()))
                .thenReturn(queryReturnValue);
    }

    @Test
    public void testImportNoClusterSuccessfully() {
        ValidateTestUtils.runAndAssertValidateSuccess(commandNoCluster);
        commandNoCluster.executeCommand();
        assertTrue(commandNoCluster.getReturnValue().getSucceeded());
        verifyCalls(false);
    }

    @Test
    public void testImportClusterSuccessfully() {
        ValidateTestUtils.runAndAssertValidateSuccess(commandCluster);
        commandCluster.executeCommand();
        assertTrue(commandCluster.getReturnValue().getSucceeded());
        verifyCalls(true);
    }

    @Test
    public void testInvalidExternalId() {
        when(providerProxy.getAll()).thenReturn(Collections.emptyList());
        ValidateTestUtils.runAndAssertValidateFailure(commandNoCluster,
                EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS);
    }

    private List<Network> getProviderNetworks() {
        ProviderNetwork providerNetwork = new ProviderNetwork();
        providerNetwork.setExternalId(EXTERNAL_ID);
        providerNetwork.setProviderId(PROVIDER_ID);
        Network network = new Network();
        network.setProvidedBy(providerNetwork);
        return Collections.singletonList(network);
    }

    private List<Cluster> getClusters() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        return Collections.singletonList(cluster);
    }

    private ActionReturnValue getAddNetworkReturnValue() {
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setActionReturnValue(NETWORK_ID);
        return returnValue;
    }

    private void verifyCalls(boolean attachToAllClusters) {
        verify(backend).runInternalAction(eq(ActionType.AddNetwork), any(), any());
        verify(networkHelper).createVnicProfile(any());
        verify(backend).runInternalAction(eq(ActionType.AddVnicProfile), any(), any());

        VerificationMode expectedNumberOfCalls = attachToAllClusters ? times(1) : never();
        verify(backend, expectedNumberOfCalls).runInternalQuery(eq(QueryType.GetClustersByStoragePoolId), any(), any());
        verify(networkHelper, expectedNumberOfCalls).attachNetworkToClusters(eq(NETWORK_ID), any());
    }
}
