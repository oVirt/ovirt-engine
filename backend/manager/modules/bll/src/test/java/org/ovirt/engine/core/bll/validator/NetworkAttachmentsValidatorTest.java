package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;

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

    private void checkVmNetworkIsSoleAssignedInterface(Matcher<ValidationResult> matcher, Network... networks) {

        List<NetworkAttachment> attachmentsToConfigure = new ArrayList<>(networks.length);
        for (Network network : networks) {
            attachmentsToConfigure.add(createNetworkAttachment(nic.getName(), network));
        }

        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(attachmentsToConfigure, networkMap);
        assertThat(validator.validateNetworkExclusiveOnNics(), matcher);
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

    @Test
    public void testValidateNetworkExclusiveOnNicsAllAttachmentsMustHaveNicNameSet() throws Exception {
        NetworkAttachment networkAttachment = createNetworkAttachment(vmNetwork1);
        List<NetworkAttachment> attachmentsToConfigure = Collections.singletonList(networkAttachment);

        expectedException.expect(IllegalArgumentException.class);
        new NetworkAttachmentsValidator(attachmentsToConfigure, networkMap).validateNetworkExclusiveOnNics();
    }


    @Test
    public void testValidateNetworkExclusiveOnNicsVmNetworkIsSoleAttachedInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(isValid(), vmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsTwoVmNetworksAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
            failsWithNetworkInterfacesNotExclusivelyUsedByNetwork(),
            vmNetwork1, vmNetwork2);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsVmAndNonVmAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
            failsWithNetworkInterfacesNotExclusivelyUsedByNetwork(),
            vmNetwork1, nonVmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicsVmAndVlanAttachedToInterface() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
            failsWithNetworkInterfacesNotExclusivelyUsedByNetwork(),
            vmNetwork1, vlanNetwork);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicAtMostOneNonVmNetwork() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(isValid(), vlanNetwork, nonVmNetwork1);
    }

    @Test
    public void testValidateNetworkExclusiveOnNicAtMostOneNonVmNetworkViolated() throws Exception {
        checkVmNetworkIsSoleAssignedInterface(
            failsWithNetworkInterfacesNotExclusivelyUsedByNetwork(),
            nonVmNetwork1, nonVmNetwork2);
    }

    private Matcher<ValidationResult> failsWithNetworkInterfacesNotExclusivelyUsedByNetwork() {
        return failsWith(EngineMessage.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK,
            ReplacementUtils.replaceWith(NetworkAttachmentsValidator.VAR_NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK_LIST,
                Collections.singletonList(nic.getName())));
    }

    @Test
    public void testDetermineNetworkType() throws Exception {
        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(null, null);
        assertThat(validator.determineNetworkType(vlanNetwork), is(NetworkAttachmentsValidator.NetworkType.VLAN));
        assertThat(validator.determineNetworkType(vmNetwork1), is(NetworkAttachmentsValidator.NetworkType.VM));
        assertThat(validator.determineNetworkType(nonVmNetwork1), is(NetworkAttachmentsValidator.NetworkType.NON_VM));
    }

    @Test
    public void testVerifyUserAttachmentsDoesNotReferenceSameNetworkDuplicatelyWhenNoDuplicates() {
        Network networkA = createNetworkWithIdAndName("networkA");
        NetworkAttachment networkAttachmentA = createNetworkAttachment(networkA);

        Network networkB = createNetworkWithIdAndName("networkB");
        NetworkAttachment networkAttachmentB = createNetworkAttachment(networkB);

        List<NetworkAttachment> attachments = Arrays.asList(networkAttachmentA, networkAttachmentB);
        BusinessEntityMap<Network> networksMap = new BusinessEntityMap<>(Arrays.asList(networkA, networkB));
        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(attachments, networksMap);
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
        NetworkAttachmentsValidator validator = new NetworkAttachmentsValidator(attachments, networksMap);

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
