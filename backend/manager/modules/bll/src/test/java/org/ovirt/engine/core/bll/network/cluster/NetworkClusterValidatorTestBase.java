package org.ovirt.engine.core.bll.network.cluster;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
public abstract class NetworkClusterValidatorTestBase<T extends NetworkClusterValidatorBase> {

    private static final Guid TEST_DC_ID1 = Guid.newGuid();
    private static final Guid TEST_DC_ID2 = Guid.newGuid();
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

    protected Network network;
    protected Cluster cluster;
    protected NetworkCluster networkCluster;

    protected T validator;

    @BeforeEach
    public void setup() {
        network = createNetwork();
        networkCluster = createNetworkCluster();
        cluster = new Cluster();
        validator = spy(createValidator());
    }

    private Network createNetwork() {
        Network network = new Network();
        network.setName(NETWORK_NAME);
        return network;
    }

    private NetworkCluster createNetworkCluster() {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setClusterId(TEST_CLUSTER_ID);
        return networkCluster;
    }

    protected abstract T createValidator();

    @Test
    public void managementNetworkNotExternalValid() {
        testManagementNetworkNotExternal(true, false, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidNotManagement() {
        testManagementNetworkNotExternal(false, true, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidNotManagementNotExternal() {
        testManagementNetworkNotExternal(false, false, isValid());
    }

    @Test
    public void managementNetworkNotExternalValidInvalidExternal() {
        testManagementNetworkNotExternal(true, true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_CANNOT_BE_EXTERNAL))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    @Test
    public void testDefaultRouteNetworkCannotBeExternal() {
        networkCluster.setDefaultRoute(true);
        setNetworkExternal(network, true);

        EngineMessage message = EngineMessage.ACTION_TYPE_FAILED_DEFAULT_ROUTE_NETWORK_CANNOT_BE_EXTERNAL;
        assertThat(validator.defaultRouteNetworkCannotBeExternal(network),
                both(failsWith(message)).and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    @Test
    public void testDefaultRouteNetworkCannotBeExternalWhenNotDefaultRoute() {
        networkCluster.setDefaultRoute(false);
        setNetworkExternal(network, true);

        assertThat(validator.defaultRouteNetworkCannotBeExternal(network), isValid());
    }

    @Test
    public void testDefaultRouteNetworkCannotBeExternalWhenNotExternalNetwork() {
        networkCluster.setDefaultRoute(true);
        setNetworkExternal(network, false);

        assertThat(validator.defaultRouteNetworkCannotBeExternal(network), isValid());
    }

    private void testManagementNetworkNotExternal(boolean management,
                                                  boolean external,
                                                  Matcher<ValidationResult> expected) {
        networkCluster.setManagement(management);
        setNetworkExternal(network, external);

        assertThat(validator.managementNetworkNotExternal(network), expected);
    }

    private void setNetworkExternal(Network network, boolean external) {
        network.setProvidedBy(external ? new ProviderNetwork() : null);
    }

    private void testManagementNetworkRequired(boolean management,
                                               boolean required,
                                               Matcher<ValidationResult> expected) {
        networkCluster.setManagement(management);
        networkCluster.setRequired(required);

        assertThat(validator.managementNetworkRequired(network), expected);
    }

    @Test
    public void managementNetworkRequiredValidNotManagementNotRequired() {
        testManagementNetworkRequired(false, false, isValid());
    }

    @Test
    public void managementNetworkRequiredValidNotManagementRequired() {
        testManagementNetworkRequired(false, true, isValid());
    }

    @Test
    public void managementNetworkRequiredValidManagementRequired() {
        testManagementNetworkRequired(true, true, isValid());
    }

    @Test
    public void managementNetworkRequiredInvalid() {
        testManagementNetworkRequired(true, false,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_MANAGEMENT_NETWORK_REQUIRED))
                        .and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }

    @Test
    public void networkBelongsToClusterDataCenterValid() {
        cluster.setStoragePoolId(TEST_DC_ID1);
        network.setDataCenterId(TEST_DC_ID1);

        assertThat(validator.networkBelongsToClusterDataCenter(cluster, network), isValid());
    }

    @Test
    public void networkBelongsToClusterDataCenterNotValid() {
        cluster.setStoragePoolId(TEST_DC_ID1);
        network.setDataCenterId(TEST_DC_ID2);

        assertThat(validator.networkBelongsToClusterDataCenter(cluster, network),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_FROM_DIFFERENT_DC)).
                        and(replacements(hasItem(NETWORK_NAME_REPLACEMENT))));
    }
}
