package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class NetworkClusterValidatorTestBase<T extends NetworkClusterValidatorBase> {

    private static final Guid TEST_DC_ID1 = Guid.newGuid();
    private static final Guid TEST_DC_ID2 = Guid.newGuid();
    private static final Guid TEST_CURRENT_MANAGMENT_NETWORK_ID = Guid.newGuid();
    protected static final Guid TEST_CLUSTER_ID = Guid.newGuid();
    private static final String NETWORK_NAME = RandomUtils.instance().nextString(
            RandomUtils.instance().nextInt(1, 10));

    private static final String NETWORK_NAME_REPLACEMENT = String.format(
            NetworkClusterValidatorBase.NETWORK_NAME_REPLACEMENT, NETWORK_NAME);

    @Mock
    protected VdsDao vdsDao;
    @Mock
    protected InterfaceDao interfaceDao;
    @Mock
    protected NetworkDao networkDao;
    @Mock
    protected NetworkCluster networkCluster;
    @Mock
    protected NetworkCluster oldNetworkCluster;
    @Mock
    protected Network network;
    @Mock
    protected Network currentManagementNetwork;
    @Mock
    protected Cluster cluster;

    protected T validator;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Before
    public void setup() {
        when(network.getName()).thenReturn(NETWORK_NAME);
        when(currentManagementNetwork.getId()).thenReturn(TEST_CURRENT_MANAGMENT_NETWORK_ID);

        validator = spy(createValidator());

        doReturn(vdsDao).when(validator).getVdsDao();
    }

    protected abstract T createValidator();

    @Test
    public void managementNetworkNotExternalValid() {
        testmanagementNetworkNotExternal(true, false, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidNotManagement() {
        testmanagementNetworkNotExternal(false, true, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidNotManagementNotExternal() {
        testmanagementNetworkNotExternal(false, false, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidInvalidExternal() {
        testmanagementNetworkNotExternal(true, true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_EXTERNAL))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    private void testmanagementNetworkNotExternal(boolean management,
                                                  boolean external,
                                                  Matcher<ValidationResult> expected) {
        when(networkCluster.isManagement()).thenReturn(management);
        when(network.isExternal()).thenReturn(external);

        assertThat(validator.managementNetworkNotExternal(network), expected);
    }

    private void testmanagementNetworkRequired(boolean management,
                                               boolean required,
                                               Matcher<ValidationResult> expected) {
        when(networkCluster.isManagement()).thenReturn(management);
        when(networkCluster.isRequired()).thenReturn(required);

        assertThat(validator.managementNetworkRequired(network), expected);
    }

    @Test
    public void managementNetworkRequiredValidNotManagementNotRequired() {
        testmanagementNetworkRequired(false, false, isValid());
    }

    @Test
    public void managementNetworkRequiredValidNotManagementRequired() {
        testmanagementNetworkRequired(false, true, isValid());
    }

    @Test
    public void managementNetworkRequiredValidManagementRequired() {
        testmanagementNetworkRequired(true, true, isValid());
    }

    @Test
    public void managementNetworkRequiredInvalid() {
        testmanagementNetworkRequired(true, false,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    @Test
    public void networkBelongsToClusterDataCenterValid() throws Exception {
        when(cluster.getStoragePoolId()).thenReturn(TEST_DC_ID1);
        when(network.getDataCenterId()).thenReturn(TEST_DC_ID1);

        assertThat(validator.networkBelongsToClusterDataCenter(cluster, network), isValid());
    }

    @Test
    public void networkBelongsToClusterDataCenterNotValid() throws Exception {
        when(cluster.getStoragePoolId()).thenReturn(TEST_DC_ID1);
        when(network.getDataCenterId()).thenReturn(TEST_DC_ID2);

        assertThat(validator.networkBelongsToClusterDataCenter(cluster, network),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_FROM_DIFFERENT_DC)).
                        and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }
}
