package org.ovirt.engine.core.bll.network.dc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.lock.EngineLock;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AutodefineExternalNetworkCommandTest extends BaseCommandTest {

    private static final Guid PHYSICAL_NETWORK_ID = Guid.newGuid();
    private static final Guid EXTERNAL_NETWORK_ID = Guid.newGuid();
    private static final Guid CLUSTER_DEFAULT_PROVIDER_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();

    private static final String SHORT_NAME = "net1";
    private static final String LONG_NAME =
            "mKbriFwHtfiTntyWlKyYuCvAmusHGnSGJNqjOictPsNSFZXMkBIOHrhwueuOrDqOMcZalDAxbeHMecEUmWxYRwoQSFiqfWyGPMKAycTwtbhffyigUVOeTyxvuhNySqYhrGehXsUIWXdRvWjmrxXiINRRxEBRkEYxfnrvURVslPjwcuBONKyrBbbbxspgZfAdqJImfeTeLrVPDsUzmJwjRdXYYFoEWzFxecNMNzmEvozheFhvywFmHnAXVNxXZqyw";

    private static final boolean HAS_SHORT_NAME = true;
    private static final boolean IS_VM_NETWORK = true;
    private static final boolean IS_CLUSTER_SWITCH_OVS = true;
    private static final boolean HAS_CLUSTER_VALID_PROVIDER = true;

    @Mock
    private BackendInternal backend;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private NetworkClusterDao networkClusterDao;

    @Mock
    private NetworkHelper networkHelper;

    @Mock
    AuditLogDirector auditLogDirector;

    @Mock
    EngineLock engineLock;

    @Spy
    @InjectMocks
    private AutodefineExternalNetworkCommand<IdParameters> command = new AutodefineExternalNetworkCommand<>(
            new IdParameters(PHYSICAL_NETWORK_ID), CommandContext.createContext("context"));

    private Network physicalNetwork;
    private Cluster cluster;

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        physicalNetwork = new Network();
        setUpPhysicalNetwork(HAS_SHORT_NAME, IS_VM_NETWORK);
        setUpCluster(IS_CLUSTER_SWITCH_OVS, HAS_CLUSTER_VALID_PROVIDER);

        ActionReturnValue actionReturnValue = new ActionReturnValue();
        actionReturnValue.setSucceeded(true);
        actionReturnValue.setActionReturnValue(EXTERNAL_NETWORK_ID);

        when(networkDao.get(PHYSICAL_NETWORK_ID)).thenReturn(physicalNetwork);
        when(networkClusterDao.getAllForNetwork(PHYSICAL_NETWORK_ID)).thenReturn(createNetworkClusters());
        when(clusterDao.get(CLUSTER_ID)).thenReturn(cluster);
        when(backend.runInternalAction(eq(ActionType.AddNetworkOnProvider),
                any(),
                any())).thenReturn(actionReturnValue);
        doReturn(engineLock).when(command).acquireLockForProvider(eq(CLUSTER_DEFAULT_PROVIDER_ID));
        doNothing().when(engineLock).close();
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.DefaultMTU, 1500));
    }

    @Test
    public void testCommandSuccessLongName() {
        setUpPhysicalNetwork(!HAS_SHORT_NAME, IS_VM_NETWORK);
        command.executeCommand();
        verify(backend).runInternalAction(eq(ActionType.AddNetworkOnProvider), any(), any());
        verify(networkHelper).createNetworkClusters(eq(Collections.singletonList(CLUSTER_ID)));
    }

    @Test
    public void testCommandNonVmNetwork() {
        setUpPhysicalNetwork(HAS_SHORT_NAME, !IS_VM_NETWORK);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK);
    }

    @Test
    public void testCommandExternalNetwork() {
        setUpPhysicalNetwork(HAS_SHORT_NAME, IS_VM_NETWORK);
        physicalNetwork.setProvidedBy(new ProviderNetwork());
        ValidateTestUtils.runAndAssertValidateFailure
                (command, EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK);
    }

    @Test
    public void testCommandInvalidNetworkId() {
        when(networkDao.get(PHYSICAL_NETWORK_ID)).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS);
    }

    @Test
    public void testCommandSuccess() {
        setUpPhysicalNetwork(HAS_SHORT_NAME, IS_VM_NETWORK);
        command.executeCommand();
        verify(backend).runInternalAction(eq(ActionType.AddNetworkOnProvider), any(), any());
        verify(networkHelper).createNetworkClusters(eq(Collections.singletonList(CLUSTER_ID)));
    }

    @Test
    public void testCommandWrongSwitchType() {
        setUpCluster(!IS_CLUSTER_SWITCH_OVS, HAS_CLUSTER_VALID_PROVIDER);
        command.executeCommand();
        verify(backend, never()).runInternalAction(eq(ActionType.AddNetworkOnProvider), any(), any());
    }

    @Test
    public void testCommandInvalidProviderId() {
        setUpCluster(IS_CLUSTER_SWITCH_OVS, !HAS_CLUSTER_VALID_PROVIDER);
        command.executeCommand();
        verify(backend, never()).runInternalAction(eq(ActionType.AddNetworkOnProvider), any(), any());
    }

    private List<NetworkCluster> createNetworkClusters() {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setNetworkId(PHYSICAL_NETWORK_ID);
        networkCluster.setClusterId(CLUSTER_ID);

        List<NetworkCluster> networkClusters = new ArrayList<>();
        networkClusters.add(networkCluster);

        return networkClusters;
    }

    private void setUpCluster(boolean isOvs, boolean hasValidProvider) {
        cluster.setId(CLUSTER_ID);
        cluster.setDefaultNetworkProviderId(hasValidProvider ? CLUSTER_DEFAULT_PROVIDER_ID : null);
        cluster.setRequiredSwitchTypeForCluster(isOvs ? SwitchType.OVS : SwitchType.LEGACY);
    }

    private void setUpPhysicalNetwork(boolean hasShortName, boolean isVmNetwork) {
        physicalNetwork.setId(PHYSICAL_NETWORK_ID);
        physicalNetwork.setName(hasShortName ? SHORT_NAME : LONG_NAME);
        physicalNetwork.setVmNetwork(isVmNetwork);
    }
}
