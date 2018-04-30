package org.ovirt.engine.core.bll.network.dc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.InternalImportExternalNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalImportExternalNetworkCommandTest extends BaseCommandTest {
    private final Guid DATACENTER_ID = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid CLUSTER_ID = new Guid("000000000000-0000-0000-0000-00000004");
    private final Guid NETWORK_ID = new Guid("000000000000-0000-0000-0000-00000005");
    private final String PROVIDER_NAME = "provider";

    @Mock
    private BackendInternal backend;

    @Mock
    private NetworkHelper networkHelper;

    @InjectMocks
    private InternalImportExternalNetworkCommand<InternalImportExternalNetworkParameters> commandNoCluster =
            new InternalImportExternalNetworkCommand<>(new InternalImportExternalNetworkParameters(PROVIDER_NAME,
                    new Network(), DATACENTER_ID, true, false),
                    CommandContext.createContext("context"));

    @InjectMocks
    private InternalImportExternalNetworkCommand<InternalImportExternalNetworkParameters> commandCluster =
            new InternalImportExternalNetworkCommand<>(new InternalImportExternalNetworkParameters(PROVIDER_NAME,
                    new Network(), DATACENTER_ID, true, true),
                    CommandContext.createContext("context"));

    @BeforeEach
    public void setUp() {
        prepareNetwork(commandNoCluster.getParameters().getExternalNetwork());
        prepareNetwork(commandCluster.getParameters().getExternalNetwork());

        when(backend.runInternalAction(eq(ActionType.AddNetwork), any(), any()))
                .thenReturn(getAddNetworkReturnValue());

        when(networkHelper.createVnicProfile(any())).thenReturn(new VnicProfile());

        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);
        when(networkHelper.attachNetworkToClusters(eq(NETWORK_ID), any())).thenReturn(returnValue);

        QueryReturnValue queryReturnValue = new QueryReturnValue();
        queryReturnValue.setReturnValue(getClusters());
        queryReturnValue.setSucceeded(true);
        when(backend.runInternalQuery(eq(QueryType.GetClustersByStoragePoolId), any(), any()))
                .thenReturn(queryReturnValue);
    }

    @Test
    public void testImportNoClusterSuccessfully() {
        runCommand(commandNoCluster);
        verifyCalls(false);
    }

    @Test
    public void testImportClusterSuccessfully() {
        runCommand(commandCluster);
        verifyCalls(true);
    }

    private void prepareNetwork(Network network) {
        network.setId(NETWORK_ID);
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

    private void runCommand(InternalImportExternalNetworkCommand<InternalImportExternalNetworkParameters> command) {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
        assertEquals(NETWORK_ID, command.getReturnValue().getActionReturnValue());
    }

    private void verifyCalls(boolean attachToAllClusters) {
        verify(backend).runInternalAction(eq(ActionType.AddNetwork), any(), any());

        VerificationMode expectedNumberOfCalls = attachToAllClusters ? times(1) : never();
        verify(backend, expectedNumberOfCalls).runInternalQuery(eq(QueryType.GetClustersByStoragePoolId), any(), any());
        verify(networkHelper, expectedNumberOfCalls).createNetworkClusters(eq(Collections.singletonList(CLUSTER_ID)));
    }
}
