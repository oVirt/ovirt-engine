package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.common.errors.EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class NetworkAttachmentsValidatorTest {

    private Network vlanNetwork;
    private Network vmNetwork1;
    private Network vmNetwork2;
    private Network nonVmNetwork1;
    private Network nonVmNetwork2;

    private VdsNetworkInterface nic;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private BusinessEntityMap<Network> networkMap;

    @Mock
    private NetworkExclusivenessValidator networkExclusivenessValidator;

    @Captor
    private ArgumentCaptor<List<NetworkType>> networkTypeCaptor;

    @Before
    public void setUp() throws Exception {
        nic = new VdsNetworkInterface();
        nic.setId(Guid.newGuid());
        nic.setName("nicName");

        vlanNetwork = createNetworkWithIdAndName("vlanNetwork");
        vlanNetwork.setVlanId(1);

        vmNetwork1 = createNetworkWithIdAndName("Network1");
        vmNetwork1.setVmNetwork(true);

        vmNetwork2 = createNetworkWithIdAndName("Network2");
        vmNetwork2.setVmNetwork(true);

        nonVmNetwork1 = createNetworkWithIdAndName("nonVmNetwork1");
        nonVmNetwork1.setVmNetwork(false);

        nonVmNetwork2 = createNetworkWithIdAndName("nonVmNetwork2");
        nonVmNetwork2.setVmNetwork(false);

        networkMap = new BusinessEntityMap<>(
                Arrays.asList(vlanNetwork, vmNetwork1, vmNetwork2, nonVmNetwork1, nonVmNetwork2));

        when(networkExclusivenessValidator.getViolationMessage())
                .thenReturn(NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK);
    }

    private Network createNetworkWithId() {
        Network network = new Network();
        network.setId(Guid.newGuid());
        return network;
    }

    private Network createNetworkWithIdAndName(String networkName) {
        Network network = createNetworkWithId();
        network.setName(networkName);
        return network;
    }

    private void checkVmNetworkIsSoleAssignedInterface(boolean valid,
            List<NetworkType> expectedNetworksTypes,
            Network... networks) {

        List<NetworkAttachment> attachmentsToConfigure = new ArrayList<>(networks.length);
        for (Network network : networks) {
            attachmentsToConfigure.add(createNetworkAttachment(nic.getName(), network));
        }

        when(networkExclusivenessValidator.isNetworkExclusive(expectedNetworksTypes)).thenReturn(valid);

        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(
                attachmentsToConfigure,
                networkMap,
                networkExclusivenessValidator);

        final ValidationResult actual = validator.validateNetworkExclusiveOnNics();

        verify(networkExclusivenessValidator).isNetworkExclusive(networkTypeCaptor.capture());

        assertThat(networkTypeCaptor.getValue(), is(expectedNetworksTypes));
        final Matcher<? super ValidationResult> matcher =
                valid ? isValid() : failsWith(NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_UNTAGGED_NETWORK);
        assertThat(actual, matcher);
    }

    private NetworkAttachment createNetworkAttachment(String nicName, Network network) {
        NetworkAttachment result = createNetworkAttachment(network);
        result.setNicName(nicName);
        return result;
    }

    private NetworkAttachment createNetworkAttachment(Network network) {
        NetworkAttachment result = new NetworkAttachment();
        result.setNetworkId(network.getId());
        return result;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNetworkExclusiveOnNicsAllAttachmentsMustHaveNicNameSet() throws Exception {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(vmNetwork1.getId());
        List<NetworkAttachment> attachmentsToConfigure = Collections.singletonList(networkAttachment);

        new NetworkAttachmentsValidator(attachmentsToConfigure,
                networkMap,
                networkExclusivenessValidator).validateNetworkExclusiveOnNics();
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsVmNetworkMustBeSoleAttachedInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(false, Arrays.asList(NetworkType.VM), vmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsTwoVmNetworksAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
                false,
                Arrays.asList(NetworkType.VM, NetworkType.VM),
                vmNetwork1, vmNetwork2);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsVmAndNonVmAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
                false,
                Arrays.asList(NetworkType.VM, NetworkType.NON_VM),
                vmNetwork1, nonVmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsVmAndVlanAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
                true,
                Arrays.asList(NetworkType.VM, NetworkType.VLAN),
                vmNetwork1, vlanNetwork);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicAtMostOneNonVmNetwork() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
                true,
                Arrays.asList(NetworkType.VLAN, NetworkType.NON_VM),
                vlanNetwork, nonVmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicAtMostOneNonVmNetworkViolated() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
                false,
                Arrays.asList(NetworkType.NON_VM, NetworkType.NON_VM),
                nonVmNetwork1, nonVmNetwork2);
    }

    @Test
    public void testDetermineNetworkType() throws Exception {
        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(null, null,
                networkExclusivenessValidator);
        assertThat(validator.determineNetworkType(vlanNetwork), is(NetworkType.VLAN));
        assertThat(validator.determineNetworkType(vmNetwork1), is(NetworkType.VM));
        assertThat(validator.determineNetworkType(nonVmNetwork1), is(NetworkType.NON_VM));
    }

    @Test
    public void testVerifyUserAttachmentsDoesNotReferenceSameNetworkDuplicatelyWhenNoDuplicates() {
        Network networkA = createNetworkWithIdAndName("networkA");
        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);

        Network networkB = createNetworkWithIdAndName("networkB");
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        List<NetworkAttachment> attachments = Arrays.asList(networkAttachmentA, networkAttachmentB);
        BusinessEntityMap<Network> networksMap = new BusinessEntityMap<>(Arrays.asList(networkA, networkB));
        NetworkAttachmentsValidator validator =
                new NetworkAttachmentsValidator(attachments, networksMap, networkExclusivenessValidator);
        assertThat(validator.verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately(), isValid());
    }

    @Test
    public void testVerifyUserAttachmentsDoesNotReferenceSameNetworkDuplicatelyWhenDuplicatesPresent() {
        Network duplicatelyReferencedNetwork = createNetworkWithId();
        duplicatelyReferencedNetwork.setName("networkName");

        NetworkAttachment networkAttachmentA = createNetworkAttachment(duplicatelyReferencedNetwork);
        NetworkAttachment networkAttachmentB = createNetworkAttachment(duplicatelyReferencedNetwork);

        List<NetworkAttachment> attachments = Arrays.asList(networkAttachmentA, networkAttachmentB);
        BusinessEntityMap<Network> networksMap = new BusinessEntityMap<>(Collections.singletonList(
            duplicatelyReferencedNetwork));
        NetworkAttachmentsValidator validator =
                new NetworkAttachmentsValidator(attachments, networksMap, networkExclusivenessValidator);

        List<String> replacements = new ArrayList<>();
        replacements.addAll(ReplacementUtils.replaceWith(
            "ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY_LIST",
            Arrays.asList(networkAttachmentA.getId(), networkAttachmentB.getId())));
        replacements.add(ReplacementUtils.createSetVariableString(
            "ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY_ENTITY",
            duplicatelyReferencedNetwork.getName()));

        assertThat(validator.verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately(),
            failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY,
                replacements));
    }
}
