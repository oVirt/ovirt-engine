package org.ovirt.engine.core.bll.provider;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.RemoveProviderCommand.RemoveProviderValidator;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveProviderValidatorTest {

    @Mock
    private Provider<?> provider;

    private List<Network> networks = new ArrayList<>();

    private List<Cluster> clusters = new ArrayList<>();

    private RemoveProviderValidator validator;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private ClusterDao clusterDao;

    /* --- Set up for tests --- */

    @BeforeEach
    public void setUp() {
        validator = spy(new RemoveProviderValidator(networkDao, clusterDao, provider));
        when(networkDao.getAllForProvider(any())).thenReturn(networks);
        when(clusterDao.getAllClustersByDefaultNetworkProviderId(any())).thenReturn(clusters);
    }

    @Test
    public void networksNotUsedWhenNoNetworks() {
        assertThat(validator.providerNetworksNotUsed(), isValid());
    }

    private Network mockNetwork() {
        Network net = mock(Network.class);
        when(net.getName()).thenReturn("net");
        networks.add(net);
        return net;
    }

    private void networksUsedTest(boolean vmsNotUsingNetwork,
            boolean templatesNotUsingNetwork,
            Matcher<ValidationResult> matcher) {

        NetworkValidator networkValidator = mock(NetworkValidator.class);
        for (Network network : networks) {
            when(validator.getValidator(network)).thenReturn(networkValidator);
        }

        when(networkValidator.networkNotUsedByVms()).thenReturn(createValidationResult(vmsNotUsingNetwork));
        when(networkValidator.networkNotUsedByTemplates()).thenReturn(createValidationResult(templatesNotUsingNetwork));

        assertThat(validator.providerNetworksNotUsed(), matcher);
    }

    private ValidationResult createValidationResult(boolean valid) {
        return valid ? ValidationResult.VALID : new ValidationResult(EngineMessage.Unassigned);
    }

    @Test
    public void networksNotUsedByVmsNorTemplates() {
        mockNetwork();
        networksUsedTest(true, true, isValid());
    }

    @Test
    public void networksUsedByAVm() {
        Network net = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE))
                .and(replacements(hasItem(containsString(net.getName())))));
    }

    @Test
    public void networksUsedByAVmMultipleNetworks() {
        Network net = mockNetwork();
        Network net2 = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES))
                .and(replacements(hasItem(containsString(net.getName()))))
                .and(replacements(hasItem(containsString(net2.getName())))));
    }

    @Test
    public void networksUsedByATemplate() {
        Network net = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE))
                .and(replacements(hasItem(containsString(net.getName())))));
    }

    @Test
    public void networksUsedByATemplateMultipleNetworks() {
        Network net = mockNetwork();
        Network net2 = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES))
                .and(replacements(hasItem(containsString(net.getName()))))
                        .and(replacements(hasItem(containsString(net2.getName())))));
    }

    @Test
    public void providerIsNoDefaultProvider() {
        assertThat(validator.providerIsNoDefaultProvider(), isValid());
    }

    @Test
    public void providerIsDefaultProviderOfCluster() {
        Cluster cluster0 = mockCluster("0");
        Cluster cluster1 = mockCluster("1");

        assertThat(validator.providerIsNoDefaultProvider(),
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_USED_IN_CLUSTER))
                .and(replacements(hasItem(containsString(cluster0.getName()))))
                .and(replacements(hasItem(containsString(cluster1.getName())))));
    }

    private Cluster mockCluster(String suffix) {
        Cluster cluster = mock(Cluster.class);
        when(cluster.getName()).thenReturn("cluster" + suffix);
        clusters.add(cluster);
        return cluster;
    }
}
