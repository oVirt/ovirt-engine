package org.ovirt.engine.core.bll.network.dc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.network.dc.UpdateNetworkCommand.UpdateNetworkValidator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class UpdateNetworkValidatorTest {

    private static final String EXTERNAL_NETWORK_ID = "52d5c1c6-cb15-4832-b2a4-023770607200";

    private Network network;

    private UpdateNetworkValidator validator;

    @BeforeEach
    public void setup() {
        network = new Network();
        validator = new UpdateNetworkValidator(network);
    }

    private Network createExternalNetwork() {
        Network externalNetwork = new Network();
        ProviderNetwork providerNetwork = createProviderNetwork(Guid.newGuid());
        providerNetwork.setExternalId(EXTERNAL_NETWORK_ID);
        network.setProvidedBy(providerNetwork);
        externalNetwork.setProvidedBy(providerNetwork);

        return externalNetwork;
    }

    @Test
    public void externalNetworkNameChanged() {
        Network externalNetwork = createExternalNetwork();

        externalNetwork.setName("aaa");
        network.setName("bbb");

        assertThat(validator.externalNetworkDetailsUnchanged(externalNetwork), isValid());
    }

    @Test
    public void externalNetworkDescriptionChanged() {
        Network externalNetwork = createExternalNetwork();

        externalNetwork.setDescription("aaa");
        network.setDescription("bbb");

        assertThat(validator.externalNetworkDetailsUnchanged(externalNetwork), isValid());
    }

    private void assertThatExternalNetworkDetailsUnchangedFails(Network externalNetwork) {
        assertThat(validator.externalNetworkDetailsUnchanged(externalNetwork),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_DETAILS_CANNOT_BE_EDITED));
    }

    @Test
    public void externalNetworkStpChanged() {
        Network externalNetwork = createExternalNetwork();

        externalNetwork.setStp(true);
        network.setStp(false);

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void externalNetworkVlanIdChanged() {
        Network externalNetwork = createExternalNetwork();

        externalNetwork.setVlanId(0);
        network.setVlanId(1);

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void externalNetworkVmNetworkChanged() {
        Network externalNetwork = createExternalNetwork();

        externalNetwork.setVmNetwork(true);
        network.setVmNetwork(false);

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void externalNetworkProviderIdChanged() {
        Network externalNetwork = createExternalNetwork();
        externalNetwork.setProvidedBy(createProviderNetwork(Guid.newGuid()));

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void externalNetworkExternalIdChanged() {
        Network externalNetwork = createExternalNetwork();
        externalNetwork.setProvidedBy(createProviderNetwork(network.getProvidedBy().getProviderId()));

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void internalNetworkProvidedIdChanged() {
        Network externalNetwork = createExternalNetwork();
        network.setProvidedBy(createProviderNetwork(Guid.newGuid()));

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    @Test
    public void internalNetworkExternalIdChanged() {
        Network externalNetwork = createExternalNetwork();
        network.setProvidedBy(createProviderNetwork(externalNetwork.getProvidedBy().getProviderId()));

        assertThatExternalNetworkDetailsUnchangedFails(externalNetwork);
    }

    private ProviderNetwork createProviderNetwork(Guid providerId) {
        final ProviderNetwork result = new ProviderNetwork();
        result.setProviderId(providerId);
        return result;
    }
}
