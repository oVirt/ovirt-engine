package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class HostInterfaceValidatorTest {

    @Test
    public void testInterfaceExistsWhenInterfaceIsNull() throws Exception {
        String failingSlaveName = "slaveName";
        EngineMessage engineMessage = EngineMessage.HOST_NETWORK_INTERFACE_HAVING_NAME_DOES_NOT_EXIST;
        String nicNameReplacement = ReplacementUtils.getVariableAssignmentString(engineMessage, failingSlaveName);
        assertThat(new HostInterfaceValidator(null).interfaceExists(failingSlaveName),
                failsWith(engineMessage, nicNameReplacement));
    }

    @Test
    public void testInterfaceExistsWhenInterfaceIsNotNull() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceExists("nicId"),
            isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenLabelsIsNull() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(null);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsNotLabeled() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(new HashSet<>());
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsLabeledByDifferentLabel() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(Collections.singleton("labelB"));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith("labelA"), isValid());
    }

    @Test
    public void testInterfaceAlreadyLabeledWithWhenInterfaceIsLabeledBySameLabel() throws Exception {
        String labelA = "labelA";
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setLabels(Collections.singleton(labelA));
        Matcher<ValidationResult> matcher = failsWith(EngineMessage.INTERFACE_ALREADY_LABELED,
            ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_LABELED_NIC,
                vdsNetworkInterface.getName()),
            ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NIC_LABEL, labelA));

        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceAlreadyLabeledWith(labelA),
            matcher);

    }

    @Test
    public void testInterfaceInHostWhenInSameHost() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        Guid vdsId = Guid.newGuid();
        vdsNetworkInterface.setVdsId(vdsId);
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceInHost(vdsId), isValid());
    }

    @Test
    public void testInterfaceInHostWhenInDifferentHost() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setVdsId(Guid.newGuid());
        Guid hostId = Guid.newGuid();
        final EngineMessage engineMessage = EngineMessage.NIC_NOT_EXISTS_ON_HOST;
        Matcher<ValidationResult> matcher = failsWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, hostId.toString()));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceInHost(hostId), matcher);

    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName() {
        return createVdsNetworkInterfaceWithName("name");
    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName(String name) {
        return createVdsNetworkInterfaceWithName(name, false);
    }

    private VdsNetworkInterface createVdsNetworkInterfaceWithName(String name, boolean bonded) {
        VdsNetworkInterface vdsNetworkInterface = new VdsNetworkInterface();
        vdsNetworkInterface.setName(name);
        vdsNetworkInterface.setBonded(bonded);
        return vdsNetworkInterface;
    }

    @Test
    public void testValidBond() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBonded(false);
        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        assertThat(validator.validBond(Collections.<VdsNetworkInterface> emptyList()), isValid());
    }

    @Test
    public void testValidBondWhenBondHasNoSlave() throws Exception {
        assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(0);
    }

    @Test
    public void testValidBondWhenBondHasOneSlave() throws Exception {
        assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(1);
    }

    @Test
    public void testValidBondWhenSufficientNumberOfSlaves() throws Exception {
        assertCorrectSlaveCountInValidBonds(2,
            "bonded interface with two or more slaves should be valid bond",
            isValid(),
            "bondName");
    }

    private void assertCorrectSlaveCountInValidBondsWhenInsufficientBonds(int numberOfSlaves) {
        String bondName = "bondName";
        Matcher<ValidationResult> matcher = failsWith(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
            ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT, bondName));

        assertCorrectSlaveCountInValidBonds(
            numberOfSlaves,
            String.format("bonded interface with only %1$d slaves is not valid bond", numberOfSlaves),
            matcher,
            bondName);

    }

    private void assertCorrectSlaveCountInValidBonds(int numberOfSlaves,
        String reason,
        Matcher<ValidationResult> matcher,
        String bondName) {

        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName(bondName, true);
        List<VdsNetworkInterface> slaves = createGivenCountOfSlavesForBond(bondName, numberOfSlaves);

        assertThat(reason, new HostInterfaceValidator(vdsNetworkInterface).validBond(slaves), matcher);
    }

    private List<VdsNetworkInterface> createGivenCountOfSlavesForBond(String bondName, int numberOfSlaves) {
        List<VdsNetworkInterface> slaves = new ArrayList<>();
        for (int i = 0; i < numberOfSlaves; i++) {
            VdsNetworkInterface slave = createVdsNetworkInterfaceWithName();
            slave.setBondName(bondName);
            slaves.add(slave);
        }
        return slaves;
    }

    @Test
    public void testAnotherInterfaceAlreadyLabeledWithThisLabel() throws Exception {
        //identity of nics is compared based on names.
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName("name");
        VdsNetworkInterface preexistingVdsNetworkInterface = createVdsNetworkInterfaceWithName("differentName");

        //set same label
        String label = "label";
        vdsNetworkInterface.setLabels(Collections.singleton(label));
        preexistingVdsNetworkInterface.setLabels(Collections.singleton(label));

        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        List<VdsNetworkInterface> preexistingInterfaces = Collections.singletonList(preexistingVdsNetworkInterface);
        assertThat("different nics cannot have same label set.",
                validator.anotherInterfaceAlreadyLabeledWithThisLabel(label, preexistingInterfaces),
            failsWith(EngineMessage.OTHER_INTERFACE_ALREADY_LABELED,
                ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_LABELED_NIC,
                    preexistingVdsNetworkInterface.getName()),
                ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NIC_LABEL, label)));

    }

    @Test
    public void testAnotherInterfaceAlreadyLabeledWithThisLabelDoNotCompareWithSelf() throws Exception {
        //identity of nics is compared based on names.
        String sameName = "sameName";
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName(sameName);
        VdsNetworkInterface preexistingVdsNetworkInterface = createVdsNetworkInterfaceWithName(sameName);

        //set same label
        String label = "label";
        vdsNetworkInterface.setLabels(Collections.singleton(label));
        preexistingVdsNetworkInterface.setLabels(Collections.singleton(label));

        HostInterfaceValidator validator = new HostInterfaceValidator(vdsNetworkInterface);
        List<VdsNetworkInterface> preexistingInterfaces = Collections.singletonList(preexistingVdsNetworkInterface);
        assertThat(validator.anotherInterfaceAlreadyLabeledWithThisLabel(label, preexistingInterfaces), isValid());
    }

    @Test
    public void testLabeledNetworkAttachedToThisInterfaceWhenNetworkIsAssignedToWrongInterface() throws Exception {
        String networkName = "networkA";
        Network network = new Network();
        network.setName(networkName);

        VdsNetworkInterface nicToWhichLabeledNetworkShouldBeAttached = createVdsNetworkInterfaceWithName("nicA");
        VdsNetworkInterface nicImproperlyAssignedToNetwork = createVdsNetworkInterfaceWithName("nicB");
        nicImproperlyAssignedToNetwork.setNetworkName(networkName);

        List<VdsNetworkInterface> hostInterfaces = Arrays.asList(
                nicToWhichLabeledNetworkShouldBeAttached,
                nicImproperlyAssignedToNetwork);


        HostInterfaceValidator validator = new HostInterfaceValidator(nicToWhichLabeledNetworkShouldBeAttached);
        assertThat(validator.networksAreAttachedToThisInterface(hostInterfaces, Collections.singletonList(network)),
            failsWith(EngineMessage.LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE,
                ReplacementUtils.replaceWith(HostInterfaceValidator.VAR_ASSIGNED_NETWORKS,
                    Collections.singletonList(networkName))));

    }

    @Test
    public void testLabeledNetworkAttachedToThisInterface() throws Exception {
        String networkName = "networkA";
        Network network = new Network();
        network.setName(networkName);

        VdsNetworkInterface nicToWhichLabeledNetworkShouldBeAttached = createVdsNetworkInterfaceWithName("nicA");
        nicToWhichLabeledNetworkShouldBeAttached.setNetworkName(networkName);

        List<VdsNetworkInterface> hostInterfaces = Arrays.asList(
                nicToWhichLabeledNetworkShouldBeAttached,
                createVdsNetworkInterfaceWithName("nicB"));


        HostInterfaceValidator validator = new HostInterfaceValidator(nicToWhichLabeledNetworkShouldBeAttached);
        List<Network> clusterNetworksWithLabel = Collections.singletonList(network);
        assertThat(validator.networksAreAttachedToThisInterface(hostInterfaces, clusterNetworksWithLabel), isValid());
    }

    @Test
    public void testInterfaceByNameExists() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceHasNameSet(), isValid());
    }

    @Test
    public void testInterfaceByNameExistsWhenInterfacesNameIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName(null)).interfaceHasNameSet(),
            failsWith(EngineMessage.HOST_NETWORK_INTERFACE_DOES_NOT_HAVE_NAME_SET));

    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsNull() throws Exception {
        assertThat(new HostInterfaceValidator(null).interfaceIsBondOrNull(), isValid());
    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsBonded() throws Exception {
        VdsNetworkInterface iface = createVdsNetworkInterfaceWithName();
        iface.setBonded(true);
        assertThat(new HostInterfaceValidator(iface).interfaceIsBondOrNull(), isValid());
    }

    @Test
    public void testInterfaceIsBondWhenInterfaceIsNotBonded() throws Exception {
        VdsNetworkInterface iface = createVdsNetworkInterfaceWithName();
        iface.setBonded(false);
        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        Matcher<ValidationResult> matcher = failsWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, iface.getName()));
        assertThat(new HostInterfaceValidator(iface).interfaceIsBondOrNull(), matcher);

    }

    @Test
    public void testInterfaceIsValidSlave() throws Exception {
        assertThat(new HostInterfaceValidator(createVdsNetworkInterfaceWithName()).interfaceIsValidSlave(), isValid());
    }

    @Test
    public void testInterfaceIsValidSlaveWhenInterfaceIsBond() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setBonded(true);
        Matcher<ValidationResult> matcher = failsWith(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
            ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NIC_NAME,
                vdsNetworkInterface.getName()));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceIsValidSlave(), matcher);

    }

    @Test
    public void testInterfaceIsValidSlaveWhenInterfaceIsVlan() throws Exception {
        VdsNetworkInterface vdsNetworkInterface = createVdsNetworkInterfaceWithName();
        vdsNetworkInterface.setVlanId(1);
        Matcher<ValidationResult> matcher = failsWith(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
            ReplacementUtils.createSetVariableString(HostInterfaceValidator.VAR_NIC_NAME,
                vdsNetworkInterface.getName()));
        assertThat(new HostInterfaceValidator(vdsNetworkInterface).interfaceIsValidSlave(), matcher);

    }
}
